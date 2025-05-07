package com.burjkhalifacorp.storage.errors;

public class StorageException extends RuntimeException {
    public StorageException(String message) {
        super(message);
    }
}