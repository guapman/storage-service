package com.burjkhalifacorp.storage.api.controller;

import com.burjkhalifacorp.storage.api.models.ErrorResponse;
import com.burjkhalifacorp.storage.errors.StorageException;
import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(StorageException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleStorageExceptions(StorageException ex) {
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(new ErrorResponse(ex.getMessage(), "", ex.getStatusCode().value()));
    }

    @ExceptionHandler({ValidationException.class})
    public ResponseEntity<ErrorResponse> handleCoreValidation(ValidationException ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Validation failed",
                        ex.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<ErrorResponse> handleCoreExceptions(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Unexpected error",
                        ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
}
