package com.project.lmrs.filter;

import com.project.lmrs.entity.PartnerAccount;
import com.project.lmrs.entity.PartnerApiUsage;
import com.project.lmrs.repository.PartnerAccountRepository;
import com.project.lmrs.repository.PartnerApiUsageRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class PartnerApiKeyFilter extends OncePerRequestFilter {

    private final PartnerAccountRepository partnerAccountRepository;
    private final PartnerApiUsageRepository partnerApiUsageRepository;
    private final ConcurrentHashMap<String, long[]> rateLimitMap = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/v1/partner");
    }

    private boolean isRateLimited(String apiKey) {
        long now = System.currentTimeMillis();
        long[] hits = rateLimitMap.computeIfAbsent(apiKey, k -> new long[]{0, 0});
        synchronized (hits) {
            if (now - hits[1] > 60000) {
                hits[0] = 0;
                hits[1] = now;
            }
            hits[0]++;
            return hits[0] > 100;
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String apiKey = request.getHeader("X-API-Key");
        long start = System.currentTimeMillis();

        if (apiKey == null || apiKey.isBlank()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Missing X-API-Key header\"}");
            return;
        }

        PartnerAccount partner = partnerAccountRepository
                .findByApiKeyAndIsActiveTrueAndIsDeletedFalse(apiKey)
                .orElse(null);

        if (partner == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Invalid or inactive API key\"}");
            return;
        }

        if (isRateLimited(apiKey)) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Rate limit exceeded. Max 100 requests per minute.\"}");
            return;
        }

        request.setAttribute("partner", partner);
        request.setAttribute("tenantId", partner.getTenant().getTenantId());

        StatusCapturingWrapper wrapped = new StatusCapturingWrapper(response);
        chain.doFilter(request, wrapped);

        int elapsed = (int)(System.currentTimeMillis() - start);
        try {
            partnerApiUsageRepository.save(PartnerApiUsage.builder()
                    .partner(partner)
                    .endpoint(request.getRequestURI())
                    .httpMethod(request.getMethod())
                    .statusCode(wrapped.getStatus())
                    .responseMs(elapsed)
                    .build());
        } catch (Exception e) {
            log.warn("Could not log partner API usage: {}", e.getMessage());
        }
    }

    static class StatusCapturingWrapper extends HttpServletResponseWrapper {
        private int status = 200;

        public StatusCapturingWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public void setStatus(int sc) {
            this.status = sc;
            super.setStatus(sc);
        }

        @Override
        public void sendError(int sc) throws IOException {
            this.status = sc;
            super.sendError(sc);
        }

        @Override
        public void sendError(int sc, String msg) throws IOException {
            this.status = sc;
            super.sendError(sc, msg);
        }

        public int getStatus() {
            return status;
        }
    }
}
