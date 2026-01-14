package org.mephi.finance.manager.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mephi.finance.manager.domain.TransactionType;
import org.mephi.finance.manager.domain.entity.User;
import org.mephi.finance.manager.domain.entity.Wallet;
import org.mephi.finance.manager.domain.repository.WalletRepository;
import org.mephi.finance.manager.dto.TransferDto;
import org.mephi.finance.manager.exception.ResourceNotFoundException;
import org.mephi.finance.manager.service.BalanceService;
import org.mephi.finance.manager.service.TransactionService;
import org.mephi.finance.manager.service.UserSearchService;
import org.mephi.finance.manager.service.WalletSearchService;
import org.mephi.finance.manager.service.WalletService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletSearchService walletSearchService;
    private final UserSearchService userSearchService;
    private final TransactionService transactionService;
    private final BalanceService balanceService;

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

        walletRepository.save(wallet);
        log.info("Кошелек создан для пользователя: {}", userId);
    }

    @Override
    @Transactional
    public UUID transferToUser(TransferDto transferDto) {
        UUID senderId = transferDto.getUserId();
        UUID recipientId = getRecipientId(transferDto.getTargetUsername());

        Wallet senderWallet = walletSearchService.getUserWallet(senderId);
        Wallet recipientWallet = walletSearchService.getUserWallet(recipientId);

        balanceService.validateSufficientFunds(senderWallet, transferDto.getAmount());

        balanceService.updateBalance(senderWallet, transferDto.getAmount(), TransactionType.EXPENSE);
        balanceService.updateBalance(recipientWallet, transferDto.getAmount(), TransactionType.INCOME);

        UUID transactionId = transactionService.createTransferTransaction(
                senderId, recipientId, transferDto.getAmount(), transferDto.getDescription()
        );

        log.info("Перевод завершен. От: {}, Кому: {}, Сумма: {}",
                senderId, recipientId, transferDto.getAmount());

        return transactionId;
    }

    private UUID getRecipientId(String username) {
        User recipient = userSearchService.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Получатель не найден: " + username
                ));
        return recipient.getId();
    }
}
