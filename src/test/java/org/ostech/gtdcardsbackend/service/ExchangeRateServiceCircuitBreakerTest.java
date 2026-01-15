package org.ostech.gtdcardsbackend.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ostech.gtdcardsbackend.dto.ExchangeRateDTO;
import org.ostech.gtdcardsbackend.exception.CardServiceException;
import org.ostech.gtdcardsbackend.exception.ExternalAPIUnavailableException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExchangeRateService - Circuit Breaker Integration Tests")
class ExchangeRateServiceCircuitBreakerTest {

    @Mock
    private RestTemplate restTemplate;

    private ExchangeRateService exchangeRateService;
    private CircuitBreaker circuitBreaker;

    @BeforeEach
    void setUp() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .slowCallRateThreshold(50)
            .waitDurationInOpenState(Duration.ofMillis(500))
            .slowCallDurationThreshold(Duration.ofSeconds(3))
            .minimumNumberOfCalls(3)
            .recordExceptions(Exception.class)
            .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        circuitBreaker = registry.circuitBreaker("testExchangeRateAPI", config);

        exchangeRateService = new ExchangeRateService(circuitBreaker);
        ReflectionTestUtils.setField(exchangeRateService, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(exchangeRateService, "apiKey", "test-api-key");
    }

    @Test
    @DisplayName("1. Should successfully call external API and return exchange rates")
    void testSuccessfulAPICall_ReturnsExchangeRates() {
        ExchangeRateDTO mockResponse = ExchangeRateDTO.builder()
            .result("success")
            .baseCode("USD")
            .conversionRates(new java.util.HashMap<>())
            .build();

        when(restTemplate.getForObject(anyString(), eq(ExchangeRateDTO.class)))
            .thenReturn(mockResponse);

        ExchangeRateDTO response = exchangeRateService.getExchangeRates("USD");

        assertThat(response)
            .isNotNull()
            .hasFieldOrPropertyWithValue("result", "success")
            .hasFieldOrPropertyWithValue("baseCode", "USD");

        assertThat(circuitBreaker.getState().toString()).isEqualTo("CLOSED");
        verify(restTemplate, times(1)).getForObject(anyString(), eq(ExchangeRateDTO.class));
    }

    @Test
    @DisplayName("2. Circuit Breaker Opens after 3 consecutive failures - Fail Fast Behavior")
    void testCircuitBreakerOpensAfterThreeFailures_FailFast() {
        when(restTemplate.getForObject(anyString(), eq(ExchangeRateDTO.class)))
            .thenThrow(new RestClientException("API connection timeout"));

        assertThatThrownBy(() -> exchangeRateService.getExchangeRates("USD"))
            .isInstanceOf(CardServiceException.class);
        assertThat(circuitBreaker.getState().toString()).isEqualTo("CLOSED");

        assertThatThrownBy(() -> exchangeRateService.getExchangeRates("USD"))
            .isInstanceOf(CardServiceException.class);
        assertThat(circuitBreaker.getState().toString()).isEqualTo("CLOSED");

        assertThatThrownBy(() -> exchangeRateService.getExchangeRates("USD"))
            .isInstanceOf(CardServiceException.class);
        assertThat(circuitBreaker.getState().toString()).isEqualTo("OPEN");

        assertThatThrownBy(() -> exchangeRateService.getExchangeRates("USD"))
            .isInstanceOf(ExternalAPIUnavailableException.class)
            .hasMessageContaining("temporarily unavailable");

        verify(restTemplate, times(3)).getForObject(anyString(), eq(ExchangeRateDTO.class));
    }

    @Test
    @DisplayName("3. Circuit Breaker Recovers after wait duration - Half-Open State")
    void testCircuitBreakerRecovery_AfterWaitDuration() throws InterruptedException {
        when(restTemplate.getForObject(anyString(), eq(ExchangeRateDTO.class)))
            .thenThrow(new RestClientException("API connection timeout"));

        for (int i = 0; i < 3; i++) {
            try {
                exchangeRateService.getExchangeRates("USD");
            } catch (Exception e) {
            }
        }

        String stateBeforeWait = circuitBreaker.getState().toString();
        assertThat(stateBeforeWait).isEqualTo("OPEN");

        int failuresBeforeWait = circuitBreaker.getMetrics().getNumberOfFailedCalls();
        assertThat(failuresBeforeWait).isEqualTo(3);

        Thread.sleep(600);

        ExchangeRateDTO mockResponse = ExchangeRateDTO.builder()
            .result("success")
            .baseCode("USD")
            .conversionRates(new java.util.HashMap<>())
            .build();

        when(restTemplate.getForObject(anyString(), eq(ExchangeRateDTO.class)))
            .thenReturn(mockResponse);

        ExchangeRateDTO response = exchangeRateService.getExchangeRates("USD");

        assertThat(response)
            .isNotNull()
            .hasFieldOrPropertyWithValue("result", "success");

        String stateAfterRecovery = circuitBreaker.getState().toString();
        assertThat(stateAfterRecovery)
            .as("Circuit should allow successful calls (HALF_OPEN or CLOSED)")
            .isIn("HALF_OPEN", "CLOSED");

        long successfulCalls = circuitBreaker.getMetrics().getNumberOfSuccessfulCalls();
        assertThat(successfulCalls).isGreaterThan(0);

        assertThat(failuresBeforeWait).isEqualTo(3);
        assertThat(successfulCalls).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("4. Circuit Breaker Rejects Fast when API is Down - No Hanging")
    void testCircuitBreakerRejectsFast_NeverHangs() {
        when(restTemplate.getForObject(anyString(), eq(ExchangeRateDTO.class)))
            .thenThrow(new RestClientException("Connection refused - API server down"));

        for (int i = 0; i < 3; i++) {
            try {
                exchangeRateService.getExchangeRates("USD");
            } catch (Exception e) {
            }
        }

        assertThat(circuitBreaker.getState().toString()).isEqualTo("OPEN");

        long startTime = System.currentTimeMillis();

        assertThatThrownBy(() -> exchangeRateService.getExchangeRates("USD"))
            .isInstanceOf(ExternalAPIUnavailableException.class)
            .hasMessageContaining("temporarily unavailable");

        long duration = System.currentTimeMillis() - startTime;

        assertThat(duration)
            .as("Response time should be immediate (< 100ms), not hanging")
            .isLessThan(100);

        verify(restTemplate, times(3)).getForObject(anyString(), eq(ExchangeRateDTO.class));
    }

    @Test
    @DisplayName("5. Circuit Breaker Metrics - Tracks failures and recovery")
    void testCircuitBreakerMetrics_TracksFailuresAndRecovery() {
        when(restTemplate.getForObject(anyString(), eq(ExchangeRateDTO.class)))
            .thenThrow(new RestClientException("Temporary API error"));

        for (int i = 0; i < 3; i++) {
            try {
                exchangeRateService.getExchangeRates("USD");
            } catch (Exception e) {
                // Expected
            }
        }

        assertThat(circuitBreaker.getState().toString()).isEqualTo("OPEN");

        assertThat(circuitBreaker.getMetrics().getNumberOfFailedCalls()).isEqualTo(3);
        assertThat(circuitBreaker.getMetrics().getNumberOfSuccessfulCalls()).isEqualTo(0);
        assertThat(circuitBreaker.getMetrics().getNumberOfNotPermittedCalls()).isGreaterThanOrEqualTo(0);

        assertThat(circuitBreaker.getMetrics().getNumberOfFailedCalls())
            .as("Circuit breaker should track all failures")
            .isEqualTo(3);
    }
}
