package com.budgetor.ledger.service;

import com.budgetor.ledger.domain.TemporalType;
import com.budgetor.ledger.domain.Transaction;
import com.budgetor.ledger.dto.TransactionRequestDTO;
import com.budgetor.ledger.dto.TransactionResponseDTO;
import com.budgetor.ledger.exception.BusinessRuleException;
import com.budgetor.ledger.mapper.TransactionMapper;
import com.budgetor.ledger.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LedgerService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    @Transactional
    public TransactionResponseDTO createTransaction(TransactionRequestDTO requestDTO) {
        log.info("Processing request to create transaction: description='{}', type='{}', amount='{}'",
                requestDTO.getDescription(), requestDTO.getTemporalType(), requestDTO.getAmount());

        // Rule check: ACTUAL temporal transactions cannot be scheduled with a future timestamp
        if (requestDTO.getTemporalType() == TemporalType.ACTUAL &&
                requestDTO.getTransactionTimestamp().isAfter(LocalDateTime.now())) {
            log.warn("Validation failed: ACTUAL temporal transaction cannot have a future timestamp: {}",
                    requestDTO.getTransactionTimestamp());
            throw new BusinessRuleException("An ACTUAL transaction cannot be scheduled with a future timestamp");
        }

        Transaction transaction = transactionMapper.toEntity(requestDTO);
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        log.info("Transaction saved successfully with ID: {}", savedTransaction.getId());
        return transactionMapper.toResponseDTO(savedTransaction);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getAllTransactions() {
        log.info("Fetching all transaction records from the ledger...");
        return transactionRepository.findAll().stream()
                .map(transactionMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}
