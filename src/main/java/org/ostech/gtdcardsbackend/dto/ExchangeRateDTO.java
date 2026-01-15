package org.ostech.gtdcardsbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRateDTO {

    @JsonProperty("base_code")
    private String baseCode;

    @JsonProperty("target_code")
    private String targetCode;

    @JsonProperty("conversion_rate")
    private BigDecimal conversionRate;

    @JsonProperty("time_last_updated")
    private String timeLastUpdated;

    @JsonProperty("conversion_rates")
    private Map<String, BigDecimal> conversionRates;

    private String error;

    private String result;
}
