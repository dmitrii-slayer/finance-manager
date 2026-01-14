package org.mephi.finance.manager.domain.repository;

import org.mephi.finance.manager.domain.TransactionType;
import org.mephi.finance.manager.domain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Optional<Category> findByNameAndUserIdAndType(String name, UUID userId, TransactionType type);
    List<Category> findByNameAndUserId(String name, UUID userId);
    List<Category> findByUserId(UUID userId);
    List<Category> findByUserIdAndType(UUID userId, TransactionType type);
}
