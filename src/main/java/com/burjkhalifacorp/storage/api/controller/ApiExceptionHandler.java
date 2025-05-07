package com.burjkhalifacorp.storage.api.controller;

import com.burjkhalifacorp.storage.errors.NotImplementedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;

@ControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(NotImplementedException.class)
    public ResponseEntity<String> handleNotImplemented(NotImplementedException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_IMPLEMENTED)
                .body(ex.getMessage());
    }

    @ExceptionHandler({IOException.class, RuntimeException.class})
    public ResponseEntity<String> handleCoreExceptions(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ex.getMessage());
    }
}
