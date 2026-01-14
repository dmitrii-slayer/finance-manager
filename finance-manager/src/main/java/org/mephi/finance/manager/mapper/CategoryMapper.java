package org.mephi.finance.manager.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mephi.finance.manager.domain.entity.Category;
import org.mephi.finance.manager.dto.CreateCategoryDto;
import org.mephi.finance.manager.dto.UpdateBudgetDto;
import org.mephi.finance.manager.dto.UpdateCategoryDto;
import org.mephi.finance.manager.model.CategoryDetailedResponse;
import org.mephi.finance.manager.model.CategoryResponse;
import org.mephi.finance.manager.model.CreateCategoryRequest;
import org.mephi.finance.manager.model.UpdateBudgetRequest;
import org.mephi.finance.manager.model.UpdateCategoryRequest;

import java.util.UUID;

@Mapper
public interface CategoryMapper {

    CreateCategoryDto toLocalDto(CreateCategoryRequest request, UUID userId);

    UpdateCategoryDto toLocalDto(UpdateCategoryRequest request, UUID userId);

    UpdateBudgetDto toLocalDto(UpdateBudgetRequest request, UUID categoryId, UUID userId);

    @Mapping(target = "isBudgetExceeded", expression = "java(category.isBudgetExceeded())")
    CategoryResponse toApiResponse(Category category);

    @Mapping(target = "isBudgetExceeded", expression = "java(category.isBudgetExceeded())")
    CategoryDetailedResponse toApiDetailedResponse(Category category);
}
