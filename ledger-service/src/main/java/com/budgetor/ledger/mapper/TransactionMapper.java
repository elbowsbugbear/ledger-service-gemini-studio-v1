package com.budgetor.ledger.mapper;

import com.budgetor.ledger.domain.Transaction;
import com.budgetor.ledger.dto.TransactionRequestDTO;
import com.budgetor.ledger.dto.TransactionResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Transaction toEntity(TransactionRequestDTO requestDTO);

    TransactionResponseDTO toResponseDTO(Transaction transaction);
}
