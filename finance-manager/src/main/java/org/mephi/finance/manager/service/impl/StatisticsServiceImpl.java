package org.mephi.finance.manager.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mephi.finance.manager.domain.Statistics;
import org.mephi.finance.manager.domain.TransactionType;
import org.mephi.finance.manager.domain.entity.Wallet;
import org.mephi.finance.manager.domain.repository.TransactionRepository;
import org.mephi.finance.manager.service.StatisticsService;
import org.mephi.finance.manager.service.WalletService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final TransactionRepository transactionRepository;
    private final WalletService walletService;

    @Override
    @Transactional(readOnly = true)
    public Statistics getOverviewStatistics(UUID userId) {
        log.info("Получение общей статистики для пользователя: {}", userId);

        Wallet userWallet = walletService.getUserWallet(userId);

        BigDecimal totalIncome = getTotalAmountByType(userWallet.getId(), TransactionType.INCOME);
        BigDecimal totalExpenses = getTotalAmountByType(userWallet.getId(), TransactionType.EXPENSE);

        BigDecimal balance = userWallet.getBalance();

        Map<String, Double> incomeByCategory = getAmountByCategory(userWallet.getId(), TransactionType.INCOME);

        Map<String, Double> expensesByCategory = getAmountByCategory(userWallet.getId(), TransactionType.EXPENSE);

        return buildStatistics(userId, totalIncome, totalExpenses, balance,
                incomeByCategory, expensesByCategory);
    }

    private BigDecimal getTotalAmountByType(UUID walletId, TransactionType type) {
        BigDecimal total = transactionRepository.sumAmountByWalletIdAndType(walletId, type);
        return total != null ? total : BigDecimal.ZERO;
    }

    private Map<String, Double> getAmountByCategory(UUID walletId, TransactionType type) {
        Map<String, Double> result = new HashMap<>();

        List<Object[]> categorySums = transactionRepository.sumAmountByCategoryForUserWallet(walletId, type);

        for (Object[] row : categorySums) {
            String categoryName = (String) row[0];
            BigDecimal totalAmount = (BigDecimal) row[1];

            if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
                result.put(categoryName, totalAmount.doubleValue());
            }
        }

        return result;
    }

    private Statistics buildStatistics(UUID userId,
                                       BigDecimal totalIncome,
                                       BigDecimal totalExpenses,
                                       BigDecimal balance,
                                       Map<String, Double> incomeByCategory,
                                       Map<String, Double> expensesByCategory) {

        log.debug("Создание статистики для пользователя: {}, доход: {}, расходы: {}, баланс: {}",
                userId, totalIncome, totalExpenses, balance);

        return Statistics.builder()
                .userId(userId)
                .totalIncome(totalIncome.doubleValue())
                .totalExpenses(totalExpenses.doubleValue())
                .balance(balance.doubleValue())
                .incomeByCategory(incomeByCategory)
                .expensesByCategory(expensesByCategory)
                .build();
    }
}
