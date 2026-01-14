package org.mephi.finance.manager.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mephi.finance.manager.domain.TransactionType;
import org.mephi.finance.manager.domain.entity.Category;
import org.mephi.finance.manager.domain.entity.Transaction;
import org.mephi.finance.manager.domain.entity.User;
import org.mephi.finance.manager.domain.entity.Wallet;
import org.mephi.finance.manager.domain.repository.CategoryRepository;
import org.mephi.finance.manager.domain.repository.TransactionRepository;
import org.mephi.finance.manager.dto.CreateTransactionDto;
import org.mephi.finance.manager.exception.ResourceNotFoundException;
import org.mephi.finance.manager.service.BalanceService;
import org.mephi.finance.manager.service.CategoryService;
import org.mephi.finance.manager.service.TransactionService;
import org.mephi.finance.manager.service.UserSearchService;
import org.mephi.finance.manager.service.WalletSearchService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;
    private final BalanceService balanceService;
    private final UserSearchService userSearchService;
    private final WalletSearchService walletSearchService;

    @Override
    @Transactional
    public Transaction createTransaction(CreateTransactionDto dto) {
        UUID userId = dto.getUserId();
        Wallet wallet = walletSearchService.getUserWallet(userId);

        Category category = getOrCreateCategory(dto.getCategoryName(), dto.getType(), userId);

        if (dto.getType() == TransactionType.EXPENSE) {
            balanceService.validateSufficientFunds(wallet, dto.getAmount());
        }

        Transaction transaction = Transaction.builder()
                .type(dto.getType())
                .description(dto.getDescription())
                .amount(dto.getAmount())
                .walletId(wallet.getId())
                .categoryId(category.getId())
                .timestamp(Instant.now())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        balanceService.updateBalance(wallet, dto.getAmount(), dto.getType());

        if (dto.getType() == TransactionType.EXPENSE && category.hasBudget()) {
            category.addToSpentAmount(dto.getAmount());
            categoryService.save(category);
        }

        log.info("Транзакция создана: {} {} в категории {}",
                dto.getType(), dto.getAmount(), category.getName());

        return savedTransaction;
    }

    @Override
    @Transactional
    public UUID createTransferTransaction(UUID senderId, UUID recipientId,
                                          BigDecimal amount, String description) {
        Instant timestamp = Instant.now();

        UUID senderWalletId = walletSearchService.getWalletIdByUserId(senderId);
        UUID recipientWalletId = walletSearchService.getWalletIdByUserId(recipientId);

        User sender = userSearchService.findByUserId(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Отправитель не найден: " + senderId));
        User recipient = userSearchService.findByUserId(recipientId)
                .orElseThrow(() -> new ResourceNotFoundException("Получатель не найден: " + recipientId));

        Transaction senderTransaction = createTransferTransactionRecord(
                senderWalletId, senderId, recipient.getUsername(),
                amount, description, TransactionType.EXPENSE, timestamp
        );

        Transaction recipientTransaction = createTransferTransactionRecord(
                recipientWalletId, recipientId, sender.getUsername(),
                amount, description, TransactionType.INCOME, timestamp
        );

        transactionRepository.save(senderTransaction);
        transactionRepository.save(recipientTransaction);

        log.debug("Транзакции перевода созданы. От: {}, Кому: {}, Сумма: {}",
                senderId, recipientId, amount);

        return senderTransaction.getId();
    }

    @Override
    public List<Transaction> getUserTransactions(UUID userId) {
        UUID walletId = walletSearchService.getWalletIdByUserId(userId);
        return transactionRepository.findByWalletId(walletId);
    }

    @Override
    public List<Transaction> getUserTransactionsByCategory(UUID userId, String categoryName) {
        // могут быть категории с одинаковыми названиями но разными типами - Переводы (доход) и Переводы (расходы)
        List<Category> categories = categoryRepository.findByNameAndUserId(categoryName, userId);
        Set<UUID> categoryIds = categories.stream()
                .map(Category::getId)
                .collect(Collectors.toSet());

        UUID walletId = walletSearchService.getWalletIdByUserId(userId);
        return transactionRepository.findByWalletIdAndCategoryIdIn(walletId, categoryIds);
    }

    private Category getOrCreateCategory(String name, TransactionType type, UUID userId) {
        return categoryRepository.findByNameAndUserIdAndType(name, userId, type)
                .orElseGet(() -> {
                    Category newCategory = Category.builder()
                            .name(name)
                            .userId(userId)
                            .type(type)
                            .build();
                    return categoryRepository.save(newCategory);
                });
    }

    private Transaction createTransferTransactionRecord(UUID walletId, UUID userId,
                                                        String counterpartyUsername,
                                                        BigDecimal amount, String description,
                                                        TransactionType type, Instant timestamp) {
        Category category = getOrCreateTransferCategory(userId, type);

        String transactionDescription = getTransactionDescription(counterpartyUsername, description, type);

        return Transaction.builder()
                .walletId(walletId)
                .categoryId(category.getId())
                .amount(amount)
                .description(transactionDescription)
                .type(type)
                .timestamp(timestamp)
                .build();
    }

    private String getTransactionDescription(String counterpartyUsername, String description, TransactionType type) {
        String senderTransactionDescription =  String.format("Перевод пользователю %s: %s",
                counterpartyUsername,
                description != null ? ": " + description : "");

        String recipientTransactionDescription =  String.format("Перевод от пользователя %s: %s",
                counterpartyUsername,
                description != null ? ": " + description : "");

        return type == TransactionType.EXPENSE ?
                senderTransactionDescription : recipientTransactionDescription;
    }

    private Category getOrCreateTransferCategory(UUID userId, TransactionType type) {
        String categoryName = "Переводы";
        return categoryRepository.findByNameAndUserIdAndType(categoryName, userId, type)
                .orElseGet(() -> {
                    Category newCategory = Category.builder()
                            .name(categoryName)
                            .userId(userId)
                            .type(type)
                            .build();
                    return categoryRepository.save(newCategory);
                });
    }
}
