package com.project.lmrs.service;

import com.project.lmrs.dto.request.AddVariantRequest;
import com.project.lmrs.dto.request.CreateCategoryRequest;
import com.project.lmrs.dto.request.CreateMenuItemRequest;
import com.project.lmrs.dto.response.MenuItemResponse;
import com.project.lmrs.entity.*;
import com.project.lmrs.exception.ResourceNotFoundException;
import com.project.lmrs.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuCategoryRepository menuCategoryRepository;
    private final MenuItemRepository menuItemRepository;
    private final MenuItemVariantRepository menuItemVariantRepository;
    private final TenantRepository tenantRepository;

    public List<MenuCategory> getAllCategories(String tenantId) {
        return menuCategoryRepository.findAllByTenant_TenantIdAndIsDeletedFalseOrderByDisplayOrderAsc(tenantId);
    }

    @Transactional
    public MenuCategory createCategory(String tenantId, CreateCategoryRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));
        MenuCategory category = MenuCategory.builder()
                .tenant(tenant)
                .name(request.getName())
                .displayOrder(request.getDisplayOrder())
                .isActive(request.isActive())
                .isDeleted(false)
                .build();
        return menuCategoryRepository.save(category);
    }

    @Transactional
    public MenuCategory updateCategory(String categoryId, CreateCategoryRequest request, String tenantId) {
        MenuCategory category = menuCategoryRepository.findByCategoryIdAndTenant_TenantIdAndIsDeletedFalse(categoryId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("MenuCategory", "id", categoryId));
        category.setName(request.getName());
        category.setDisplayOrder(request.getDisplayOrder());
        category.setActive(request.isActive());
        return menuCategoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(String categoryId, String tenantId) {
        MenuCategory category = menuCategoryRepository.findByCategoryIdAndTenant_TenantIdAndIsDeletedFalse(categoryId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("MenuCategory", "id", categoryId));
        category.setDeleted(true);
        menuCategoryRepository.save(category);
    }

    public List<MenuItemResponse> getAllMenuItems(String tenantId) {
        return menuItemRepository.findAllByTenant_TenantIdAndIsDeletedFalse(tenantId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public MenuItemResponse getMenuItemById(String itemId, String tenantId) {
        MenuItem item = menuItemRepository.findByItemIdAndTenant_TenantIdAndIsDeletedFalse(itemId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", itemId));
        return toResponse(item);
    }

    @Transactional
    public MenuItemResponse createMenuItem(String tenantId, CreateMenuItemRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));

        MenuCategory category = menuCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("MenuCategory", "id", request.getCategoryId()));

        MenuItem item = MenuItem.builder()
                .tenant(tenant)
                .category(category)
                .name(request.getName())
                .description(request.getDescription())
                .basePrice(request.getBasePrice())
                .allergens(request.getAllergens())
                .dietaryFlags(request.getDietaryFlags())
                .isAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true)
                .isDeleted(false)
                .build();

        item = menuItemRepository.save(item);
        return toResponse(item);
    }

    @Transactional
    public MenuItemResponse updateMenuItem(String itemId, CreateMenuItemRequest request, String tenantId) {
        MenuItem item = menuItemRepository.findByItemIdAndTenant_TenantIdAndIsDeletedFalse(itemId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", itemId));

        MenuCategory category = menuCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("MenuCategory", "id", request.getCategoryId()));

        item.setCategory(category);
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setBasePrice(request.getBasePrice());
        item.setAllergens(request.getAllergens());
        item.setDietaryFlags(request.getDietaryFlags());
        if (request.getIsAvailable() != null) {
            item.setAvailable(request.getIsAvailable());
        }

        item = menuItemRepository.save(item);
        return toResponse(item);
    }

    @Transactional
    public void deleteMenuItem(String itemId, String tenantId) {
        MenuItem item = menuItemRepository.findByItemIdAndTenant_TenantIdAndIsDeletedFalse(itemId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", itemId));
        item.setDeleted(true);
        menuItemRepository.save(item);
    }

    @Transactional
    public MenuItemVariant addVariant(String itemId, AddVariantRequest request, String tenantId) {
        MenuItem item = menuItemRepository.findByItemIdAndTenant_TenantIdAndIsDeletedFalse(itemId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", itemId));
        MenuItemVariant variant = MenuItemVariant.builder()
                .menuItem(item)
                .name(request.getName())
                .priceModifier(request.getPriceModifier())
                .isDeleted(false)
                .build();
        return menuItemVariantRepository.save(variant);
    }

    @Transactional
    public void deleteVariant(String variantId, String tenantId) {
        MenuItemVariant variant = menuItemVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItemVariant", "id", variantId));
        if (!variant.getMenuItem().getTenant().getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("MenuItemVariant", "id", variantId);
        }
        variant.setDeleted(true);
        menuItemVariantRepository.save(variant);
    }

    private MenuItemResponse toResponse(MenuItem item) {
        List<MenuItemResponse.VariantDto> variants = item.getVariants() != null
                ? item.getVariants().stream()
                    .filter(v -> !v.isDeleted())
                    .map(v -> MenuItemResponse.VariantDto.builder()
                            .variantId(v.getVariantId())
                            .name(v.getName())
                            .priceModifier(v.getPriceModifier())
                            .build())
                    .collect(Collectors.toList())
                : List.of();

        return MenuItemResponse.builder()
                .itemId(item.getItemId())
                .categoryId(item.getCategory().getCategoryId())
                .categoryName(item.getCategory().getName())
                .name(item.getName())
                .description(item.getDescription())
                .basePrice(item.getBasePrice())
                .allergens(item.getAllergens())
                .dietaryFlags(item.getDietaryFlags())
                .isAvailable(item.isAvailable())
                .variants(variants)
                .build();
    }
}
