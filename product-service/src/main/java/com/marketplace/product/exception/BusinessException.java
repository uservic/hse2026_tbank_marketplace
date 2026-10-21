package com.marketplace.product.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Кастомное исключение для обработки контролируемых бизнес-ошибок маркетплейса.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus status;

    public BusinessException(String errorCode, HttpStatus status, String message) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }
}
