package com.project.lmrs.service;

import com.project.lmrs.dto.response.AiConciergeResponse;
import com.project.lmrs.dto.response.AiFeedbackResponse;
import com.project.lmrs.dto.response.AiMenuDescriptionResponse;
import com.project.lmrs.entity.Guest;
import com.project.lmrs.entity.Reservation;
import com.project.lmrs.repository.GuestRepository;
import com.project.lmrs.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AiConciergeService {

    private final GroqAiService groqAiService;
    private final GuestRepository guestRepository;
    private final ReservationRepository reservationRepository;

    public AiConciergeResponse handleConciergeQuery(String query, String guestId,
                                                      String reservationId, String roomId,
                                                      String tenantId) {
        StringBuilder context = new StringBuilder();

        if (guestId != null) {
            guestRepository.findByGuestIdAndTenant_TenantIdAndIsDeletedFalse(guestId, tenantId)
                    .ifPresent(g -> {
                context.append("Guest: ").append(g.getFirstName()).append(" ").append(g.getLastName());
                if (g.getLoyaltyTier() != null) {
                    context.append(" (").append(g.getLoyaltyTier()).append(" tier)");
                }
                context.append(". ");
            });
        }

        if (reservationId != null) {
            reservationRepository.findByReservationIdAndTenant_TenantIdAndIsDeletedFalse(reservationId, tenantId)
                    .ifPresent(r -> {
                context.append("Reservation: ").append(r.getStatus())
                        .append(", check-in: ").append(r.getCheckInDate())
                        .append(", check-out: ").append(r.getCheckOutDate())
                        .append(", room: ").append(r.getRoom().getRoomNumber())
                        .append(" (").append(r.getRoom().getRoomType().getName()).append("). ");
            });
        }

        String systemPrompt = "You are an AI concierge assistant for a luxury hotel and restaurant called LMRS. " +
                "You help staff respond to guest queries in a courteous, personalized manner. " +
                "Keep responses concise and professional. " +
                "If you don't have enough information to answer, suggest the staff member follow up directly with the guest. " +
                "Available hotel services include: spa, pool, gym, room service, valet parking, business center. " +
                "Here is the context you have:\n" + context;

        Map<String, Object> result = groqAiService.chatCompletion(systemPrompt, query);

        return AiConciergeResponse.builder()
                .reply((String) result.get("content"))
                .model((String) result.get("model"))
                .tokensUsed(((Number) result.getOrDefault("tokens", 0)).longValue())
                .build();
    }

    public AiFeedbackResponse analyzeFeedback(String feedbackText, String guestName) {
        String systemPrompt = "You are an AI assistant that analyzes hotel guest feedback. " +
                "Analyze the feedback and provide:\n" +
                "1. Overall sentiment (POSITIVE, NEGATIVE, or NEUTRAL)\n" +
                "2. A one-sentence summary of the feedback\n" +
                "3. A suggested reply the hotel manager could send to the guest\n\n" +
                "Format your response exactly as:\n" +
                "SENTIMENT: <sentiment>\n" +
                "SUMMARY: <summary>\n" +
                "SUGGESTED_REPLY: <reply>";

        String userMessage = "Guest: " + (guestName != null ? guestName : "Anonymous") + "\nFeedback: " + feedbackText;

        Map<String, Object> result = groqAiService.chatCompletion(systemPrompt, userMessage);
        String content = (String) result.get("content");

        String sentiment = extractField(content, "SENTIMENT:");
        String summary = extractField(content, "SUMMARY:");
        String suggestedReply = extractField(content, "SUGGESTED_REPLY:");

        if (sentiment == null) sentiment = "NEUTRAL";
        if (summary == null) summary = feedbackText.length() > 100 ? feedbackText.substring(0, 100) + "..." : feedbackText;
        if (suggestedReply == null) suggestedReply = "Thank you for your feedback. We will review your comments.";

        return AiFeedbackResponse.builder()
                .sentiment(sentiment)
                .summary(summary)
                .suggestedReply(suggestedReply)
                .model((String) result.get("model"))
                .tokensUsed(((Number) result.getOrDefault("tokens", 0)).longValue())
                .build();
    }

    public AiMenuDescriptionResponse generateMenuDescription(String itemName, String categoryName,
                                                               java.util.List<String> ingredients,
                                                               java.util.List<String> dietaryFlags,
                                                               double basePrice) {
        String systemPrompt = "You are a professional food writer creating menu descriptions for an upscale restaurant. " +
                "Generate an appealing description for a menu item based on the details provided. " +
                "Keep it under 60 words. Highlight key flavors, texture, and presentation. " +
                "Mention dietary suitability if relevant. Be evocative and appetizing.";

        StringBuilder details = new StringBuilder();
        details.append("Item: ").append(itemName).append("\n");
        details.append("Category: ").append(categoryName).append("\n");
        if (ingredients != null && !ingredients.isEmpty()) {
            details.append("Ingredients: ").append(String.join(", ", ingredients)).append("\n");
        }
        if (dietaryFlags != null && !dietaryFlags.isEmpty()) {
            details.append("Dietary: ").append(String.join(", ", dietaryFlags)).append("\n");
        }
        details.append("Price: ₹").append(String.format("%.2f", basePrice));

        Map<String, Object> result = groqAiService.chatCompletion(systemPrompt, details.toString());

        return AiMenuDescriptionResponse.builder()
                .description((String) result.get("content"))
                .model((String) result.get("model"))
                .tokensUsed(((Number) result.getOrDefault("tokens", 0)).longValue())
                .build();
    }

    private String extractField(String text, String prefix) {
        if (text == null) return null;
        int idx = text.indexOf(prefix);
        if (idx == -1) return null;
        int start = idx + prefix.length();
        int end = text.indexOf('\n', start);
        if (end == -1) end = text.length();
        String value = text.substring(start, end).trim();
        return value.isEmpty() ? null : value;
    }
}
