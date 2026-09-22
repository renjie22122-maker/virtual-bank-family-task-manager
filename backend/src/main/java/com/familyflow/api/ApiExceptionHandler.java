package com.familyflow.api;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<?> security(SecurityException error) { return response(HttpStatus.FORBIDDEN, error.getMessage()); }
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<?> badRequest(RuntimeException error) { return response(HttpStatus.BAD_REQUEST, error.getMessage()); }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> validation(MethodArgumentNotValidException error) {
        String message = error.getBindingResult().getFieldErrors().stream().findFirst()
                .map(item -> item.getField() + " " + item.getDefaultMessage()).orElse("Invalid request.");
        return response(HttpStatus.BAD_REQUEST, message);
    }
    private ResponseEntity<?> response(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("status", status.value(), "message", message == null ? status.getReasonPhrase() : message, "timestamp", Instant.now().toString()));
    }
}
