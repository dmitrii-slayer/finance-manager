package org.mephi.finance.manager.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mephi.finance.manager.domain.entity.User;
import org.mephi.finance.manager.domain.repository.UserRepository;
import org.mephi.finance.manager.dto.CreateUserDto;
import org.mephi.finance.manager.dto.LoginUserDto;
import org.mephi.finance.manager.service.AuthService;
import org.mephi.finance.manager.service.WalletService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final WalletService walletService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User register(CreateUserDto createUserDto) {
        log.info("Регистрация пользователя: {}", createUserDto.getUsername());

        if (userRepository.existsByUsername(createUserDto.getUsername())) {
            throw new IllegalArgumentException("Пользователь с таким username уже существует");
        }

        if (createUserDto.getPassword() == null || createUserDto.getPassword().length() < 6) {
            throw new IllegalArgumentException("Пароль должен содержать не менее 6 символов");
        }

        User user = User.builder()
                .username(createUserDto.getUsername())
                .password(passwordEncoder.encode(createUserDto.getPassword()))
                .build();

        // сначала сохраняем пользователя чтобы получить id
        User savedUser = userRepository.save(user);
        walletService.createWalletForUser(user.getId());

        log.info("Пользователь успешно зарегистрирован: {}", savedUser.getUsername());
        return savedUser;
    }

    @Override
    public User login(LoginUserDto loginUserDto) {
        log.info("Попытка входа пользователя: {}", loginUserDto.getUsername());

        User user = userRepository.findByUsername(loginUserDto.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Не найден пользователь: " + loginUserDto.getUsername()));

        if (!passwordEncoder.matches(loginUserDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Некорректное имя пользователя или пароль");
        }

        log.info("Пользователь успешно вошел: {}", user.getUsername());
        return user;
    }
}
