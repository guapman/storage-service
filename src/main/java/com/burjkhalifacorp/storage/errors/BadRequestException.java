package com.burjkhalifacorp.storage.errors;

import org.springframework.http.HttpStatus;

public class BadRequestException extends StorageException {
    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}