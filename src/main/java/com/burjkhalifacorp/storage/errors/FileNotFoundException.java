package com.burjkhalifacorp.storage.errors;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class FileNotFoundException extends StorageException {
    public FileNotFoundException(UUID fileId) {
        super("File %s not found".formatted(fileId), HttpStatus.NOT_FOUND);
    }
}
