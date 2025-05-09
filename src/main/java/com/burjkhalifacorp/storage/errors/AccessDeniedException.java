package com.burjkhalifacorp.storage.errors;

import org.springframework.http.HttpStatus;

public class AccessDeniedException extends StorageException {
    public AccessDeniedException() {
        super("Access denied", HttpStatus.UNAUTHORIZED);
    }
}
