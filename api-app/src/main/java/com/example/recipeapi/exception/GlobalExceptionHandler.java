package com.example.recipeapi.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

// This can later be extended to handle individual exceptions depending on how we want to convey to client
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest().body(java.util.Map.of("error","validation_failed"));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAll(Exception ex) {
        ex.printStackTrace();
        return ResponseEntity.status(500).body(java.util.Map.of("error","internal"));
    }
}
