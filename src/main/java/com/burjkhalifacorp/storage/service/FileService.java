package com.burjkhalifacorp.storage.service;

import com.burjkhalifacorp.storage.api.models.FileMetadataDto;
import com.burjkhalifacorp.storage.common.Visibility;
import com.burjkhalifacorp.storage.errors.StorageException;
import com.burjkhalifacorp.storage.service.models.StoredFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

public interface FileService {
    FileMetadataDto upload(
            String userId,
            String filename,
            String contentType,
            Visibility visibility,
            Set<String> tags,
            InputStream inputStream) throws StorageException;

    void deleteFile(String userId, UUID fileId) throws StorageException;

    FileMetadataDto renameFile(String userId, UUID fileId, String newFilename) throws StorageException;

    StoredFile getFile(String userId, UUID fileId) throws StorageException;

    Page<FileMetadataDto> listPublicFiles(Set<String> tags, Pageable pageable) throws StorageException;

    Page<FileMetadataDto> listUserFiles(String userId, Set<String> tags, Pageable pageable) throws StorageException;
}
