package org.mephi.finance.manager.rest.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mephi.finance.manager.api.TransactionApi;
import org.mephi.finance.manager.domain.TransactionType;
import org.mephi.finance.manager.domain.entity.Category;
import org.mephi.finance.manager.domain.entity.Transaction;
import org.mephi.finance.manager.mapper.TransactionMapper;
import org.mephi.finance.manager.model.TransactionRequest;
import org.mephi.finance.manager.model.TransactionResponse;
import org.mephi.finance.manager.model.TransactionTypeEnum;
import org.mephi.finance.manager.service.CategoryService;
import org.mephi.finance.manager.service.CurrentUserService;
import org.mephi.finance.manager.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TransactionController implements TransactionApi {

    private final TransactionService transactionService;
    private final CategoryService categoryService;
    private final TransactionMapper transactionMapper;
    private final CurrentUserService currentUserService;

    @Override
    public ResponseEntity<TransactionResponse> addExpenseTransaction(TransactionRequest transactionRequest) {
        log.info("Добавление транзакции(расход) по категории: {}", transactionRequest.getCategoryName());

        UUID userId = currentUserService.getCurrentUserId();
        Transaction transaction = transactionService.createTransaction(
                transactionMapper.toLocalDto(transactionRequest, userId, TransactionType.EXPENSE));

        return ResponseEntity.ok(transactionMapper.toApiResponse(transaction, transactionRequest.getCategoryName()));

    }

    @Override
    public ResponseEntity<TransactionResponse> addIncomeTransaction(TransactionRequest transactionRequest) {
        log.info("Добавление транзакции(доход) по категории: {}", transactionRequest.getCategoryName());

        UUID userId = currentUserService.getCurrentUserId();
        Transaction transaction = transactionService.createTransaction(
                transactionMapper.toLocalDto(transactionRequest, userId, TransactionType.INCOME));

        return ResponseEntity.ok(transactionMapper.toApiResponse(transaction, transactionRequest.getCategoryName()));

    }

    @Override
    public ResponseEntity<List<TransactionResponse>> getTransactions(TransactionTypeEnum type, String category) {
        log.info("Получение транзакций, фильтры - тип: {}, категория: {}", type, category);

        UUID currentUserId = currentUserService.getCurrentUserId();

        List<Transaction> transactions;

        if (category != null) {
            transactions = transactionService.getUserTransactionsByCategory(currentUserId, category);
        } else {
            transactions = transactionService.getUserTransactions(currentUserId);
        }

        if (type != null) {
            TransactionType transactionType = TransactionType.valueOf(type.name());
            transactions = transactions.stream()
                    .filter(t -> t.getType() == transactionType)
                    .collect(Collectors.toList());
        }

        List<TransactionResponse> responses = transactions.stream()
                .map(transaction -> {
                    Category cat = categoryService.getCategoryById(transaction.getCategoryId());
                    return transactionMapper.toApiResponse(transaction, cat.getName());
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }
}
