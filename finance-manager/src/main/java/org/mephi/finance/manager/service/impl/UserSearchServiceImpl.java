package org.mephi.finance.manager.service.impl;

import lombok.RequiredArgsConstructor;
import org.mephi.finance.manager.domain.entity.User;
import org.mephi.finance.manager.domain.repository.UserRepository;
import org.mephi.finance.manager.service.UserSearchService;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserSearchServiceImpl implements UserSearchService {

    private final UserRepository userRepository;

    @Override
    public Optional<User> findByUserId(UUID userId) {
        return userRepository.findById(userId);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
