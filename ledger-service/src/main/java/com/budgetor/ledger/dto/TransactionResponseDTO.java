package com.budgetor.ledger.dto;

import com.budgetor.ledger.domain.TemporalType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponseDTO {
    private Long id;
    private String description;
    private BigDecimal amount;
    private TemporalType temporalType;
    private LocalDateTime transactionTimestamp;
    private LocalDateTime createdAt;
}
