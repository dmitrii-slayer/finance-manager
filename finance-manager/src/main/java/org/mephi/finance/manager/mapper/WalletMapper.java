package org.mephi.finance.manager.mapper;

import org.mapstruct.Mapper;
import org.mephi.finance.manager.domain.entity.Wallet;
import org.mephi.finance.manager.model.WalletResponse;

@Mapper
public interface WalletMapper {

    WalletResponse toApiResponse(Wallet wallet);
}
