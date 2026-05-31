package com.project.lmrs.controller;

import com.project.lmrs.dto.response.DashboardSummaryResponse;
import com.project.lmrs.entity.InventoryItem;
import com.project.lmrs.repository.*;
import com.project.lmrs.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final RoomRepository roomRepository;
    private final GuestRepository guestRepository;
    private final OrderItemRepository orderItemRepository;
    private final HousekeepingTaskRepository housekeepingTaskRepository;
    private final InventoryItemRepository inventoryItemRepository;

    @GetMapping("/revenue")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','FINANCE')")
    public ResponseEntity<List<Map<String, Object>>> getRevenueReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        List<Object[]> rows = paymentRepository.paymentsByDateRange(tenantId, from.atStartOfDay(), to.plusDays(1).atStartOfDay());

        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> point = new HashMap<>();
            point.put("date", row[0].toString());
            point.put("amount", row[1]);
            result.add(point);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/occupancy")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getOccupancyReport() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        long totalRooms = roomRepository.countByTenant_TenantIdAndIsDeletedFalse(tenantId);
        List<Object[]> statusGroups = roomRepository.countByStatusGrouped(tenantId);

        Map<String, Long> statusCounts = new HashMap<>();
        long occupied = 0;
        for (Object[] row : statusGroups) {
            String status = row[0].toString();
            long count = (Long) row[1];
            statusCounts.put(status, count);
            if ("OCCUPIED".equals(status)) {
                occupied = count;
            }
        }

        double occupancyRate = totalRooms > 0 ? (double) occupied / totalRooms * 100 : 0;

        Map<String, Object> result = new HashMap<>();
        result.put("totalRooms", totalRooms);
        result.put("occupied", occupied);
        result.put("occupancyRate", Math.round(occupancyRate * 100.0) / 100.0);
        result.put("statusBreakdown", statusCounts);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/bookings-by-channel")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<List<DashboardSummaryResponse.StatusCount>> getBookingsByChannel() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        List<Object[]> rows = reservationRepository.countByChannelGrouped(tenantId);

        List<DashboardSummaryResponse.StatusCount> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(DashboardSummaryResponse.StatusCount.builder()
                    .label(row[0].toString())
                    .count((Long) row[1])
                    .build());
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/top-menu-items")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<List<DashboardSummaryResponse.MenuItemStat>> getTopMenuItems() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        List<Object[]> rows = orderItemRepository.findTopMenuItems(tenantId);

        List<DashboardSummaryResponse.MenuItemStat> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(DashboardSummaryResponse.MenuItemStat.builder()
                    .name((String) row[0])
                    .totalOrdered((Long) row[1])
                    .build());
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/housekeeping-summary")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<List<DashboardSummaryResponse.StatusCount>> getHousekeepingSummary() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        List<Object[]> rows = housekeepingTaskRepository.countByStatusGrouped(tenantId);

        List<DashboardSummaryResponse.StatusCount> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(DashboardSummaryResponse.StatusCount.builder()
                    .label(row[0].toString())
                    .count((Long) row[1])
                    .build());
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/export/revenue")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','FINANCE')")
    public ResponseEntity<byte[]> exportRevenueCsv(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        List<Object[]> rows = paymentRepository.paymentsByDateRange(tenantId, from.atStartOfDay(), to.plusDays(1).atStartOfDay());

        StringBuilder csv = new StringBuilder("Date,Revenue\n");
        for (Object[] row : rows) {
            csv.append(row[0]).append(",").append(row[1]).append("\n");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=revenue-report-" + from + "-to-" + to + ".csv");

        return ResponseEntity.ok().headers(headers).body(csv.toString().getBytes());
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','FINANCE')")
    public ResponseEntity<List<DashboardSummaryResponse.LowStockItem>> getLowStockReport() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        List<InventoryItem> items = inventoryItemRepository.findLowStockItems(tenantId);

        List<DashboardSummaryResponse.LowStockItem> result = new ArrayList<>();
        for (InventoryItem item : items) {
            result.add(DashboardSummaryResponse.LowStockItem.builder()
                    .name(item.getName())
                    .unit(item.getUnit())
                    .currentStock(item.getCurrentStock())
                    .reorderThreshold(item.getReorderThreshold())
                    .build());
        }
        return ResponseEntity.ok(result);
    }
}
