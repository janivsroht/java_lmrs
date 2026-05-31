package com.project.lmrs.service;

import com.project.lmrs.dto.request.ProcessPaymentRequest;
import com.project.lmrs.entity.Folio;
import com.project.lmrs.entity.Payment;
import com.project.lmrs.enums.PaymentMethod;
import com.project.lmrs.exception.BusinessRuleException;
import com.project.lmrs.exception.ResourceNotFoundException;
import com.project.lmrs.repository.FolioRepository;
import com.project.lmrs.repository.PaymentRepository;
import com.project.lmrs.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final FolioRepository folioRepository;
    private final LoyaltyService loyaltyService;
    private final AuditLogService auditLogService;

    public List<Payment> getPaymentsByFolio(String folioId, String tenantId) {
        folioRepository.findByFolioIdAndReservation_Tenant_TenantIdAndIsDeletedFalse(folioId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Folio", "id", folioId));
        return paymentRepository.findAllByFolio_FolioId(folioId);
    }

    @Transactional
    public Payment processPayment(String folioId, ProcessPaymentRequest request, String tenantId) {
        Folio folio = folioRepository.findByFolioIdAndReservation_Tenant_TenantIdAndIsDeletedFalse(folioId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Folio", "id", folioId));

        if (!"OPEN".equals(folio.getStatus())) {
            throw new BusinessRuleException("Cannot process payment on a closed folio");
        }

        if (request.getAmount().compareTo(folio.getTotalAmount()) > 0) {
            throw new BusinessRuleException(
                    String.format("Payment amount ₹%s exceeds folio balance ₹%s",
                            request.getAmount(), folio.getTotalAmount()));
        }

        Payment payment = Payment.builder()
                .folio(folio)
                .amount(request.getAmount())
                .method(PaymentMethod.valueOf(request.getMethod()))
                .gatewayRef(request.getGatewayRef())
                .status("COMPLETED")
                .paidAt(LocalDateTime.now())
                .build();

        payment = paymentRepository.save(payment);

        folio.setTotalAmount(folio.getTotalAmount().subtract(request.getAmount()));
        if (folio.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            folio.setStatus("CLOSED");
        }
        folioRepository.save(folio);

        // Loyalty: earn 1 point per rupee (100 paise) spent
        if (folio.getGuest() != null) {
            int pointsEarned = request.getAmount().intValue();
            if (pointsEarned > 0) {
                loyaltyService.earnPoints(
                        folio.getGuest().getGuestId(),
                        pointsEarned,
                        payment.getPaymentId(),
                        "PAYMENT"
                );
            }
        }

        // Audit log
        auditLogService.log(
                SecurityUtils.getCurrentTenantId(),
                SecurityUtils.getCurrentUserId(),
                "PAYMENT_PROCESSED",
                "Folio",
                folioId,
                Map.of("totalAmount", folio.getTotalAmount().add(request.getAmount())),
                Map.of("paymentId", payment.getPaymentId(),
                        "amount", request.getAmount(),
                        "method", request.getMethod(),
                        "folioStatus", folio.getStatus()),
                null
        );

        return payment;
    }
}
