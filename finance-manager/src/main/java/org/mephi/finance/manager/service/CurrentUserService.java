package org.mephi.finance.manager.service;

import org.mephi.finance.manager.domain.entity.User;

import java.util.UUID;

public interface CurrentUserService {

    UUID getCurrentUserId();
    String getCurrentUsername();
    User getCurrentUser();
}
