package com.burjkhalifacorp.storage.api.controller;
import com.burjkhalifacorp.storage.api.models.ErrorResponse;
import com.burjkhalifacorp.storage.api.models.FileMetadataDto;
import com.burjkhalifacorp.storage.api.models.FileSortBy;

import com.burjkhalifacorp.storage.common.AppConstants;
import com.burjkhalifacorp.storage.common.Visibility;
import com.burjkhalifacorp.storage.errors.BadRequestException;
import com.burjkhalifacorp.storage.service.FileService;
import com.burjkhalifacorp.storage.service.models.StoredFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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

@Tag(name = "Storage Service")
@RestController
@Slf4j
@Validated
@RequestMapping("/api/v1/files")
public class FileController {
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @Operation(
            summary = "Upload file to storage as raw binary stream",
            requestBody = @RequestBody(
                    content = @Content(
                            mediaType = "application/octet-stream",
                            schema = @Schema(type = "string", format = "binary")
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(
                            description = "API Error",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    )
            })
    @PostMapping("/upload")
    public ResponseEntity<FileMetadataDto> uploadFile(
            HttpServletRequest request,
            @RequestParam @NotBlank String userId,
            @RequestParam @NotBlank String filename,
            @RequestParam Visibility visibility,
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

    @Operation(
            summary = "List all public files",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(
                            description = "API Error",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    )
            })
    @GetMapping("/public")
    public ResponseEntity<Page<FileMetadataDto>> listPublicFiles(
            @RequestParam @NotBlank String userId,
            @RequestParam(defaultValue = "") Set<String> tags,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Min(1) @Max(value = AppConstants.MAX_FILES_PAGE_SIZE, message = "Max {value} page size allowed")
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "UPLOAD_DATE") FileSortBy sortBy,
            @RequestParam(defaultValue = "true") boolean ascending
    ) {
        Sort sort = Sort.by(sortBy.getDbField());
        Pageable pageable = PageRequest.of(page, size, ascending ? sort.ascending() : sort.descending());
        Page<FileMetadataDto> pageOfFiles = fileService.listPublicFiles(tags, pageable);
        return ResponseEntity.ok(pageOfFiles);
    }

    @Operation(
            summary = "List all files uploaded by user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(
                            description = "API Error",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    )
            })
    @GetMapping("/my")
    public ResponseEntity<Page<FileMetadataDto>> listUserFiles(
            @RequestParam @NotBlank String userId,
            @RequestParam(defaultValue = "") Set<String> tags,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Min(1) @Max(value = AppConstants.MAX_FILES_PAGE_SIZE, message = "Max {value} page size allowed")
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "UPLOAD_DATE") FileSortBy sortBy,
            @RequestParam(defaultValue = "true") boolean ascending
    ) {
        Sort sort = Sort.by(sortBy.getDbField());
        Pageable pageable = PageRequest.of(page, size, ascending ? sort.ascending() : sort.descending());
        Page<FileMetadataDto> pageOfFiles = fileService.listUserFiles(userId, tags, pageable);
        return ResponseEntity.ok(pageOfFiles);
    }

    @Operation(
            summary = "Download file from storage",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(
                            description = "API Error",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    )
            })
    @GetMapping("/{fileId}")
    public ResponseEntity<StreamingResponseBody> downloadFile(
            @PathVariable UUID fileId,
            @RequestParam @NotBlank String userId
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
                log.error("error occurred during download: {}", ex);
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

    @Operation(
            summary = "Delete file from storage",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(
                            description = "API Error",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    )
            })
    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable UUID fileId,
            @RequestParam @NotBlank String userId
    ) {
        fileService.deleteFile(userId, fileId);
        return ResponseEntity.ok().build();
    }


    @Operation(
            summary = "Rename file",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(
                            description = "API Error",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    )
            })
    @PatchMapping("/{fileId}")
    public ResponseEntity<FileMetadataDto> renameFile(
            @PathVariable UUID fileId,
            @RequestParam @NotBlank String userId,
            @RequestParam @NotBlank String filename
    ) {
        FileMetadataDto updateMetadata = fileService.renameFile(userId, fileId, filename);
        return ResponseEntity.ok(updateMetadata);
    }
}
