package com.budgetor.ledger.dto;

import com.budgetor.ledger.domain.TemporalType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionRequestDTO {

    @NotBlank(message = "Description cannot be blank")
    @Size(max = 100, message = "Description must not exceed 100 characters")
    private String description;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Temporal type (ACTUAL or PROJECTED) is required")
    private TemporalType temporalType;

    @NotNull(message = "Transaction timestamp is required")
    private LocalDateTime transactionTimestamp;
}
