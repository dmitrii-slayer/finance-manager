package org.mephi.finance.manager.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Statistics {

    private UUID userId;

    private Double totalIncome;

    private Double totalExpenses;

    private Double balance;

    @Builder.Default
    private Map<String, Double> incomeByCategory = new HashMap<>();

    @Builder.Default
    private Map<String, Double> expensesByCategory = new HashMap<>();
}
