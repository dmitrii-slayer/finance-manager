package org.mephi.finance.manager.rest.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mephi.finance.manager.api.CategoryApi;
import org.mephi.finance.manager.domain.TransactionType;
import org.mephi.finance.manager.domain.entity.Category;
import org.mephi.finance.manager.dto.CreateCategoryDto;
import org.mephi.finance.manager.dto.UpdateBudgetDto;
import org.mephi.finance.manager.dto.UpdateCategoryDto;
import org.mephi.finance.manager.mapper.CategoryMapper;
import org.mephi.finance.manager.model.CategoryDetailedResponse;
import org.mephi.finance.manager.model.CategoryResponse;
import org.mephi.finance.manager.model.CreateCategoryRequest;
import org.mephi.finance.manager.model.TransactionTypeEnum;
import org.mephi.finance.manager.model.UpdateBudgetRequest;
import org.mephi.finance.manager.model.UpdateCategoryRequest;
import org.mephi.finance.manager.service.CategoryService;
import org.mephi.finance.manager.service.CurrentUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CategoryController implements CategoryApi {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;
    private final CurrentUserService currentUserService;

    @Override
    public ResponseEntity<CategoryDetailedResponse> createCategory(CreateCategoryRequest categoryRequest) {
        log.info("Добавление категории: {}", categoryRequest.getName());

        UUID userId = currentUserService.getCurrentUserId();
        CreateCategoryDto createCategoryDto = categoryMapper.toLocalDto(categoryRequest, userId);

        Category category = categoryService.createCategory(createCategoryDto);

        CategoryDetailedResponse response = categoryMapper.toApiDetailedResponse(category);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<CategoryResponse>> getUserCategories(TransactionTypeEnum type) {
        UUID currentUserId = currentUserService.getCurrentUserId();
        log.info("Получение категорий пользователя {} - тип фильтра: {}", currentUserId, type);

        List<Category> categories;

        if (type != null) {
            TransactionType categoryType = TransactionType.valueOf(type.name());
            categories = categoryService.getUserCategoriesByType(currentUserId, categoryType);
        } else {
            categories = categoryService.getUserCategories(currentUserId);
        }

        List<CategoryResponse> responses = categories.stream()
                .map(categoryMapper::toApiResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @Override
    public ResponseEntity<CategoryDetailedResponse> getCategoryById(UUID categoryId) {
        log.info("Получение категории по ID: {}", categoryId);

        Category category = categoryService.getCategoryById(categoryId);
        CategoryDetailedResponse response = categoryMapper.toApiDetailedResponse(category);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<CategoryDetailedResponse> updateCategory(UUID categoryId, UpdateCategoryRequest categoryRequest) {
        log.info("Обновление категории: {}", categoryId);

        UUID userId = currentUserService.getCurrentUserId();
        UpdateCategoryDto updateDto = categoryMapper.toLocalDto(categoryRequest, userId);

        Category category = categoryService.updateCategory(categoryId, updateDto);

        CategoryDetailedResponse response = categoryMapper.toApiDetailedResponse(category);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> deleteCategory(UUID categoryId) {
        log.info("Удаление категории: {}", categoryId);

        UUID userId = currentUserService.getCurrentUserId();

        categoryService.deleteCategory(categoryId, userId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<CategoryDetailedResponse> setCategoryBudget(UUID categoryId, UpdateBudgetRequest updateBudgetRequest) {
        log.info("Установка бюджета для категории: {}", categoryId);

        UUID userId = currentUserService.getCurrentUserId();
        UpdateBudgetDto updateBudgetDto = categoryMapper.toLocalDto(updateBudgetRequest, categoryId, userId);
        Category category = categoryService.setBudget(updateBudgetDto);
        CategoryDetailedResponse response = categoryMapper.toApiDetailedResponse(category);

        return ResponseEntity.ok(response);
    }
}
