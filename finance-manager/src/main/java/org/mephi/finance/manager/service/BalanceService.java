package org.mephi.finance.manager.service;

import org.mephi.finance.manager.domain.TransactionType;
import org.mephi.finance.manager.domain.entity.Wallet;

import java.math.BigDecimal;

public interface BalanceService {

    void updateBalance(Wallet walletId, BigDecimal amount, TransactionType type);
    void validateSufficientFunds(Wallet wallet, BigDecimal amount);
}
