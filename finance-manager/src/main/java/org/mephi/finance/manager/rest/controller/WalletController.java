package org.mephi.finance.manager.rest.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mephi.finance.manager.api.WalletApi;
import org.mephi.finance.manager.domain.entity.User;
import org.mephi.finance.manager.domain.entity.Wallet;
import org.mephi.finance.manager.dto.TransferDto;
import org.mephi.finance.manager.mapper.WalletMapper;
import org.mephi.finance.manager.model.BalanceResponse;
import org.mephi.finance.manager.model.TransferRequest;
import org.mephi.finance.manager.model.TransferResponse;
import org.mephi.finance.manager.model.WalletResponse;
import org.mephi.finance.manager.service.CurrentUserService;
import org.mephi.finance.manager.service.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class WalletController implements WalletApi {

    private final WalletService walletService;
    private final WalletMapper walletMapper;
    private final CurrentUserService currentUserService;

    @Override
    public ResponseEntity<WalletResponse> getWallet() {
        UUID currentUserId = currentUserService.getCurrentUserId();
        log.info("Получение кошелька пользователя {}", currentUserId);

        Wallet wallet = walletService.getUserWallet(currentUserId);
        WalletResponse response = walletMapper.toApiResponse(wallet);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<BalanceResponse> getBalance() {
        UUID currentUserId = currentUserService.getCurrentUserId();
        log.info("Получение баланса пользователя {}", currentUserId);

        BigDecimal balance = walletService.getUserWallet(currentUserId).getBalance();

        BalanceResponse response = new BalanceResponse();
        response.setBalance(balance.doubleValue());

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<TransferResponse> transferToUser(TransferRequest transferRequest) {
        User currentUser = currentUserService.getCurrentUser();
        log.info("Перевод от пользователя {} пользователю: {}, сумма {}",
                currentUser.getUsername(), transferRequest.getTargetUsername(), transferRequest.getAmount());

        TransferDto transferDto = TransferDto.builder()
                .userId(currentUser.getId())
                .targetUsername(transferRequest.getTargetUsername())
                .amount(BigDecimal.valueOf(transferRequest.getAmount()))
                .description(transferRequest.getDescription())
                .build();

        UUID senderTransactionId = walletService.transferToUser(transferDto);

        TransferResponse response = new TransferResponse();
        response.setTransactionId(senderTransactionId);
        response.setFromUsername(currentUser.getUsername());
        response.setToUsername(transferRequest.getTargetUsername());
        response.setAmount(transferRequest.getAmount());
        response.setDescription(transferRequest.getDescription());
        response.setTimestamp(Instant.now());

        return ResponseEntity.ok(response);
    }
}
