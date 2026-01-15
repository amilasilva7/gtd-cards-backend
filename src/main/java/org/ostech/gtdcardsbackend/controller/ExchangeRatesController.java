package org.ostech.gtdcardsbackend.controller;

import org.ostech.gtdcardsbackend.dto.ApiResponseDTO;
import org.ostech.gtdcardsbackend.dto.ExchangeRateDTO;
import org.ostech.gtdcardsbackend.service.ExchangeRateService;
import org.ostech.gtdcardsbackend.util.APIConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(APIConstants.EXCHANGE_RATES_ENDPOINT)
public class ExchangeRatesController {

    private final ExchangeRateService exchangeRateService;

    public ExchangeRatesController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @GetMapping
    public ResponseEntity<ApiResponseDTO<ExchangeRateDTO>> getExchangeRates(
        @RequestParam(defaultValue = "USD") String baseCurrency) {
        ExchangeRateDTO exchangeRates = exchangeRateService.getExchangeRates(baseCurrency);
        ApiResponseDTO<ExchangeRateDTO> response = ApiResponseDTO.success(
            exchangeRates,
            "Exchange rates retrieved successfully"
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
