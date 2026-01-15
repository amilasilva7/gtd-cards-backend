package org.ostech.gtdcardsbackend.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequestDTO {

    @NotNull(message = "Card ID is required")
    @Positive(message = "Card ID must be positive")
    private Long cardId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @DecimalMax(value = "999999.99", message = "Amount exceeds maximum limit")
    @Digits(integer = 6, fraction = 2, message = "Amount must have max 6 digits and 2 decimal places")
    private BigDecimal amount;

    @NotBlank(message = "Transaction type is required")
    @Pattern(regexp = "^(PURCHASE|WITHDRAWAL|REFUND|TRANSFER)$",
        message = "Transaction type must be PURCHASE, WITHDRAWAL, REFUND, or TRANSFER")
    private String transactionType;

    @Size(max = 200, message = "Description cannot exceed 200 characters")
    private String description;

    @Size(max = 100, message = "Merchant name cannot exceed 100 characters")
    private String merchantName;
}
