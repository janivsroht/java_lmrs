package com.project.lmrs.repository;

import com.project.lmrs.entity.PartnerApiUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface PartnerApiUsageRepository extends JpaRepository<PartnerApiUsage, String> {

    List<PartnerApiUsage> findAllByPartner_PartnerId(String partnerId);

    @Query("SELECT u FROM PartnerApiUsage u WHERE u.partner.partnerId = :partnerId " +
           "AND u.createdAt BETWEEN :from AND :to")
    List<PartnerApiUsage> findByPartnerAndDateRange(
        @Param("partnerId") String partnerId,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to);

    @Query("SELECT u.endpoint, COUNT(u), AVG(u.responseMs) " +
           "FROM PartnerApiUsage u WHERE u.partner.partnerId = :partnerId " +
           "GROUP BY u.endpoint")
    List<Object[]> getEndpointStats(@Param("partnerId") String partnerId);

    long countByPartner_PartnerId(String partnerId);
}
