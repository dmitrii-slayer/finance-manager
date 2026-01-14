package org.mephi.finance.manager.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mephi.finance.manager.domain.TransactionType;
import org.mephi.finance.manager.domain.entity.Category;
import org.mephi.finance.manager.domain.entity.Transaction;
import org.mephi.finance.manager.domain.entity.User;
import org.mephi.finance.manager.domain.entity.Wallet;
import org.mephi.finance.manager.domain.repository.TransactionRepository;
import org.mephi.finance.manager.domain.repository.WalletRepository;
import org.mephi.finance.manager.dto.CreateCategoryDto;
import org.mephi.finance.manager.dto.TransferDto;
import org.mephi.finance.manager.exception.InsufficientFundsException;
import org.mephi.finance.manager.exception.ResourceNotFoundException;
import org.mephi.finance.manager.service.CategoryService;
import org.mephi.finance.manager.service.UserSearchService;
import org.mephi.finance.manager.service.WalletService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final UserSearchService userSearchService;
    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;

    @Override
    public Wallet getUserWallet(UUID userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Не найден кошелек пользователя: " + userId
                ));
    }

    @Override
    @Transactional
    public Wallet updateBalance(UUID userId, BigDecimal amount, boolean isIncome) {
        Wallet wallet = getUserWallet(userId);

        BigDecimal newBalance = getNewBalance(amount, isIncome, wallet);

        wallet.setBalance(newBalance);
        wallet.setUpdatedAt(Instant.now());

        Wallet updatedWallet = walletRepository.save(wallet);

        log.info("Баланс кошелька обновлен. ID пользователя: {}, новый баланс: {}, сумма: {} ({})",
                userId, newBalance, amount, isIncome ? "доход" : "расход");

        return updatedWallet;
    }

    private static BigDecimal getNewBalance(BigDecimal amount, boolean isIncome, Wallet wallet) {
        BigDecimal newBalance;
        if (isIncome) {
            newBalance = wallet.getBalance().add(amount);
        } else {
            if (wallet.getBalance().compareTo(amount) < 0) {
                throw new InsufficientFundsException(
                        "Недостаточно средств. Текущий баланс: " + wallet.getBalance() +
                                ", требуется: " + amount
                );
            }
            newBalance = wallet.getBalance().subtract(amount);
        }
        return newBalance;
    }

    @Override
    @Transactional
    public UUID transferToUser(TransferDto transferDto) {
        UUID senderUserId = transferDto.getUserId();

        Wallet senderWallet = walletRepository.findByUserId(senderUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Не найден кошелек отправителя: " + senderUserId
                ));

        if (senderWallet.getBalance().compareTo(transferDto.getAmount()) < 0) {
            throw new InsufficientFundsException(
                    "Недостаточно средств для перевода. Текущий баланс: " +
                            senderWallet.getBalance() + ", сумма перевода: " + transferDto.getAmount()
            );
        }

        User recipientUser = userSearchService.findByUsername(transferDto.getTargetUsername())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Получатель не найден: " + transferDto.getTargetUsername()
                ));

        User senderUser = userSearchService.findByUserId(senderUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Отправитель не найден: " + transferDto.getTargetUsername()
                ));

        Wallet recipientWallet = walletRepository.findByUserId(recipientUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Не найден кошелек получателя: " + recipientUser.getId()
                ));

        senderWallet.setBalance(senderWallet.getBalance().subtract(transferDto.getAmount()));
        senderWallet.setUpdatedAt(Instant.now());

        recipientWallet.setBalance(recipientWallet.getBalance().add(transferDto.getAmount()));
        recipientWallet.setUpdatedAt(Instant.now());

        walletRepository.save(senderWallet);
        walletRepository.save(recipientWallet);

        UUID senderTransactionId = createTransferTransactions(senderUserId, recipientUser.getId(), transferDto);

        log.info("Перевод завершен. От: {} (ID: {}), Кому: {} (ID: {}), Сумма: {}",
                senderUser.getUsername(), senderUserId,
                transferDto.getTargetUsername(), recipientUser.getId(),
                transferDto.getAmount());

        return senderTransactionId;
    }

    @Override
    @Transactional
    public void createWalletForUser(UUID userId) {
        walletRepository.findByUserId(userId).ifPresent(existingWallet -> {
            throw new IllegalArgumentException("Кошелек для пользователя уже существует: " + userId);
        });

        Wallet wallet = Wallet.builder()
                .userId(userId)
                .balance(BigDecimal.ZERO)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Wallet savedWallet = walletRepository.save(wallet);

        log.info("Кошелек создан для пользователя с ID: {} - ID кошелька: {}", userId, savedWallet.getId());
    }

    private UUID createTransferTransactions(UUID senderId, UUID recipientId, TransferDto transferDto) {
        Instant timestamp = Instant.now();

        Transaction senderTransaction = Transaction.builder()
                .walletId(walletRepository.findByUserId(senderId).get().getId())
                .categoryId(getOrCreateTransferCategory(senderId, TransactionType.EXPENSE))
                .amount(transferDto.getAmount())
                .description("Перевод пользователю " + transferDto.getTargetUsername() +
                        (transferDto.getDescription() != null ? ": " + transferDto.getDescription() : ""))
                .type(TransactionType.EXPENSE)
                .timestamp(timestamp)
                .build();

        Transaction recipientTransaction = Transaction.builder()
                .walletId(walletRepository.findByUserId(recipientId).get().getId())
                .categoryId(getOrCreateTransferCategory(recipientId, TransactionType.INCOME))
                .amount(transferDto.getAmount())
                .description("Перевод от пользователя " + senderId +
                        (transferDto.getDescription() != null ? ": " + transferDto.getDescription() : ""))
                .type(TransactionType.INCOME)
                .timestamp(timestamp)
                .build();

        Transaction savedSenderTransaction = transactionRepository.save(senderTransaction);
        transactionRepository.save(recipientTransaction);
        return savedSenderTransaction.getId();
    }

    private UUID getOrCreateTransferCategory(UUID userId, TransactionType type) {
        String categoryName = "Переводы";
        Optional<Category> category = categoryService.findByNameAndUserIdAndType(categoryName, userId, type);
        if (category.isPresent()) {
            return category.get().getId();
        }

        CreateCategoryDto createCategoryDto = CreateCategoryDto.builder()
                .userId(userId)
                .name(categoryName)
                .type(type)
                .build();
        Category newCategory = categoryService.createCategory(createCategoryDto);
        return newCategory.getId();
    }
}
