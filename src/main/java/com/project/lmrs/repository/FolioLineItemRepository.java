package com.project.lmrs.repository;

import com.project.lmrs.entity.FolioLineItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FolioLineItemRepository extends JpaRepository<FolioLineItem, String> {
    List<FolioLineItem> findAllByFolio_FolioIdAndIsDeletedFalse(String folioId);
}