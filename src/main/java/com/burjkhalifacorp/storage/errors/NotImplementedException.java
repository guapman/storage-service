package com.burjkhalifacorp.storage.errors;

public class NotImplementedException extends StorageException {
    public NotImplementedException() {
        super("Feature is not implemented");
    }
}
