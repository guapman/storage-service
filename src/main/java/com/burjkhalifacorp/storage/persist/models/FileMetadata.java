package com.burjkhalifacorp.storage.persist.models;

import com.burjkhalifacorp.storage.common.Visibility;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

@Getter
@Setter
@Document(collection = "files_metadata")
@CompoundIndex(name = "owner_id_filename_unique_idx", def = "{'ownerId': 1, 'filename': 1}", unique = true)
@CompoundIndex(name = "owner_id_hash_unique_idx", def = "{'ownerId': 1, 'hash': 1}", unique = true)
public class FileMetadata {
    @Id
    private String id;

    @Indexed(unique = true)
    private UUID externalId;

    private String ownerId;

    @NotEmpty(message = "filename must not be empty")
    private String filename;

    private String hash;

    @Indexed
    private Set<String> tags = new TreeSet<>();

    private long size;

    private Visibility visibility;

    @NotEmpty(message = "contentType must not be empty")
    private String contentType;

    private LocalDateTime uploadDate;
}
