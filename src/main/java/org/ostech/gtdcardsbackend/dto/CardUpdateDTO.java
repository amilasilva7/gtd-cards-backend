package org.ostech.gtdcardsbackend.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardUpdateDTO {

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Name must contain only letters and spaces")
    private String holderName;

    @Pattern(regexp = "^(ACTIVE|BLOCKED|EXPIRED|SUSPENDED)$",
        message = "Status must be ACTIVE, BLOCKED, EXPIRED, or SUSPENDED")
    private String status;
}
