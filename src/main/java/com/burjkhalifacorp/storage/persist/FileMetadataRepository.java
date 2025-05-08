package com.burjkhalifacorp.storage.persist;

import com.burjkhalifacorp.storage.persist.models.FileMetadata;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface FileMetadataRepository extends MongoRepository<FileMetadata, String> {
    Page<FileMetadata> findByOwnerId(String ownerId, Pageable pageable);

    Page<FileMetadata> findByOwnerIdAndTagsIn(String ownerId, Set<String> tags, Pageable pageable);

    Optional<FileMetadata> findByExternalId(UUID externalFileId);

    @Query("{ 'visibility': 'PUBLIC' }")
    Page<FileMetadata> findAllPublicFiles(Pageable pageable);

    @Query("{ 'tags': { $in: ?0 }, 'visibility': 'PUBLIC' }")
    Page<FileMetadata> findAllPublicFilesByTagsIn(Set<String> tags, Pageable pageable);
}
