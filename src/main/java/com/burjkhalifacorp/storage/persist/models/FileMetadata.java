package com.burjkhalifacorp.storage.persist.models;

import com.burjkhalifacorp.storage.common.Visibility;
import jakarta.validation.constraints.NotEmpty;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

@Document(collection = "files_metadata")
@CompoundIndex(name = "owner_id_filename_unique_idx", def = "{'ownerId': 1, 'filename': 1}", unique = true)
@CompoundIndex(name = "owner_id_hash_unique_idx", def = "{'ownerId': 1, 'hash': 1}", unique = true)
@CompoundIndex(name = "tags_visibility_idx", def = "{'tags': 1, 'visibility': 1}")
@Data
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
    private List<String> tags;

    private long size;

    private Visibility visibility;

    @NotEmpty(message = "contentType must not be empty")
    private String contentType;

    private Instant uploadDate;

    public void setTags(Set<String> tags) {
        this.tags = new ArrayList<>(new TreeSet<>(tags)); // sort tags
    }
}
