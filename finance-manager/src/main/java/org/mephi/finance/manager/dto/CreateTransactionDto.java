package org.mephi.finance.manager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.mephi.finance.manager.domain.TransactionType;

import java.math.BigDecimal;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CreateTransactionDto extends CurrentUserAwareDto {

    private String categoryName;
    private BigDecimal amount;
    private String description;
    private TransactionType type;
    private String walletId;
}
