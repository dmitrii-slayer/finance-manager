package org.mephi.finance.manager.mapper;

import org.mapstruct.Mapper;
import org.mephi.finance.manager.dto.CreateUserDto;
import org.mephi.finance.manager.dto.LoginUserDto;
import org.mephi.finance.manager.model.LoginRequest;
import org.mephi.finance.manager.model.RegisterRequest;

@Mapper
public interface AuthMapper {

    CreateUserDto toLocalDto(RegisterRequest request);
    LoginUserDto toLocalDto(LoginRequest request);
}
