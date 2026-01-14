package org.mephi.finance.manager.rest.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mephi.finance.manager.api.AuthenticationApi;
import org.mephi.finance.manager.domain.entity.User;
import org.mephi.finance.manager.dto.CreateUserDto;
import org.mephi.finance.manager.dto.LoginUserDto;
import org.mephi.finance.manager.mapper.AuthMapper;
import org.mephi.finance.manager.model.AuthResponse;
import org.mephi.finance.manager.model.LoginRequest;
import org.mephi.finance.manager.model.RegisterRequest;
import org.mephi.finance.manager.security.JwtTokenProvider;
import org.mephi.finance.manager.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController implements AuthenticationApi {

    private final AuthService authService;
    private final AuthMapper authMapper;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public ResponseEntity<AuthResponse> register(RegisterRequest registerRequest) {
        log.info("Регистрация пользователя: {}", registerRequest.getUsername());

        CreateUserDto createUserDto = authMapper.toLocalDto(registerRequest);

        User user = authService.register(createUserDto);

        String token = jwtTokenProvider.generateToken(user.getUsername());

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setUsername(user.getUsername());
        response.setMessage("Регистрация успешна");

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<AuthResponse> login(LoginRequest loginRequest) {
        log.info("Попытка входа пользователя: {}", loginRequest.getUsername());

        LoginUserDto createUserDto = authMapper.toLocalDto(loginRequest);

        User user = authService.login(createUserDto);

        String token = jwtTokenProvider.generateToken(user.getUsername());

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setUsername(user.getUsername());
        response.setMessage("Вход успешен");

        return ResponseEntity.ok(response);
    }
}
