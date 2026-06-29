package com.budgetor.ledger.controller;

import com.budgetor.ledger.dto.TransactionRequestDTO;
import com.budgetor.ledger.dto.TransactionResponseDTO;
import com.budgetor.ledger.service.LedgerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ledger")
@RequiredArgsConstructor
@Slf4j
public class LedgerController {

    private final LedgerService ledgerService;

    @PostMapping
    public ResponseEntity<TransactionResponseDTO> createTransaction(
            @Valid @RequestBody TransactionRequestDTO requestDTO) {
        log.info("API request received: POST /api/v1/ledger");
        TransactionResponseDTO response = ledgerService.createTransaction(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponseDTO>> getAllTransactions() {
        log.info("API request received: GET /api/v1/ledger");
        List<TransactionResponseDTO> response = ledgerService.getAllTransactions();
        return ResponseEntity.ok(response);
    }
}
