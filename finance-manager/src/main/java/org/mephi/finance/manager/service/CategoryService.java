package org.mephi.finance.manager.service;

import org.mephi.finance.manager.domain.TransactionType;
import org.mephi.finance.manager.domain.entity.Category;
import org.mephi.finance.manager.dto.CreateCategoryDto;
import org.mephi.finance.manager.dto.UpdateBudgetDto;
import org.mephi.finance.manager.dto.UpdateCategoryDto;

import java.util.List;
import java.util.UUID;

public interface CategoryService {

    Category createCategory(CreateCategoryDto createCategoryDto);
    void save(Category category);
    List<Category> getUserCategories(UUID userId);
    List<Category> getUserCategoriesByType(UUID userId, TransactionType type);
    Category getCategoryById(UUID categoryId);
    Category updateCategory(UUID categoryId, UpdateCategoryDto updateDto);
    void deleteCategory(UUID categoryId, UUID userId);
    Category setBudget(UpdateBudgetDto updateDto);
}
