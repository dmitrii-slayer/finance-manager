package org.mephi.finance.manager.domain.repository;

import org.mephi.finance.manager.domain.TransactionType;
import org.mephi.finance.manager.domain.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByWalletId(UUID walletId);
    List<Transaction> findByWalletIdAndCategoryIdIn(UUID walletId, Iterable<UUID> categoryIds);

    // сумма транзакций по типу для пользователя
    @Query("""
            SELECT COALESCE(SUM(t.amount), 0)
            FROM Transaction t
            WHERE t.walletId = :walletId AND t.type = :type
            """)
    BigDecimal sumAmountByWalletIdAndType(@Param("walletId") UUID walletId,
                                          @Param("type") TransactionType type);

    // сумма транзакций по категориям для пользователя
    @Query("""
            SELECT c.name, COALESCE(SUM(t.amount), 0)
            FROM Transaction t
            JOIN Category c ON c.id = t.categoryId
            WHERE t.walletId = :walletId AND t.type = :type
            GROUP BY c.name
            """)
    List<Object[]> sumAmountByCategoryForUserWallet(@Param("walletId") UUID walletId,
                                                    @Param("type") TransactionType type);
}
