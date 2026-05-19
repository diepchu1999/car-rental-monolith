package com.ares.car_rental_monolith.shared.error;

import com.ares.car_rental_monolith.shared.api.ApiResponse;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse<Void>> handleDomain(DomainException ex) {
        return respond(ex.errorCode(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return respond(ErrorCode.VALIDATION_ERROR, message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return respond(ErrorCode.INVALID_PARAMETER, "Invalid value for parameter '" + ex.getName() + "'");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        return respond(ErrorCode.INTERNAL_ERROR, "An unexpected error occurred");
    }

    private ResponseEntity<ApiResponse<Void>> respond(ErrorCode code, String message) {
        return ResponseEntity
                .status(code.httpStatus())
                .body(ApiResponse.error(code.code(), message));
    }
}
