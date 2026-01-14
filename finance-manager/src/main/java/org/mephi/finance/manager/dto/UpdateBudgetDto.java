package org.mephi.finance.manager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.mephi.finance.manager.domain.BudgetPeriod;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UpdateBudgetDto extends CurrentUserAwareDto {

    private UUID categoryId;
    private BigDecimal limitAmount;
    private BudgetPeriod period;
}
