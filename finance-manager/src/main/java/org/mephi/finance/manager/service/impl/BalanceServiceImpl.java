package org.mephi.finance.manager.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mephi.finance.manager.domain.TransactionType;
import org.mephi.finance.manager.domain.entity.Wallet;
import org.mephi.finance.manager.domain.repository.WalletRepository;
import org.mephi.finance.manager.exception.InsufficientFundsException;
import org.mephi.finance.manager.service.BalanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class BalanceServiceImpl implements BalanceService {

    private final WalletRepository walletRepository;

    @Override
    @Transactional
    public void updateBalance(Wallet wallet, BigDecimal amount, TransactionType type) {
        BigDecimal newBalance = type == TransactionType.INCOME
                ? wallet.getBalance().add(amount)
                : wallet.getBalance().subtract(amount);

        if (type == TransactionType.EXPENSE && newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientFundsException(
                    String.format("Недостаточно средств. Баланс: %s, Сумма: %s",
                            wallet.getBalance(), amount)
            );
        }

        wallet.setBalance(newBalance);
        wallet.setUpdatedAt(Instant.now());
        walletRepository.save(wallet);

        log.debug("Баланс обновлен. Кошелек: {}, Тип: {}, Сумма: {}, Новый баланс: {}",
                wallet.getId(), type, amount, newBalance);
    }

    @Override
    public void validateSufficientFunds(Wallet wallet, BigDecimal amount) {
        BigDecimal balance = wallet.getBalance();
        if (balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException(
                    String.format("Недостаточно средств. Доступно: %s, Требуется: %s",
                            balance, amount)
            );
        }
    }
}
