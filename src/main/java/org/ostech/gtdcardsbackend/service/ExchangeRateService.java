package org.ostech.gtdcardsbackend.service;

import org.ostech.gtdcardsbackend.dto.ExchangeRateDTO;
import org.ostech.gtdcardsbackend.exception.CardServiceException;
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

    @Value("${app.exchangerate.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public ExchangeRateService() {
        this.restTemplate = new RestTemplate();
    }

    public ExchangeRateDTO getExchangeRates(String baseCurrency) {
        try {
            logger.info("Fetching exchange rates from external API for currency: {}", baseCurrency);

            String url = EXCHANGE_RATE_API_URL + apiKey + "/latest/" + baseCurrency;
            ExchangeRateDTO response = restTemplate.getForObject(url, ExchangeRateDTO.class);

            if (response == null) {
                throw new CardServiceException("External API returned null response");
            }

            if ("error".equalsIgnoreCase(response.getResult())) {
                logger.warn("External API returned error for currency: {}", baseCurrency);
                throw new CardServiceException("Failed to fetch exchange rates: " + response.getError());
            }

            logger.info("Successfully fetched exchange rates for currency: {}", baseCurrency);
            return response;

        } catch (RestClientException ex) {
            logger.error("Error calling external exchange rate API", ex);
            throw new CardServiceException("Failed to connect to exchange rate service: " + ex.getMessage(), ex);
        } catch (CardServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected error while fetching exchange rates", ex);
            throw new CardServiceException("Error fetching exchange rates: " + ex.getMessage(), ex);
        }
    }

}
