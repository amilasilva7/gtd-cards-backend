package org.ostech.gtdcardsbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExternalApiResponseDTO {

    private String externalId;

    private String externalData;

    private String statusFromExternalApi;

    private Object rawResponse;
}
