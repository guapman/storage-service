package com.burjkhalifacorp.storage.errors;

import org.springframework.http.HttpStatus;

public abstract class StorageException extends RuntimeException {
    private HttpStatus statusCode;

    public StorageException(String message, HttpStatus statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
    public StorageException(String message, Throwable cause, HttpStatus statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public HttpStatus getStatusCode() {
        return statusCode;
    }
}