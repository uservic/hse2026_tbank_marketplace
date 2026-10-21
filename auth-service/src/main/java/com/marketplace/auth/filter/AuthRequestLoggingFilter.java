package com.marketplace.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Фильтр логирования HTTP-запросов для auth-service.
 * Этот фильтр намеренно не логирует тело запроса.
 * В логи попадают только метаданные: HTTP-метод, путь (без query-параметров),
 * статус ответа, время выполнения и request_id.
 */
@Slf4j
@Component
@Order(1)
public class AuthRequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestId = request.getHeader("X-Request-Id");
        if (requestId == null) {
            requestId = UUID.randomUUID().toString();
        }

        MDC.put("request_id", requestId);
        MDC.put("method", request.getMethod());
        // getRequestURI() возвращает путь БЕЗ query-параметров
        MDC.put("endpoint", request.getRequestURI());
        MDC.put("request_body_logged", "false");

        response.setHeader("X-Request-Id", requestId);

        long startTime = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            MDC.put("status_code", String.valueOf(response.getStatus()));
            MDC.put("duration_ms", String.valueOf(duration));

            log.info("Auth request completed");

            MDC.clear();
        }
    }
}
