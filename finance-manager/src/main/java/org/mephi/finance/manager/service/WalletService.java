package org.mephi.finance.manager.service;

import org.mephi.finance.manager.domain.entity.Wallet;
import org.mephi.finance.manager.dto.TransferDto;

import java.math.BigDecimal;
import java.util.UUID;

public interface WalletService {

    Wallet getUserWallet(UUID userId);
    Wallet updateBalance(UUID userId, BigDecimal amount, boolean isIncome);
    UUID transferToUser(TransferDto transferDto);
    void createWalletForUser(UUID userId);
}
