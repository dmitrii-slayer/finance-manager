package org.mephi.finance.manager.service;

import org.mephi.finance.manager.domain.entity.User;
import org.mephi.finance.manager.dto.CreateUserDto;
import org.mephi.finance.manager.dto.LoginUserDto;

public interface AuthService {

    User register(CreateUserDto createUserDto);
    User login(LoginUserDto loginUserDto);
}
