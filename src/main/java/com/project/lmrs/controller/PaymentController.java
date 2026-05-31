package com.project.lmrs.controller;

import com.project.lmrs.dto.request.ProcessPaymentRequest;
import com.project.lmrs.dto.response.PaymentResponse;
import com.project.lmrs.entity.Payment;
import com.project.lmrs.service.PaymentService;
import com.project.lmrs.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/folio/{folioId}")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','FRONT_DESK','FINANCE')")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByFolio(@PathVariable String folioId) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        List<Payment> payments = paymentService.getPaymentsByFolio(folioId, tenantId);
        return ResponseEntity.ok(payments.stream().map(this::toResponse).collect(Collectors.toList()));
    }

    @PostMapping("/folio/{folioId}")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','FRONT_DESK','FINANCE')")
    public ResponseEntity<PaymentResponse> processPayment(@PathVariable String folioId,
                                                           @Valid @RequestBody ProcessPaymentRequest request) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(paymentService.processPayment(folioId, request, tenantId)));
    }

    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .amount(payment.getAmount())
                .method(payment.getMethod().name())
                .status(payment.getStatus())
                .gatewayRef(payment.getGatewayRef())
                .paidAt(payment.getPaidAt())
                .build();
    }
}
