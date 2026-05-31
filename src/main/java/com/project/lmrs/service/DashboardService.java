package com.project.lmrs.service;

import com.project.lmrs.dto.response.DashboardSummaryResponse;
import com.project.lmrs.enums.ReservationStatus;
import com.project.lmrs.enums.RoomStatus;
import com.project.lmrs.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final GuestRepository guestRepository;
    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final OrderItemRepository orderItemRepository;
    private final HousekeepingTaskRepository housekeepingTaskRepository;
    private final InventoryItemRepository inventoryItemRepository;

    public DashboardSummaryResponse getSummary(String tenantId) {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(30);

        long totalGuests = guestRepository.countByTenant_TenantIdAndIsDeletedFalse(tenantId);
        long totalRooms = roomRepository.countByTenant_TenantIdAndIsDeletedFalse(tenantId);
        long activeReservations = reservationRepository
                .countByTenant_TenantIdAndStatusAndIsDeletedFalse(tenantId, ReservationStatus.CHECKED_IN);
        BigDecimal totalRevenue = paymentRepository.sumCompletedPaymentsByTenant(tenantId);
        long occupiedRooms = roomRepository.countByTenant_TenantIdAndStatusAndIsDeletedFalse(tenantId, RoomStatus.OCCUPIED);
        double occupancyRate = totalRooms > 0 ? (occupiedRooms * 100.0 / totalRooms) : 0;

        long pendingHousekeeping = 0;
        List<Object[]> hkStatuses = housekeepingTaskRepository.countByStatusGrouped(tenantId);
        for (Object[] row : hkStatuses) {
            if ("PENDING".equals(row[0])) {
                pendingHousekeeping = ((Number) row[1]).longValue();
            }
        }

        DashboardSummaryResponse.KpiData kpis = DashboardSummaryResponse.KpiData.builder()
                .totalGuests(totalGuests)
                .totalRooms(totalRooms)
                .activeReservations(activeReservations)
                .totalRevenue(totalRevenue)
                .occupancyRate(occupancyRate)
                .pendingHousekeeping(pendingHousekeeping)
                .build();

        List<DashboardSummaryResponse.StatusCount> reservationStatuses = reservationRepository
                .countByStatusGrouped(tenantId)
                .stream()
                .map(row -> DashboardSummaryResponse.StatusCount.builder()
                        .label(row[0].toString())
                        .count(((Number) row[1]).longValue())
                        .build())
                .collect(Collectors.toList());

        List<DashboardSummaryResponse.StatusCount> bookingChannels = reservationRepository
                .countByChannelGrouped(tenantId)
                .stream()
                .map(row -> DashboardSummaryResponse.StatusCount.builder()
                        .label(row[0].toString())
                        .count(((Number) row[1]).longValue())
                        .build())
                .collect(Collectors.toList());

        List<DashboardSummaryResponse.RevenuePoint> revenueTrend = paymentRepository
                .paymentsByDateRange(tenantId, thirtyDaysAgo.atStartOfDay(), today.plusDays(1).atStartOfDay())
                .stream()
                .map(row -> DashboardSummaryResponse.RevenuePoint.builder()
                        .date(row[0].toString())
                        .amount((BigDecimal) row[1])
                        .build())
                .collect(Collectors.toList());

        List<DashboardSummaryResponse.StatusCount> roomStatuses = roomRepository
                .countByStatusGrouped(tenantId)
                .stream()
                .map(row -> DashboardSummaryResponse.StatusCount.builder()
                        .label(row[0].toString())
                        .count(((Number) row[1]).longValue())
                        .build())
                .collect(Collectors.toList());

        List<DashboardSummaryResponse.StatusCount> roomsByFloor = roomRepository
                .countByFloorGrouped(tenantId)
                .stream()
                .map(row -> DashboardSummaryResponse.StatusCount.builder()
                        .label("Floor " + row[0])
                        .count(((Number) row[1]).longValue())
                        .build())
                .collect(Collectors.toList());

        List<DashboardSummaryResponse.StatusCount> roomsByType = roomRepository
                .countByRoomTypeGrouped(tenantId)
                .stream()
                .map(row -> DashboardSummaryResponse.StatusCount.builder()
                        .label((String) row[0])
                        .count(((Number) row[1]).longValue())
                        .build())
                .collect(Collectors.toList());

        List<DashboardSummaryResponse.StatusCount> housekeepingStatuses = hkStatuses
                .stream()
                .map(row -> DashboardSummaryResponse.StatusCount.builder()
                        .label((String) row[0])
                        .count(((Number) row[1]).longValue())
                        .build())
                .collect(Collectors.toList());

        List<DashboardSummaryResponse.MenuItemStat> topMenuItems = orderItemRepository
                .findTopMenuItems(tenantId)
                .stream()
                .map(row -> DashboardSummaryResponse.MenuItemStat.builder()
                        .name((String) row[0])
                        .totalOrdered(((Number) row[1]).longValue())
                        .build())
                .collect(Collectors.toList());

        List<DashboardSummaryResponse.LowStockItem> lowStockItems = inventoryItemRepository
                .findLowStockItems(tenantId)
                .stream()
                .map(item -> DashboardSummaryResponse.LowStockItem.builder()
                        .name(item.getName())
                        .unit(item.getUnit())
                        .currentStock(item.getCurrentStock())
                        .reorderThreshold(item.getReorderThreshold())
                        .build())
                .collect(Collectors.toList());

        long totalTasksToday = housekeepingTaskRepository
                .countByTenant_TenantIdAndScheduledDateAndIsDeletedFalse(tenantId, today);
        long completedTasksToday = housekeepingTaskRepository
                .countCompletedByDate(tenantId, today);

        return DashboardSummaryResponse.builder()
                .kpis(kpis)
                .reservationStatuses(reservationStatuses)
                .bookingChannels(bookingChannels)
                .revenueTrend(revenueTrend)
                .roomStatuses(roomStatuses)
                .roomsByFloor(roomsByFloor)
                .roomsByType(roomsByType)
                .housekeepingStatuses(housekeepingStatuses)
                .topMenuItems(topMenuItems)
                .lowStockItems(lowStockItems)
                .totalTasksToday(totalTasksToday)
                .completedTasksToday(completedTasksToday)
                .build();
    }
}
