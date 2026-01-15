package org.ostech.gtdcardsbackend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExternalApiResponseDTO {

    private CardResponseDTO cardData;

    private String externalData;

    private String source;

    private String statusFromExternalApi;

    private Object rawResponse;
}
