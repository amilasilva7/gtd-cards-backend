package org.ostech.gtdcardsbackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExternalApiRequestDTO {

    @NotBlank(message = "User ID is required")
    private String userId;

    private String additionalData;
}
