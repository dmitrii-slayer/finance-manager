package org.mephi.finance.manager.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.mephi.finance.manager.domain.BudgetPeriod;
import org.mephi.finance.manager.domain.TransactionType;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "categories",
        uniqueConstraints = @UniqueConstraint(
                name = "categories_user_id_name_type_key",
                columnNames = {"user_id", "name", "type"}))
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Column(name = "budget_limit", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal budgetLimit = BigDecimal.ZERO;

    @Column(name = "budget_spent", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal budgetSpent = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "budget_period", length = 10)
    private BudgetPeriod budgetPeriod;

    @PrePersist
    @PreUpdate
    public void validate() {
        if (type == TransactionType.INCOME && hasBudget()) {
            throw new IllegalStateException(
                    "Категории дохода не могут иметь бюджет: " + name);
        }
    }

    public boolean hasBudget() {
        return budgetLimit != null && budgetLimit.compareTo(BigDecimal.ZERO) > 0;
    }

    public BigDecimal getRemainingBudget() {
        return hasBudget() ? budgetLimit.subtract(budgetSpent) : null;
    }

    public boolean isBudgetExceeded() {
        return hasBudget() && budgetSpent.compareTo(budgetLimit) > 0;
    }

    public void addToSpentAmount(BigDecimal amount) {
        if (hasBudget()) {
            this.budgetSpent = this.budgetSpent.add(amount);
        }
    }

    public void setBudget(BigDecimal limit, BudgetPeriod period) {
        if (type == TransactionType.INCOME) {
            throw new IllegalStateException("Невозможно установить бюджет для категории доходов: " + name);
        }
        this.budgetLimit = limit;
        this.budgetPeriod = period != null ? period : BudgetPeriod.MONTHLY;
        this.budgetSpent = BigDecimal.ZERO;
    }
}
