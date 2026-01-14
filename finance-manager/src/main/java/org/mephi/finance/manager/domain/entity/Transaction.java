package org.mephi.finance.manager.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.mephi.finance.manager.domain.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "transactions_wallet_id_timestamp_idx", columnList = "wallet_id, timestamp DESC"),
        @Index(name = "transactions_category_id_idx", columnList = "category_id"),
})
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column
    private UUID id;

    @Column(nullable = false)
    private UUID walletId;

    @Column
    private UUID categoryId;

    @Column
    private BigDecimal amount;

    @Column
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Column(nullable = false)
    private Instant timestamp;
}
