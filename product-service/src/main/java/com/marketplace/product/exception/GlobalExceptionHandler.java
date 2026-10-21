package com.marketplace.product.exception;

import com.marketplace.product.model.ErrorCode;
import com.marketplace.product.model.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Ошибки валидации тела запроса ({@code @Valid} на {@code @RequestBody}).
     * Возвращает {@code VALIDATION_ERROR} (400) с полями в {@code details.errors}.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<Map<String, String>> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> Map.of(
                        "field", fe.getField(),
                        "message", fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid value"))
                .toList();

        ErrorResponse response = new ErrorResponse()
                .errorCode(ErrorCode.VALIDATION_ERROR)
                .message("Ошибка валидации входных данных");
        response.setDetails(Map.of("errors", errors));

        log.warn("Validation error: {}", errors);

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Ошибки валидации параметров запроса ({@code @Validated} + {@code @Min}/{@code @Max}).
     * Возвращает {@code VALIDATION_ERROR} (400) с полями в {@code details.errors}.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        List<Map<String, String>> errors = ex.getConstraintViolations()
                .stream()
                .map(v -> Map.of(
                        "field", v.getPropertyPath().toString(),
                        "message", v.getMessage()))
                .toList();

        ErrorResponse response = new ErrorResponse()
                .errorCode(ErrorCode.VALIDATION_ERROR)
                .message("Ошибка валидации параметров запроса");
        response.setDetails(Map.of("errors", errors));

        log.warn("Constraint violation: {}", errors);

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Контролируемые бизнес-ошибки ({@link BusinessException}).
     * Статус и код берутся из исключения; неизвестный код заменяется на {@code VALIDATION_ERROR}.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        log.warn("Business error: code={}, message={}", ex.getErrorCode(), ex.getMessage());

        ErrorResponse response = new ErrorResponse()
                .errorCode(ErrorCode.fromValue(ex.getErrorCode()))
                .message(ex.getMessage());

        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    /**
     * Непредвиденные ошибки — fallback, чтобы ответ всегда был в контрактном формате.
     * Возвращает 500 с {@link ErrorResponse}.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);

        ErrorResponse response = new ErrorResponse()
                .errorCode(ErrorCode.VALIDATION_ERROR)
                .message("Внутренняя ошибка сервера");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Маппит строковый код в {@link ErrorCode}; при неизвестном коде — {@code VALIDATION_ERROR}.
     */
    private ErrorCode parseErrorCode(String code) {
        try {
            return ErrorCode.valueOf(code);
        } catch (IllegalArgumentException e) {
            log.warn("Unknown error code: {}, falling back to VALIDATION_ERROR", code);
            return ErrorCode.VALIDATION_ERROR;
        }
    }
}