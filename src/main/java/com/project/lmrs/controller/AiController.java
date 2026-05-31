package com.project.lmrs.controller;

import com.project.lmrs.dto.request.AiConciergeRequest;
import com.project.lmrs.dto.request.AiFeedbackRequest;
import com.project.lmrs.dto.request.AiMenuDescriptionRequest;
import com.project.lmrs.dto.response.AiConciergeResponse;
import com.project.lmrs.dto.response.AiFeedbackResponse;
import com.project.lmrs.dto.response.AiMenuDescriptionResponse;
import com.project.lmrs.security.SecurityUtils;
import com.project.lmrs.service.AiConciergeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiConciergeService aiConciergeService;

    @PostMapping("/concierge")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','FRONT_DESK')")
    public ResponseEntity<AiConciergeResponse> concierge(@Valid @RequestBody AiConciergeRequest request) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        AiConciergeResponse response = aiConciergeService.handleConciergeQuery(
                request.getQuery(),
                request.getGuestId(),
                request.getReservationId(),
                request.getRoomId(),
                tenantId
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/feedback-analyze")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<AiFeedbackResponse> analyzeFeedback(@Valid @RequestBody AiFeedbackRequest request) {
        AiFeedbackResponse response = aiConciergeService.analyzeFeedback(
                request.getFeedbackText(),
                request.getGuestName()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/menu-description")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','KITCHEN')")
    public ResponseEntity<AiMenuDescriptionResponse> generateMenuDescription(
            @Valid @RequestBody AiMenuDescriptionRequest request) {
        AiMenuDescriptionResponse response = aiConciergeService.generateMenuDescription(
                request.getItemName(),
                request.getCategoryName(),
                request.getIngredients(),
                request.getDietaryFlags(),
                request.getBasePrice()
        );
        return ResponseEntity.ok(response);
    }
}
