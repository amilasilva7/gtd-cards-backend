package org.ostech.gtdcardsbackend.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CircuitBreakerConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(CircuitBreakerConfiguration.class);

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        return CircuitBreakerRegistry.of(getCircuitBreakerConfigDefaults());
    }

    @Bean
    public CircuitBreaker exchangeRateCircuitBreaker(CircuitBreakerRegistry circuitBreakerRegistry) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .slowCallRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .slowCallDurationThreshold(Duration.ofSeconds(3))
            .minimumNumberOfCalls(3)
            .recordExceptions(Exception.class)
            .build();

        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("exchangeRateAPI", config);
        circuitBreaker.getEventPublisher()
            .onStateTransition(event -> logger.warn("Circuit breaker state transition: {} -> {}",
                event.getStateTransition().getFromState(),
                event.getStateTransition().getToState()))
            .onError(event -> logger.error("Circuit breaker recorded error: {}", event.getThrowable().getMessage()))
            .onSuccess(event -> logger.debug("Circuit breaker recorded success"));

        return circuitBreaker;
    }

    private CircuitBreakerConfig getCircuitBreakerConfigDefaults() {
        return CircuitBreakerConfig.ofDefaults();
    }
}
