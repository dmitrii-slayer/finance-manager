package org.mephi.finance.manager.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mephi.finance.manager.domain.TransactionType;
import org.mephi.finance.manager.domain.entity.Category;
import org.mephi.finance.manager.domain.repository.CategoryRepository;
import org.mephi.finance.manager.dto.CreateCategoryDto;
import org.mephi.finance.manager.dto.UpdateBudgetDto;
import org.mephi.finance.manager.dto.UpdateCategoryDto;
import org.mephi.finance.manager.exception.ActionForbiddenException;
import org.mephi.finance.manager.exception.ResourceNotFoundException;
import org.mephi.finance.manager.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public Category createCategory(CreateCategoryDto createCategoryDto) {
        UUID userId = createCategoryDto.getUserId();

        categoryRepository.findByNameAndUserIdAndType(
                createCategoryDto.getName(),
                userId,
                createCategoryDto.getType()
        ).ifPresent(existingCategory -> {
            throw new IllegalArgumentException(
                    "Категория с названием: " + createCategoryDto.getName() +
                            " и типом: " + createCategoryDto.getType() + " уже существует"
            );
        });

        Category category = Category.builder()
                .userId(userId)
                .name(createCategoryDto.getName())
                .type(createCategoryDto.getType())
                .build();

        Category savedCategory = categoryRepository.save(category);

        log.info("Категория создана: {} (тип: {})", createCategoryDto.getName(), createCategoryDto.getType());

        return savedCategory;
    }

    @Override
    @Transactional
    public void save(Category category) {
        categoryRepository.save(category);
    }

    @Override
    public Category getCategoryById(UUID categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Не найдена категория " + categoryId
                ));
    }

    @Override
    public List<Category> getUserCategories(UUID userId) {
        return categoryRepository.findByUserId(userId);
    }

    @Override
    public List<Category> getUserCategoriesByType(UUID userId, TransactionType type) {
        return categoryRepository.findByUserIdAndType(userId, type);
    }

    @Override
    @Transactional
    public Category updateCategory(UUID categoryId, UpdateCategoryDto updateDto) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Не найдена категория " + categoryId
                ));

        checkUserCanModifyCategory(updateDto.getUserId(), category);

        if (!category.getName().equals(updateDto.getName())) {
            categoryRepository.findByNameAndUserIdAndType(
                    updateDto.getName(),
                    category.getUserId(),
                    category.getType()
            ).ifPresent(existingCategory -> {
                if (!existingCategory.getId().equals(categoryId)) {
                    throw new IllegalArgumentException(
                            "Категория с названием: " + updateDto.getName() +
                                    " и типом: " + category.getType() + " уже существует"
                    );
                }
            });
        }

        category.setName(updateDto.getName());

        Category updatedCategory = categoryRepository.save(category);

        log.info("Категория обновлена: {} (ID: {})", updateDto.getName(), categoryId);

        return updatedCategory;
    }

    @Override
    @Transactional
    public void deleteCategory(UUID categoryId, UUID userId) {
        Category category = getCategoryById(categoryId);

        checkUserCanModifyCategory(userId, category);
        categoryRepository.deleteById(categoryId);

        log.info("Категория удалена: {} (ID: {})", category.getName(), categoryId);
    }

    @Override
    public Optional<Category> findByNameAndUserIdAndType(String categoryName, UUID userId, TransactionType type) {
        return Optional.empty();
    }

    @Override
    @Transactional
    public Category setBudget(UpdateBudgetDto updateDto) {
        Category category = getCategoryById(updateDto.getCategoryId());

        category.setBudget(updateDto.getLimitAmount(), updateDto.getPeriod());
        return categoryRepository.save(category);
    }

    private void checkUserCanModifyCategory(UUID userId, Category category) {
        if (category.getUserId() == null || !Objects.equals(category.getUserId(), userId)) {
            throw new ActionForbiddenException("Категория не принадлежит пользователю");
        }
    }
}
