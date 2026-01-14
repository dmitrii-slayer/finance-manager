package org.mephi.finance.manager.service.impl;

import lombok.RequiredArgsConstructor;
import org.mephi.finance.manager.domain.entity.Wallet;
import org.mephi.finance.manager.domain.repository.WalletRepository;
import org.mephi.finance.manager.exception.ResourceNotFoundException;
import org.mephi.finance.manager.service.WalletSearchService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletSearchServiceImpl implements WalletSearchService {

    private final WalletRepository walletRepository;

    @Override
    public Wallet getUserWallet(UUID userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Не найден кошелек пользователя: " + userId
                ));
    }


    @Override
    public UUID getWalletIdByUserId(UUID userId) {
        return getUserWallet(userId).getId();
    }
}
