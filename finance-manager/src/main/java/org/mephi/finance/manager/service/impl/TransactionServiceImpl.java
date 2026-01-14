package org.mephi.finance.manager.service.impl;

import lombok.RequiredArgsConstructor;
import org.mephi.finance.manager.domain.TransactionType;
import org.mephi.finance.manager.domain.entity.Category;
import org.mephi.finance.manager.domain.entity.Transaction;
import org.mephi.finance.manager.domain.entity.Wallet;
import org.mephi.finance.manager.domain.repository.CategoryRepository;
import org.mephi.finance.manager.domain.repository.TransactionRepository;
import org.mephi.finance.manager.domain.repository.WalletRepository;
import org.mephi.finance.manager.dto.CreateTransactionDto;
import org.mephi.finance.manager.service.CategoryService;
import org.mephi.finance.manager.service.TransactionService;
import org.mephi.finance.manager.service.WalletService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final WalletRepository walletRepository;
    private final WalletService walletService;
    private final CategoryService categoryService;

    @Override
    @Transactional
    public Transaction createTransaction(CreateTransactionDto dto) {
        UUID userId = dto.getUserId();
        Wallet wallet = walletService.getUserWallet(userId);

        Category category = getOrCreateCategory(dto.getCategoryName(), dto.getType(), userId);

        Transaction transaction = Transaction.builder()
                .type(dto.getType())
                .description(dto.getDescription())
                .amount(dto.getAmount())
                .walletId(wallet.getId())
                .categoryId(category.getId())
                .timestamp(Instant.now())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        if (transaction.getType() == TransactionType.EXPENSE) {
            category.addToSpentAmount(transaction.getAmount());
            categoryService.save(category);
        }
        updateWalletBalance(wallet, dto.getAmount(), dto.getType());

        return savedTransaction;
    }

    @Override
    public List<Transaction> getUserTransactions(UUID userId) {
        Wallet wallet = walletService.getUserWallet(userId);

        return transactionRepository.findByWalletId(wallet.getId());
    }

    @Override
    public List<Transaction> getUserTransactionsByCategory(UUID userId, String categoryName) {
        // могут быть категории с одинаковыми названиями но разными типами - Переводы (доход) и Переводы (расходы)
        List<Category> categories = categoryRepository.findByNameAndUserId(categoryName, userId);
        Set<UUID> ids = categories.stream().map(Category::getId).collect(Collectors.toSet());
        Wallet wallet = walletService.getUserWallet(userId);

        return transactionRepository.findByWalletIdAndCategoryIdIn(
                wallet.getId(), ids
        );
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

    private void updateWalletBalance(Wallet wallet, BigDecimal amount, TransactionType type) {
        BigDecimal newBalance = type == TransactionType.INCOME
                ? wallet.getBalance().add(amount)
                : wallet.getBalance().subtract(amount);

        wallet.setBalance(newBalance);
        wallet.setUpdatedAt(Instant.now());
        walletRepository.save(wallet);
    }
}
