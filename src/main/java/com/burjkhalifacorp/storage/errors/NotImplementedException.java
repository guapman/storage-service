package com.burjkhalifacorp.storage.errors;

import org.springframework.http.HttpStatus;

public class NotImplementedException extends StorageException {
    public NotImplementedException() {
        super("Feature is not implemented", HttpStatus.NOT_IMPLEMENTED);
    }
}
