package com.burjkhalifacorp.storage.service.models;

import com.burjkhalifacorp.storage.api.models.FileMetadataDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.InputStream;

@AllArgsConstructor
@Getter
public class StoredFile {
    FileMetadataDto metadata;
    InputStream inputStream;
}
