package com.project.lmrs.service;

import com.project.lmrs.entity.Guest;
import com.project.lmrs.entity.LoyaltyTransaction;
import com.project.lmrs.enums.LoyaltyTier;
import com.project.lmrs.exception.BusinessRuleException;
import com.project.lmrs.exception.ResourceNotFoundException;
import com.project.lmrs.repository.GuestRepository;
import com.project.lmrs.repository.LoyaltyTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LoyaltyService {

    private final LoyaltyTransactionRepository loyaltyTransactionRepository;
    private final GuestRepository guestRepository;

    public Integer getPointsBalance(String guestId, String tenantId) {
        guestRepository.findByGuestIdAndTenant_TenantIdAndIsDeletedFalse(guestId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Guest", "id", guestId));
        Integer total = loyaltyTransactionRepository.sumPointsByGuestId(guestId);
        return total != null ? total : 0;
    }

    public List<LoyaltyTransaction> getTransactionHistory(String guestId, String tenantId) {
        guestRepository.findByGuestIdAndTenant_TenantIdAndIsDeletedFalse(guestId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Guest", "id", guestId));
        return loyaltyTransactionRepository.findAllByGuest_GuestId(guestId);
    }

    @Transactional
    public LoyaltyTransaction earnPoints(String guestId, int points, String referenceId, String referenceType) {
        Guest guest = guestRepository.findById(guestId)
                .filter(g -> !g.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Guest", "id", guestId));

        LoyaltyTransaction transaction = LoyaltyTransaction.builder()
                .guest(guest)
                .transactionType("EARN")
                .points(points)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .build();

        LoyaltyTransaction saved = loyaltyTransactionRepository.save(transaction);

        Integer totalPoints = loyaltyTransactionRepository.sumPointsByGuestId(guestId);
        int total = totalPoints != null ? totalPoints : 0;

        LoyaltyTier newTier;
        if (total >= 10000) newTier = LoyaltyTier.PLATINUM;
        else if (total >= 5000) newTier = LoyaltyTier.GOLD;
        else if (total >= 1000) newTier = LoyaltyTier.SILVER;
        else newTier = LoyaltyTier.BRONZE;

        if (guest.getLoyaltyTier() != newTier) {
            guest.setLoyaltyTier(newTier);
            guestRepository.save(guest);
        }

        return saved;
    }

    @Transactional
    public LoyaltyTransaction redeemPoints(String guestId, int points, String referenceId, String referenceType, String tenantId) {
        Guest guest = guestRepository.findByGuestIdAndTenant_TenantIdAndIsDeletedFalse(guestId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Guest", "id", guestId));

        Integer balance = loyaltyTransactionRepository.sumPointsByGuestId(guestId);
        int currentBalance = balance != null ? balance : 0;

        if (currentBalance < points) {
            throw new BusinessRuleException(
                    String.format("Insufficient points. Available: %d, requested: %d", currentBalance, points));
        }

        LoyaltyTransaction transaction = LoyaltyTransaction.builder()
                .guest(guest)
                .transactionType("REDEEM")
                .points(-points)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .build();

        return loyaltyTransactionRepository.save(transaction);
    }
}
