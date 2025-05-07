package com.burjkhalifacorp.storage.api.controller;
import com.burjkhalifacorp.storage.api.models.FileSortBy;

import com.burjkhalifacorp.storage.common.Visibility;
import com.burjkhalifacorp.storage.errors.NotImplementedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            HttpServletRequest request,
            @RequestParam(required = true) String userId,
            @RequestParam(required = true) String filename,
            @RequestParam(required = true) Visibility visibility,
            @RequestParam(defaultValue = "") Set<String> tags
    ) throws IOException {
        InputStream inputStream = request.getInputStream();
        throw new NotImplementedException();
    }

    @GetMapping
    public ResponseEntity<?> listPublicFiles(
            @RequestParam(required = true) String userId,
            @RequestParam(defaultValue = "") Set<String> tags,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "UPLOAD_DATE") FileSortBy sortBy,
            @RequestParam(defaultValue = "true") boolean ascending
    ) {
        throw new NotImplementedException();
    }

    @GetMapping("/my")
    public ResponseEntity<?> listUserFiles(
            @RequestParam(required = true) String userId,
            @RequestParam(defaultValue = "") Set<String> tags,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "UPLOAD_DATE") FileSortBy sortBy,
            @RequestParam(defaultValue = "true") boolean ascending
    ) {
        throw new NotImplementedException();
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<?> downloadFile(
            @PathVariable UUID fileId,
            @RequestParam(required = true) String userId
    ) {
        throw new NotImplementedException();
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<?> deleteFile(
            @PathVariable UUID fileId,
            @RequestParam(required = true) String userId
    ) {
        throw new NotImplementedException();
    }


    @PatchMapping("/{fileId}")
    public ResponseEntity<?> renameFile(
            @PathVariable UUID fileId,
            @RequestParam(required = true) String userId,
            @RequestParam(required = true) String filename
    ) {
        throw new NotImplementedException();
    }
}
