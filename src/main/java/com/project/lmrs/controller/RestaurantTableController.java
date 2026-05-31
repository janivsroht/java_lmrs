package com.project.lmrs.controller;

import com.project.lmrs.dto.request.CreateTableRequest;
import com.project.lmrs.dto.response.RestaurantTableResponse;
import com.project.lmrs.entity.RestaurantTable;
import com.project.lmrs.security.SecurityUtils;
import com.project.lmrs.service.RestaurantTableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tables")
@RequiredArgsConstructor
public class RestaurantTableController {

    private final RestaurantTableService restaurantTableService;

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','SERVER')")
    public ResponseEntity<List<RestaurantTableResponse>> getAllTables() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(restaurantTableService.getAllTables(tenantId).stream().map(this::toResponse).toList());
    }

    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','SERVER','FRONT_DESK')")
    public ResponseEntity<List<RestaurantTableResponse>> getAvailableTables() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(restaurantTableService.getAvailableTables(tenantId).stream().map(this::toResponse).toList());
    }

    @GetMapping("/{tableId}")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','SERVER')")
    public ResponseEntity<RestaurantTableResponse> getTableById(@PathVariable String tableId) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(toResponse(restaurantTableService.getTableById(tableId, tenantId)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<RestaurantTableResponse> createTable(@Valid @RequestBody CreateTableRequest request) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(restaurantTableService.createTable(tenantId, request)));
    }

    @PutMapping("/{tableId}")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<RestaurantTableResponse> updateTable(@PathVariable String tableId,
                                                       @Valid @RequestBody CreateTableRequest request) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(toResponse(restaurantTableService.updateTable(tableId, request, tenantId)));
    }

    @DeleteMapping("/{tableId}")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Void> deleteTable(@PathVariable String tableId) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        restaurantTableService.deleteTable(tableId, tenantId);
        return ResponseEntity.noContent().build();
    }

    private RestaurantTableResponse toResponse(RestaurantTable table) {
        return RestaurantTableResponse.builder()
                .tableId(table.getTableId())
                .tableNumber(table.getTableNumber())
                .zone(table.getZone())
                .capacity(table.getCapacity())
                .status(table.getStatus())
                .positionX(table.getPositionX())
                .positionY(table.getPositionY())
                .build();
    }
}
