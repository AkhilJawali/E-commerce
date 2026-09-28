package com.shopease.exception;

import lombok.Getter;

/**
 * Custom exception for cart-related errors.
 * 
 * @author akhil jawali
 * @since 1.0
 */
@Getter
public class CartException extends RuntimeException {

    private final String errorCode;

    public CartException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public CartException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
