package org.mephi.finance.manager.service;

import org.mephi.finance.manager.domain.entity.Wallet;

import java.util.UUID;

public interface WalletSearchService {

    Wallet getUserWallet(UUID userId);
    UUID getWalletIdByUserId(UUID userId);
}
