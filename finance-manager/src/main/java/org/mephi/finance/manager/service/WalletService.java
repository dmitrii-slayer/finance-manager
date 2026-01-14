package org.mephi.finance.manager.service;

import org.mephi.finance.manager.dto.TransferDto;

import java.util.UUID;

public interface WalletService {

    void createWalletForUser(UUID userId);
    UUID transferToUser(TransferDto transferDto);
}
