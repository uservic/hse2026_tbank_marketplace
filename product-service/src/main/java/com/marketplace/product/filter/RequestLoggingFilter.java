package com.marketplace.product.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/*
* Отвечает за логирование запросов, клакладёт данные в MDC и явно вызывает логирование
 */
@Slf4j
@Component
@Order(1)
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        var bodySizeLimitBytes = 1024;
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request, bodySizeLimitBytes);

        String requestId = request.getHeader("X-Request-Id");
        if (requestId == null) {
            requestId = UUID.randomUUID().toString();
        }

        // Кладем в MDC, чтобы все логи внутри запроса его подхватили
        MDC.put("request_id", requestId);
        // Кладем метод и endpoint сразу, чтобы не вычислять в каждом логе
        MDC.put("method", request.getMethod());
        MDC.put("endpoint", request.getRequestURI());

        response.setHeader("X-Request-Id", requestId);

        long startTime = System.currentTimeMillis();
        try {
            filterChain.doFilter(wrappedRequest, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            MDC.put("status_code", String.valueOf(response.getStatus()));
            MDC.put("duration_ms", String.valueOf(duration));

            // Логируем тело ТОЛЬКО для POST и PUT
            if (List.of("POST", "PUT").contains(request.getMethod())) {
                long contentLength = request.getContentLengthLong();

                if (contentLength > bodySizeLimitBytes) {
                    // Если тело больше лимита, не логируем его, а пишем размер
                    MDC.put("request_body", "[TOO LARGE: " + contentLength + " bytes]");
                } else {
                    byte[] body = wrappedRequest.getContentAsByteArray();
                    if (body.length > 0) {
                        String requestBody = new String(body, StandardCharsets.UTF_8);
                        MDC.put("request_body", requestBody);
                    }
                }
            }

            log.info("Request completed");

            // Очищаем MDC, чтобы не засорять следующий запрос в этом же потоке
            MDC.clear();
        }
    }
}
