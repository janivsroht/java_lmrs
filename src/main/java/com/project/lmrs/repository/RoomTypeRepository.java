package com.project.lmrs.repository;

import com.project.lmrs.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RoomTypeRepository extends JpaRepository<RoomType, String> {
    List<RoomType> findAllByTenant_TenantIdAndIsDeletedFalse(String tenantId);
}