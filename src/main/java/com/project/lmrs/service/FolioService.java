package com.project.lmrs.service;

import com.project.lmrs.dto.request.PostChargeRequest;
import com.project.lmrs.dto.response.FolioResponse;
import com.project.lmrs.entity.*;
import com.project.lmrs.enums.ChargeType;
import com.project.lmrs.exception.BusinessRuleException;
import com.project.lmrs.exception.ResourceNotFoundException;
import com.project.lmrs.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FolioService {

    private final FolioRepository folioRepository;
    private final FolioLineItemRepository folioLineItemRepository;
    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final GuestRepository guestRepository;

    public FolioResponse getFolioByReservation(String reservationId, String tenantId) {
        Folio folio = folioRepository.findByReservation_ReservationIdAndIsDeletedFalse(reservationId)
                .filter(f -> f.getReservation().getTenant().getTenantId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Folio", "reservationId", reservationId));
        return toResponse(folio);
    }

    public FolioResponse getFolioById(String folioId, String tenantId) {
        Folio folio = folioRepository.findByFolioIdAndReservation_Tenant_TenantIdAndIsDeletedFalse(folioId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Folio", "id", folioId));
        return toResponse(folio);
    }

    @Transactional
    public FolioResponse createFolio(String reservationId, String tenantId) {
        Reservation reservation = reservationRepository.findByReservationIdAndTenant_TenantIdAndIsDeletedFalse(reservationId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", reservationId));

        folioRepository.findByReservation_ReservationIdAndIsDeletedFalse(reservationId)
                .ifPresent(f -> { throw new BusinessRuleException("Folio already exists for this reservation"); });

        Folio folio = Folio.builder()
                .reservation(reservation)
                .guest(reservation.getGuest())
                .currency("INR")
                .isDeleted(false)
                .build();

        folio = folioRepository.save(folio);
        return toResponse(folio);
    }

    @Transactional
    public FolioResponse postCharge(String folioId, PostChargeRequest request, String tenantId) {
        Folio folio = folioRepository.findByFolioIdAndReservation_Tenant_TenantIdAndIsDeletedFalse(folioId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Folio", "id", folioId));

        if (!"OPEN".equals(folio.getStatus())) {
            throw new BusinessRuleException("Cannot post charges to a closed folio");
        }

        FolioLineItem lineItem = FolioLineItem.builder()
                .folio(folio)
                .description(request.getDescription())
                .amount(request.getAmount())
                .chargeType(ChargeType.valueOf(request.getChargeType()))
                .build();

        folioLineItemRepository.save(lineItem);

        folio.setTotalAmount(folio.getTotalAmount().add(request.getAmount()));
        folioRepository.save(folio);

        return toResponse(folio);
    }

    @Transactional
    public void closeFolio(String folioId, String tenantId) {
        Folio folio = folioRepository.findByFolioIdAndReservation_Tenant_TenantIdAndIsDeletedFalse(folioId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Folio", "id", folioId));

        if (folio.getTotalAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
            throw new BusinessRuleException("Cannot close folio with outstanding balance of ₹" + folio.getTotalAmount());
        }

        folio.setStatus("CLOSED");
        folioRepository.save(folio);
    }

    private FolioResponse toResponse(Folio folio) {
        List<FolioLineItem> lineItems = folio.getLineItems() != null
                ? folio.getLineItems().stream().filter(li -> !li.isDeleted()).toList()
                : List.of();

        List<FolioResponse.LineItemDto> lineItemDtos = lineItems.stream()
                .map(li -> FolioResponse.LineItemDto.builder()
                        .lineItemId(li.getLineItemId())
                        .description(li.getDescription())
                        .amount(li.getAmount())
                        .chargeType(li.getChargeType().name())
                        .postedAt(li.getPostedAt())
                        .build())
                .collect(Collectors.toList());

        List<Payment> payments = folio.getPayments() != null
                ? folio.getPayments().stream().filter(p -> !p.isDeleted()).toList()
                : List.of();

        List<FolioResponse.PaymentDto> paymentDtos = payments.stream()
                .map(p -> FolioResponse.PaymentDto.builder()
                        .paymentId(p.getPaymentId())
                        .amount(p.getAmount())
                        .method(p.getMethod().name())
                        .status(p.getStatus())
                        .paidAt(p.getPaidAt())
                        .build())
                .collect(Collectors.toList());

        return FolioResponse.builder()
                .folioId(folio.getFolioId())
                .reservationId(folio.getReservation().getReservationId())
                .guestName(folio.getGuest().getFirstName() + " " + folio.getGuest().getLastName())
                .status(folio.getStatus())
                .totalAmount(folio.getTotalAmount())
                .currency(folio.getCurrency())
                .lineItems(lineItemDtos)
                .payments(paymentDtos)
                .build();
    }
}
