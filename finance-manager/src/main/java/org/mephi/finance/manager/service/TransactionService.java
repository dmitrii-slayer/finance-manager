package org.mephi.finance.manager.service;

import org.mephi.finance.manager.domain.entity.Transaction;
import org.mephi.finance.manager.dto.CreateTransactionDto;

import java.util.List;
import java.util.UUID;

public interface TransactionService {
    Transaction createTransaction(CreateTransactionDto dto);
    List<Transaction> getUserTransactions(UUID userId);
    List<Transaction> getUserTransactionsByCategory(UUID userId, String categoryName);
}
