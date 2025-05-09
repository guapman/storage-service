package com.burjkhalifacorp.storage.service;

import com.burjkhalifacorp.storage.api.models.FileMetadataDto;
import com.burjkhalifacorp.storage.common.Visibility;
import com.burjkhalifacorp.storage.errors.*;
import com.burjkhalifacorp.storage.mappers.FileMetadataMapper;
import com.burjkhalifacorp.storage.persist.FileMetadataRepository;
import com.burjkhalifacorp.storage.persist.models.FileMetadata;
import com.burjkhalifacorp.storage.service.models.StoredFile;
import com.burjkhalifacorp.storage.utils.UploadHelperInputStream;
import io.minio.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FileServiceImpl implements FileService {
    private final MinioClient minioClient;
    private final FileMetadataRepository fileRepository;
    private final FileMetadataMapper fileMetadataMapper;
    private final Tika tika = new Tika();

    @Value("${minio.bucket}")
    private String bucketName;

    public FileServiceImpl(
            MinioClient minioClient,
            FileMetadataRepository fileRepository,
            FileMetadataMapper fileMetadataMapper) {
        this.minioClient = minioClient;
        this.fileRepository = fileRepository;
        this.fileMetadataMapper = fileMetadataMapper;
    }

    @PostConstruct
    public void init() {
        try {
            boolean found = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            log.error("failed to create MinIO bucket: {}", e.getMessage());
            throw new InitializationFailedException("failed to create MinIO bucket", e.getCause());
        }
    }

    @Override
    public FileMetadataDto upload(
            String userId,
            String filename,
            String contentType,
            Visibility visibility,
            Set<String> tags,
            InputStream inputStream
    ) throws StorageException {
        final int PART_SIZE = 10 * 1024 * 1024;

        MessageDigest digestSha256;
        try {
            digestSha256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            log.error("SHA-256 not found");
            throw new InternalServerErrorException();
        }

        final UUID fileId = UUID.randomUUID();

        UploadHelperInputStream helperStream = new UploadHelperInputStream(inputStream);
        DigestInputStream digestStream = new DigestInputStream(helperStream, digestSha256);

        ObjectWriteResponse response = null;
        try {
            log.info("uploading file with id {}", fileId);
            response = minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileId.toString())
                            .stream(digestStream, -1, PART_SIZE)
                            .build()
            );
            log.info("minio response {}", response.toString());
        } catch (Exception ex) {
            log.error("putObject failed: {}", ex.getMessage());
            throw new InternalServerErrorException();
        }

        byte[] hashBytes = digestSha256.digest();
        String hashHex = HexFormat.of().formatHex(hashBytes);

        try {
            if(helperStream.getTotalBytesCount() == 0) {
                throw new BadRequestException("empty file isn't acceptable");
            }

            FileMetadata metadata = new FileMetadata();
            metadata.setFilename(filename);
            metadata.setUploadDate(Instant.now());
            metadata.setExternalId(fileId);
            metadata.setVisibility(visibility);
            metadata.setOwnerId(userId);
            metadata.setTags(normalizeAndValidateTags(tags));
            metadata.setHash(hashHex);
            metadata.setSize(helperStream.getTotalBytesCount());
            metadata.setContentType(detectMimeType(contentType, helperStream.getHeaderBuffer()));

            FileMetadata createdFileMetadata = fileRepository.save(metadata);
            return fileMetadataMapper.toDto(createdFileMetadata);
        } catch (RuntimeException ex) {
            try {
                // remove object on minio as file creation failed
                log.info("remove file {} as upload failed", fileId.toString());
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(bucketName)
                                .object(fileId.toString())
                                .build());
            } catch (Exception ex2) {
                log.error("removeObject failed in uploadFile: {}", ex.getMessage());
            }

            if (ex instanceof DuplicateKeyException) {
                log.warn("file already exist ({}, {})", filename, hashHex);
                throw new FileDuplicatedException();
            }
        }
        throw new InternalServerErrorException();
    }

    @Override
    public void deleteFile(String userId, UUID fileId) throws StorageException {
        FileMetadata metadata = getFileMetadataWithAccessCheck(userId, fileId, false);

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileId.toString())
                            .build());
        } catch (Exception ex) {
            log.error("removeObject failed in deleteFile: {}", ex.getMessage());
            throw new InternalServerErrorException();
        }

        fileRepository.deleteById(metadata.getId());
    }

    @Override
    public FileMetadataDto renameFile(String userId, UUID fileId, String newFilename) throws StorageException {
        FileMetadata metadata = getFileMetadataWithAccessCheck(userId, fileId, false);

        if(!metadata.getFilename().equals(newFilename)) {
            metadata.setFilename(newFilename);
            try {
                fileRepository.save(metadata);
            } catch (DuplicateKeyException ex) {
                log.warn("file with the same name already exist {}", newFilename);
                throw new FileDuplicatedException();
            }
        }

        return fileMetadataMapper.toDto(metadata);
    }

    @Override
    public StoredFile getFile(String userId, UUID fileId) throws StorageException {
        FileMetadata metadata = getFileMetadataWithAccessCheck(userId, fileId, true);

        GetObjectArgs args = GetObjectArgs.builder()
                .bucket(bucketName)
                .object(fileId.toString())
                .build();

        try {
            InputStream inputStream = minioClient.getObject(args);
            return new StoredFile(fileMetadataMapper.toDto(metadata), inputStream);
        } catch (Exception ex) {
            log.error("getObject failed in getFile: {}", ex.getMessage());
            throw new InternalServerErrorException();
        }
    }

    @Override
    public Page<FileMetadataDto> listPublicFiles(Set<String> tags, Pageable pageable) {
        Page<FileMetadata> result;
        if(tags != null && !tags.isEmpty()) {
            Set<String> normalizedTags = normalizeAndValidateTags(tags);
            result = fileRepository.findAllPublicFilesByTagsIn(normalizedTags, pageable);
        } else {
            result = fileRepository.findAllPublicFiles(pageable);
        }
        return result.map(fileMetadataMapper::toDto);
    }

    @Override
    public Page<FileMetadataDto> listUserFiles(String userId, Set<String> tags, Pageable pageable) {
        Page<FileMetadata> result;
        if(tags != null && !tags.isEmpty()) {
            Set<String> normalizedTags = normalizeAndValidateTags(tags);
            result = fileRepository.findByOwnerIdAndTagsIn(userId, normalizedTags, pageable);
        } else {
            result = fileRepository.findByOwnerId(userId, pageable);
        }
        return result.map(fileMetadataMapper::toDto);
    }

    private Set<String> normalizeAndValidateTags(Set<String> tags) throws StorageException {
        if(tags.stream().anyMatch(String::isEmpty)) {
            throw new BadRequestException("tag shouldn't be empty");
        }
        Set<String> lowercaseTags = tags.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        return lowercaseTags;
    }

    private String detectMimeType(String contentTypeFromUser, byte[] fileHeader) {
        final String DEFAULT_TYPE = "application/octet-stream";
        try {
            // user provided type has priority as mentioned in req
            MimeType mimetype = MimeTypes.getDefaultMimeTypes()
                    .forName(contentTypeFromUser);
            if(!mimetype.toString().equals(DEFAULT_TYPE)) {
                return mimetype.toString();
            }
        } catch (MimeTypeException ex) {
        }

        log.warn("got unknown content type from user {}, will detect internally", contentTypeFromUser);
        String detectedType = tika.detect(fileHeader);
        if(detectedType != null && !detectedType.isEmpty()) {
            return detectedType;
        }

        log.warn("default content type will be used");
        // default, if nothing found
        return DEFAULT_TYPE;
    }

    private FileMetadata getFileMetadataWithAccessCheck(
            String userId, UUID fileId, Boolean doNotCheckAccessIfPublic) throws StorageException {
        FileMetadata fileMetadata = fileRepository.findByExternalId(fileId)
                .orElseThrow(() -> new FileNotFoundException(fileId));

        if(doNotCheckAccessIfPublic && fileMetadata.getVisibility() == Visibility.PUBLIC) {
            return fileMetadata;
        }

        if (!fileMetadata.getOwnerId().equals(userId)) {
            log.warn("attempt of unauthorized access to file {}, user {}", fileId.toString(), userId);
            throw new AccessDeniedException();
        }

        return fileMetadata;
    }
}
