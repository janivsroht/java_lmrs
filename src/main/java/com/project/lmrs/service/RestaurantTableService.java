package com.project.lmrs.service;

import com.project.lmrs.dto.request.CreateTableRequest;
import com.project.lmrs.entity.RestaurantTable;
import com.project.lmrs.entity.Tenant;
import com.project.lmrs.exception.BusinessRuleException;
import com.project.lmrs.exception.ResourceNotFoundException;
import com.project.lmrs.repository.RestaurantTableRepository;
import com.project.lmrs.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantTableService {

    private final RestaurantTableRepository restaurantTableRepository;
    private final TenantRepository tenantRepository;

    public List<RestaurantTable> getAllTables(String tenantId) {
        return restaurantTableRepository.findAllByTenant_TenantIdAndIsDeletedFalse(tenantId);
    }

    public List<RestaurantTable> getAvailableTables(String tenantId) {
        return restaurantTableRepository.findAllByTenant_TenantIdAndStatusAndIsDeletedFalse(tenantId, "AVAILABLE");
    }

    public RestaurantTable getTableById(String tableId, String tenantId) {
        return restaurantTableRepository.findByTableIdAndTenant_TenantIdAndIsDeletedFalse(tableId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("RestaurantTable", "id", tableId));
    }

    @Transactional
    public RestaurantTable createTable(String tenantId, CreateTableRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));

        RestaurantTable table = RestaurantTable.builder()
                .tenant(tenant)
                .tableNumber(request.getTableNumber())
                .zone(request.getZone())
                .capacity(request.getCapacity())
                .status(request.getStatus() != null ? request.getStatus() : "AVAILABLE")
                .positionX(request.getPositionX())
                .positionY(request.getPositionY())
                .isDeleted(false)
                .build();

        return restaurantTableRepository.save(table);
    }

    @Transactional
    public RestaurantTable updateTable(String tableId, CreateTableRequest request, String tenantId) {
        RestaurantTable table = restaurantTableRepository.findByTableIdAndTenant_TenantIdAndIsDeletedFalse(tableId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("RestaurantTable", "id", tableId));

        table.setTableNumber(request.getTableNumber());
        table.setZone(request.getZone());
        table.setCapacity(request.getCapacity());
        table.setStatus(request.getStatus());
        table.setPositionX(request.getPositionX());
        table.setPositionY(request.getPositionY());

        return restaurantTableRepository.save(table);
    }

    @Transactional
    public void deleteTable(String tableId, String tenantId) {
        RestaurantTable table = restaurantTableRepository.findByTableIdAndTenant_TenantIdAndIsDeletedFalse(tableId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("RestaurantTable", "id", tableId));
        table.setDeleted(true);
        restaurantTableRepository.save(table);
    }
}
