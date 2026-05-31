package com.project.lmrs.repository;

import com.project.lmrs.entity.LoyaltyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, String> {
    List<LoyaltyTransaction> findAllByGuest_GuestId(String guestId);

    @Query("SELECT SUM(lt.points) FROM LoyaltyTransaction lt WHERE lt.guest.guestId = :guestId")
    Integer sumPointsByGuestId(@Param("guestId") String guestId);
}