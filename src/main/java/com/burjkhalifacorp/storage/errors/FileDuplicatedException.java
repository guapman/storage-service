package com.burjkhalifacorp.storage.errors;

import org.springframework.http.HttpStatus;

public class FileDuplicatedException extends StorageException {
    public FileDuplicatedException() {
        super("File duplicated", HttpStatus.CONFLICT);
    }
}
