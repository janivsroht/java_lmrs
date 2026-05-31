package com.project.lmrs.repository;

import com.project.lmrs.entity.MenuItemVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MenuItemVariantRepository extends JpaRepository<MenuItemVariant, String> {
    List<MenuItemVariant> findAllByMenuItem_ItemIdAndIsDeletedFalse(String itemId);
}