package com.project.lmrs.controller;

import com.project.lmrs.dto.request.AddVariantRequest;
import com.project.lmrs.dto.request.CreateCategoryRequest;
import com.project.lmrs.dto.request.CreateMenuItemRequest;
import com.project.lmrs.dto.response.CategoryResponse;
import com.project.lmrs.dto.response.MenuItemResponse;
import com.project.lmrs.dto.response.MenuItemVariantResponse;
import com.project.lmrs.entity.MenuCategory;
import com.project.lmrs.entity.MenuItemVariant;
import com.project.lmrs.security.SecurityUtils;
import com.project.lmrs.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/categories")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','SERVER','KITCHEN')")
    public ResponseEntity<List<CategoryResponse>> getCategories() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(menuService.getAllCategories(tenantId).stream().map(this::toCategoryResponse).toList());
    }

    @PostMapping("/categories")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.status(HttpStatus.CREATED).body(toCategoryResponse(menuService.createCategory(tenantId, request)));
    }

    @PutMapping("/categories/{categoryId}")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable String categoryId,
                                                       @Valid @RequestBody CreateCategoryRequest request) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(toCategoryResponse(menuService.updateCategory(categoryId, request, tenantId)));
    }

    @DeleteMapping("/categories/{categoryId}")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable String categoryId) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        menuService.deleteCategory(categoryId, tenantId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/items")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','SERVER','KITCHEN')")
    public ResponseEntity<List<MenuItemResponse>> getMenuItems() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(menuService.getAllMenuItems(tenantId));
    }

    @GetMapping("/items/{itemId}")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','SERVER','KITCHEN')")
    public ResponseEntity<MenuItemResponse> getMenuItemById(@PathVariable String itemId) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(menuService.getMenuItemById(itemId, tenantId));
    }

    @PostMapping("/items")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<MenuItemResponse> createMenuItem(@Valid @RequestBody CreateMenuItemRequest request) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.status(HttpStatus.CREATED).body(menuService.createMenuItem(tenantId, request));
    }

    @PutMapping("/items/{itemId}")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<MenuItemResponse> updateMenuItem(@PathVariable String itemId,
                                                           @Valid @RequestBody CreateMenuItemRequest request) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(menuService.updateMenuItem(itemId, request, tenantId));
    }

    @DeleteMapping("/items/{itemId}")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable String itemId) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        menuService.deleteMenuItem(itemId, tenantId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/items/{itemId}/variants")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<MenuItemVariantResponse> addVariant(@PathVariable String itemId,
                                                       @Valid @RequestBody AddVariantRequest request) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.status(HttpStatus.CREATED).body(toVariantResponse(menuService.addVariant(itemId, request, tenantId)));
    }

    @DeleteMapping("/variants/{variantId}")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Void> deleteVariant(@PathVariable String variantId) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        menuService.deleteVariant(variantId, tenantId);
        return ResponseEntity.noContent().build();
    }

    private CategoryResponse toCategoryResponse(MenuCategory c) {
        return CategoryResponse.builder()
                .categoryId(c.getCategoryId())
                .name(c.getName())
                .displayOrder(c.getDisplayOrder())
                .isActive(c.isActive())
                .build();
    }

    private MenuItemVariantResponse toVariantResponse(MenuItemVariant v) {
        return MenuItemVariantResponse.builder()
                .variantId(v.getVariantId())
                .name(v.getName())
                .priceModifier(v.getPriceModifier())
                .build();
    }
}
