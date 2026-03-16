package com.nurun.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status)
                .body(buildErrorMap(ex.getMessage(), status));
    }

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleAlreadyExists(AlreadyExistsException ex) {
        HttpStatus status = HttpStatus.CONFLICT;
        return ResponseEntity.status(status)
                .body(buildErrorMap(ex.getMessage(), status));
    }

    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<Map<String, Object>> handleRateLimit(RateLimitException ex) {
        HttpStatus status = HttpStatus.TOO_MANY_REQUESTS;
        return ResponseEntity.status(status)
                .body(buildErrorMap(ex.getMessage(), status));
    }

    @ExceptionHandler(AllProvidersFailedException.class)
    public ResponseEntity<Map<String, Object>> handleAllProviderFailed(AllProvidersFailedException ex) {
        HttpStatus status = HttpStatus.TOO_MANY_REQUESTS;
        return ResponseEntity.status(status)
                .body(buildErrorMap(ex.getMessage(), status));
    }

    @ExceptionHandler(ModelNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleModelNotSupport(ModelNotSupportedException ex) {
        HttpStatus status = HttpStatus.UNSUPPORTED_MEDIA_TYPE;
        return ResponseEntity.status(status)
                .body(buildErrorMap(ex.getMessage(), status));
    }

    @ExceptionHandler(ModelUnavailableException.class)
    public ResponseEntity<Map<String, Object>> handleModelUnavailable(ModelUnavailableException ex) {
        HttpStatus status = HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(status)
                .body(buildErrorMap(ex.getMessage(), status));
    }




    private Map<String, Object> buildErrorMap(String message, HttpStatus status) {
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("timestamp", Instant.now());
        errorBody.put("status", status.value());
        errorBody.put("error", status.getReasonPhrase());
        errorBody.put("message", message);

        return errorBody;
    }



}
