package com.burjkhalifacorp.storage.api.models;

public enum FileSortBy {
    FILENAME("filename"),
    UPLOAD_DATE("uploadDate"),
    TAG("tag"),
    CONTENT_TYPE("contentType");

    private final String dbField;

    FileSortBy(String dbField) {
        this.dbField = dbField;
    }

    public String getDbField() {
        return dbField;
    }
}
