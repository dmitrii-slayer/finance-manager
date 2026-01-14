package org.mephi.finance.manager.mapper;

import org.mapstruct.Mapper;
import org.mephi.finance.manager.domain.TransactionType;
import org.mephi.finance.manager.domain.entity.Transaction;
import org.mephi.finance.manager.dto.CreateTransactionDto;
import org.mephi.finance.manager.model.TransactionRequest;
import org.mephi.finance.manager.model.TransactionResponse;

import java.util.UUID;

@Mapper
public interface TransactionMapper {

    CreateTransactionDto toLocalDto(TransactionRequest request, UUID userId, TransactionType type);

    TransactionResponse toApiResponse(Transaction transaction, String categoryName);
}
