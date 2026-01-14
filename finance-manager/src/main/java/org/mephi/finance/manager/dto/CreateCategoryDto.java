package org.mephi.finance.manager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.mephi.finance.manager.domain.TransactionType;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CreateCategoryDto extends CurrentUserAwareDto {

    private String name;
    private TransactionType type;
}
