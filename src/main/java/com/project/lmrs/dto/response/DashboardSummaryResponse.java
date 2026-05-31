package com.project.lmrs.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DashboardSummaryResponse {
    private KpiData kpis;
    private List<StatusCount> reservationStatuses;
    private List<StatusCount> bookingChannels;
    private List<RevenuePoint> revenueTrend;
    private List<StatusCount> roomStatuses;
    private List<StatusCount> roomsByFloor;
    private List<StatusCount> roomsByType;
    private List<StatusCount> housekeepingStatuses;
    private List<MenuItemStat> topMenuItems;
    private List<LowStockItem> lowStockItems;
    private long totalTasksToday;
    private long completedTasksToday;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class KpiData {
        private long totalGuests;
        private long totalRooms;
        private long activeReservations;
        private BigDecimal totalRevenue;
        private double occupancyRate;
        private long pendingHousekeeping;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class StatusCount {
        private String label;
        private long count;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class RevenuePoint {
        private String date;
        private BigDecimal amount;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class MenuItemStat {
        private String name;
        private long totalOrdered;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class LowStockItem {
        private String name;
        private String unit;
        private BigDecimal currentStock;
        private BigDecimal reorderThreshold;
    }
}
