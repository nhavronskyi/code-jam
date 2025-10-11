package com.team.codejam.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;
import java.util.UUID;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception ex, WebRequest request) {
        String correlationId = UUID.randomUUID().toString();
        // Log error with correlationId (logging framework assumed)
        return ResponseEntity.status(500).body(Map.of(
                "error", "An unexpected error occurred.",
                "correlationId", correlationId
        ));
    }
}
