package com.burjkhalifacorp.storage.errors;

public class InitializationFailedException extends RuntimeException {
    public InitializationFailedException(String error, Throwable cause) {
        super("Initialization failed: " + error, cause);
    }
}