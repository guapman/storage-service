package com.burjkhalifacorp.storage.mappers;

import com.burjkhalifacorp.storage.api.models.FileMetadataDto;
import com.burjkhalifacorp.storage.persist.models.FileMetadata;
import org.springframework.stereotype.Component;

@Component
public class FileMetadataMapper {
    public FileMetadataDto toDto(FileMetadata metadata) {
        return new FileMetadataDto(
                metadata.getExternalId(),
                metadata.getFilename(),
                metadata.getTags(),
                metadata.getSize(),
                metadata.getVisibility(),
                metadata.getContentType(),
                metadata.getUploadDate()
        );
    }
}
