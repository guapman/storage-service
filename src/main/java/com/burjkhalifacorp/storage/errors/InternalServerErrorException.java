package com.burjkhalifacorp.storage.errors;

import org.springframework.http.HttpStatus;

public class InternalServerErrorException extends StorageException {
    public InternalServerErrorException() {
        super("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
