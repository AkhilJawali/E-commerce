package com.shopease.exception;

import com.shopease.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Exception handler for cart operations.
 * 
 * @author akhil jawali
 * @since 1.0
 */
@RestControllerAdvice
@Slf4j
public class CartExceptionHandler {

    @ExceptionHandler(CartException.class)
    public ResponseEntity<ErrorResponse> handleCartException(CartException ex, HttpServletRequest request) {
        log.warn("Cart exception: {} - {}", ex.getErrorCode(), ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(getStatusForErrorCode(ex.getErrorCode()))
            .error(getErrorNameForErrorCode(ex.getErrorCode()))
            .message(ex.getMessage())
            .path(request.getRequestURI())
            .details(List.of())
            .build();
        
        return ResponseEntity.status(error.getStatus()).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        List<ErrorResponse.Detail> details = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> ErrorResponse.Detail.builder()
                .field(error.getField())
                .message(error.getDefaultMessage())
                .rejectedValue(error.getRejectedValue())
                .build())
            .collect(Collectors.toList());

        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(400)
            .error("Bad Request")
            .message("Validation failed")
            .path(request.getRequestURI())
            .details(details)
            .build();

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        
        List<ErrorResponse.Detail> details = ex.getConstraintViolations().stream()
            .map(violation -> ErrorResponse.Detail.builder()
                .field(violation.getPropertyPath().toString())
                .message(violation.getMessage())
                .rejectedValue(violation.getInvalidValue())
                .build())
            .collect(Collectors.toList());

        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(400)
            .error("Bad Request")
            .message("Validation failed")
            .path(request.getRequestURI())
            .details(details)
            .build();

        return ResponseEntity.badRequest().body(error);
    }

    private int getStatusForErrorCode(String errorCode) {
        return switch (errorCode) {
            case "ITEM_NOT_FOUND", "CART_NOT_FOUND" -> 404;
            case "OUT_OF_STOCK", "INSUFFICIENT_STOCK", "CART_FULL", 
                 "PRODUCT_INACTIVE", "MAX_QUANTITY_EXCEEDED" -> 400;
            default -> 400;
        };
    }

    private String getErrorNameForErrorCode(String errorCode) {
        return switch (errorCode) {
            case "ITEM_NOT_FOUND", "CART_NOT_FOUND" -> "Not Found";
            default -> "Bad Request";
        };
    }
}
