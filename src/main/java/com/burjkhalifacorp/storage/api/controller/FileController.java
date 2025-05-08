package com.burjkhalifacorp.storage.api.controller;
import com.burjkhalifacorp.storage.api.models.FileMetadataDto;
import com.burjkhalifacorp.storage.api.models.FileSortBy;

import com.burjkhalifacorp.storage.common.AppConstants;
import com.burjkhalifacorp.storage.common.Visibility;
import com.burjkhalifacorp.storage.errors.BadRequestException;
import com.burjkhalifacorp.storage.service.FileService;
import com.burjkhalifacorp.storage.service.models.StoredFile;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

@RestController
@Slf4j
@Validated
@RequestMapping("/api/v1/files")
public class FileController {
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public ResponseEntity<FileMetadataDto> uploadFile(
            HttpServletRequest request,
            @RequestParam(required = true) String userId,
            @RequestParam(required = true) String filename,
            @RequestParam(required = true) Visibility visibility,
            @Size(max = AppConstants.MAX_TAGS, message = "Max {max} tags allowed")
            @RequestParam(defaultValue = "") Set<String> tags
    ) throws IOException {
        // storing of empty files meaningless
        if(request.getContentLengthLong() == 0) {
            throw new BadRequestException("empty file isn't acceptable");
        }

        final String contentType = request.getContentType();
        final InputStream inputStream = request.getInputStream();
        FileMetadataDto fileMetadataDto = fileService.upload(
                userId, filename, contentType, visibility, tags, inputStream);
        return ResponseEntity.ok(fileMetadataDto);
    }

    @GetMapping("/")
    public ResponseEntity<Page<FileMetadataDto>> listPublicFiles(
            @RequestParam(required = true) String userId,
            @RequestParam(defaultValue = "") Set<String> tags,
            @RequestParam(defaultValue = "0") int page,
            @Max(value = AppConstants.MAX_FILES_PAGE_SIZE, message = "Max {value} page size allowed")
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "UPLOAD_DATE") FileSortBy sortBy,
            @RequestParam(defaultValue = "true") boolean ascending
    ) {
        Sort sort = Sort.by(sortBy.getDbField());
        Pageable pageable = PageRequest.of(page, size, ascending ? sort.ascending() : sort.descending());
        Page<FileMetadataDto> pageOfFiles = fileService.listPublicFiles(tags, pageable);
        return ResponseEntity.ok(pageOfFiles);
    }

    @GetMapping("/my")
    public ResponseEntity<Page<FileMetadataDto>> listUserFiles(
            @RequestParam(required = true) String userId,
            @RequestParam(defaultValue = "") Set<String> tags,
            @RequestParam(defaultValue = "0") int page,
            @Max(value = AppConstants.MAX_FILES_PAGE_SIZE, message = "Max {value} page size allowed")
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "UPLOAD_DATE") FileSortBy sortBy,
            @RequestParam(defaultValue = "true") boolean ascending
    ) {
        Sort sort = Sort.by(sortBy.getDbField());
        Pageable pageable = PageRequest.of(page, size, ascending ? sort.ascending() : sort.descending());
        Page<FileMetadataDto> pageOfFiles = fileService.listUserFiles(userId, tags, pageable);
        return ResponseEntity.ok(pageOfFiles);
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<StreamingResponseBody> downloadFile(
            @PathVariable UUID fileId,
            @RequestParam(required = true) String userId
    ) {
        final StoredFile storedFile = fileService.getFile(userId, fileId);

        StreamingResponseBody responseBody = outputStream -> {
            try(InputStream fileStream = storedFile.getInputStream()) {
                final int BUFFER_SIZE = 16 * 1024;
                byte[] buffer = new byte[BUFFER_SIZE];
                int byteCount;
                while ((byteCount = fileStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, byteCount);
                }
            } catch (Exception ex) {
                log.error("error occured during download: {}", ex);
                throw ex;
            }
        };

        FileMetadataDto fileMetadataDto = storedFile.getMetadata();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + fileMetadataDto.getFilename())
                .header(HttpHeaders.CONTENT_TYPE, fileMetadataDto.getContentType())
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileMetadataDto.getSize()))
                .body(responseBody);
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable UUID fileId,
            @RequestParam(required = true) String userId
    ) {
        fileService.deleteFile(userId, fileId);
        return ResponseEntity.ok().build();
    }


    @PatchMapping("/{fileId}")
    public ResponseEntity<FileMetadataDto> renameFile(
            @PathVariable UUID fileId,
            @RequestParam(required = true) String userId,
            @RequestParam(required = true) String filename
    ) {
        FileMetadataDto updateMetadata = fileService.renameFile(userId, fileId, filename);
        return ResponseEntity.ok(updateMetadata);
    }
}
