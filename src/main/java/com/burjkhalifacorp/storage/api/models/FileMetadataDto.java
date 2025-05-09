package com.burjkhalifacorp.storage.api.models;

import com.burjkhalifacorp.storage.common.Visibility;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class FileMetadataDto {
    private UUID id;
    private String filename;
    private List<String> tags;
    private long size;
    private Visibility visibility;
    private String contentType;
    private Instant uploadDate;
}
