package org.ostech.gtdcardsbackend.service;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.ostech.gtdcardsbackend.dto.ExchangeRateDTO;
import org.ostech.gtdcardsbackend.exception.CardServiceException;
import org.ostech.gtdcardsbackend.exception.ExternalAPIUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class ExchangeRateService {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeRateService.class);

    private static final String EXCHANGE_RATE_API_URL = "https://v6.exchangerate-api.com/v6/"; // TODO: Move this to dev app params/ or DB
    private final RestTemplate restTemplate;
    private final CircuitBreaker circuitBreaker;
    @Value("${app.exchangerate.api-key}")
    private String apiKey;

    public ExchangeRateService(CircuitBreaker circuitBreaker) {
        this.restTemplate = new RestTemplate();
        this.circuitBreaker = circuitBreaker;
    }

    public ExchangeRateDTO getExchangeRates(String baseCurrency) {
        try {
            logger.info("Fetching exchange rates from external API for currency: {}", baseCurrency);

            ExchangeRateDTO response = circuitBreaker.executeSupplier(() ->
                callExchangeRateAPI(baseCurrency)
            );

            if (response == null) {
                throw new CardServiceException("External API returned null response");
            }

            if ("error".equalsIgnoreCase(response.getResult())) {
                logger.warn("External API returned error for currency: {}", baseCurrency);
                throw new CardServiceException("Failed to fetch exchange rates: " + response.getError());
            }

            logger.info("Successfully fetched exchange rates for currency: {}", baseCurrency);
            return response;

        } catch (CallNotPermittedException ex) {
            logger.error("Circuit breaker is OPEN - Exchange rate API is unavailable", ex);
            throw new ExternalAPIUnavailableException(
                "Exchange rate service is temporarily unavailable. Please try again later.",
                ex
            );
        } catch (RestClientException ex) {
            logger.error("Error calling external exchange rate API", ex);
            throw new CardServiceException("Failed to connect to exchange rate service: " + ex.getMessage(), ex);
        } catch (ExternalAPIUnavailableException ex) {
            throw ex;
        } catch (CardServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected error while fetching exchange rates", ex);
            throw new CardServiceException("Error fetching exchange rates: " + ex.getMessage(), ex);
        }
    }

    private ExchangeRateDTO callExchangeRateAPI(String baseCurrency) {
        String url = EXCHANGE_RATE_API_URL + apiKey + "/latest/" + baseCurrency;
        return restTemplate.getForObject(url, ExchangeRateDTO.class);
    }

}
