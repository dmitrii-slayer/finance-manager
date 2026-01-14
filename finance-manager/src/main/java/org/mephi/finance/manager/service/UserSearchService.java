package org.mephi.finance.manager.service;

import org.mephi.finance.manager.domain.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserSearchService {

    Optional<User> findByUserId(UUID userId);
    Optional<User> findByUsername(String username);
}
