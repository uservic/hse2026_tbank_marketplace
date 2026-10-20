package com.marketplace.product.exception;

import com.marketplace.product.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Перехватывает ошибки контрактной валидации входных данных.
     * Возвращает код ошибки VALIDATION_ERROR (HTTP 400) и детализирует нарушенные поля.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, Object> details = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            details.put(fieldName, errorMessage);
        });

        ErrorResponse response = new ErrorResponse()
                .errorCode("VALIDATION_ERROR")
                .message("Ошибка валидации входных данных");

        response.setDetails(details); // Прокидываем мапу с ошибками в object details

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Перехватывает кастомные бизнес-исключения.
     * Автоматически маппит коды ошибок (например, PRODUCT_NOT_FOUND) в формат контракта.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        ErrorResponse response = new ErrorResponse()
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage());

        return ResponseEntity.status(ex.getStatus()).body(response);
    }
}