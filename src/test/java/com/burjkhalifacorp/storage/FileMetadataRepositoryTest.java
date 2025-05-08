package com.burjkhalifacorp.storage;

import com.burjkhalifacorp.storage.common.Visibility;
import com.burjkhalifacorp.storage.persist.FileMetadataRepository;
import com.burjkhalifacorp.storage.persist.models.FileMetadata;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.springframework.dao.DuplicateKeyException;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Testcontainers
@DataMongoTest
@ActiveProfiles("test")
@Tag("integration")
public class FileMetadataRepositoryTest extends TestBase {
    @Autowired
    private FileMetadataRepository repository;

    @Container
    private static ComposeContainer env = new ComposeContainer(new File("docker-compose-tests.yml"))
            .withExposedService("mongo", 27017, Wait.forListeningPort());

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void testSaveAndFindByOwnerId() {
        FileMetadata file1 = mkRandomFileMetadata(userId1, Visibility.PRIVATE, tags1);
        FileMetadata file2 = mkRandomFileMetadata(userId1, Visibility.PUBLIC, tags2);
        FileMetadata file3 = mkRandomFileMetadata(userId2, Visibility.PUBLIC, tags1);

        repository.insert(file1);
        repository.insert(file2);
        repository.insert(file3);

        PageRequest pageReq = PageRequest.of(0, 20, Sort.by("uploadDate").descending());
        Page<FileMetadata> bobFiles = repository.findByOwnerId(userId1, pageReq);
        Page<FileMetadata> romanFiles = repository.findByOwnerId(userId2, pageReq);
        Page<FileMetadata> aliceFiles = repository.findByOwnerId(userId3, pageReq);

        assertEquals(2, bobFiles.getTotalElements());
        assertEquals(1, romanFiles.getTotalElements());
        assertEquals(0, aliceFiles.getTotalElements());

        Page<FileMetadata> bobFilesWithTag = repository.findByOwnerIdAndTagsIn(userId1, Set.of("scuba"), pageReq);
        assertEquals(1, bobFilesWithTag.getTotalElements());
    }

    @Test
    void testSaveAndFindPublic() {
        FileMetadata file1 = mkRandomFileMetadata(userId1, Visibility.PUBLIC, tags1);
        FileMetadata file2 = mkRandomFileMetadata(userId1, Visibility.PUBLIC, tags2);
        FileMetadata file3 = mkRandomFileMetadata(userId2, Visibility.PUBLIC, tags1);
        FileMetadata file4 = mkRandomFileMetadata(userId2, Visibility.PUBLIC, tags1);
        FileMetadata file_private = mkRandomFileMetadata(userId2, Visibility.PRIVATE, tags1);

        repository.insert(file1);
        repository.insert(file2);
        repository.insert(file3);
        repository.insert(file4);
        repository.insert(file_private);

        PageRequest pageReq = PageRequest.of(0, 20, Sort.by("uploadDate").descending());
        Page<FileMetadata> allFiles = repository.findAllPublicFiles(pageReq);
        assertEquals(4, allFiles.getTotalElements());

        Page<FileMetadata> tags1Files = repository.findAllPublicFilesByTagsIn(Set.of(tags1.iterator().next()), pageReq);
        assertEquals(3, tags1Files.getTotalElements());

        Page<FileMetadata> tags2Files = repository.findAllPublicFilesByTagsIn(Set.of(tags2.iterator().next()), pageReq);
        assertEquals(1, tags2Files.getTotalElements());

        Page<FileMetadata> tags1and2Files = repository.findAllPublicFilesByTagsIn(
                Set.of(tags1.iterator().next(), tags2.iterator().next()), pageReq);
        assertEquals(4, tags1and2Files.getTotalElements());
    }

    @Test
    void testSaveSameName() {
        FileMetadata file1 = mkRandomFileMetadata(userId1, Visibility.PUBLIC, tags1);
        FileMetadata file2 = mkRandomFileMetadata(userId1, Visibility.PUBLIC, tags2);
        // another user
        FileMetadata file3 = mkRandomFileMetadata(userId2, Visibility.PUBLIC, tags2);

        file2.setFilename(file1.getFilename());
        file3.setFilename(file1.getFilename());

        repository.insert(file1);
        assertThrows(DuplicateKeyException.class, () -> {
            repository.insert(file2);
        });
        assertDoesNotThrow(() -> {
            repository.insert(file3);
        });
    }

    @Test
    void testSaveSameHash() {
        FileMetadata file1 = mkRandomFileMetadata(userId1, Visibility.PUBLIC, tags1);
        FileMetadata file2 = mkRandomFileMetadata(userId1, Visibility.PUBLIC, tags2);
        // another user
        FileMetadata file3 = mkRandomFileMetadata(userId2, Visibility.PUBLIC, tags2);

        file2.setHash(file1.getHash());
        file3.setHash(file1.getHash());

        repository.insert(file1);
        assertThrows(DuplicateKeyException.class, () -> {
            repository.insert(file2);
        });
        assertDoesNotThrow(() -> {
            repository.insert(file3);
        });
    }

    @Test
    void testTagSorting() {
        FileMetadata file1 = mkRandomFileMetadata(userId1, Visibility.PUBLIC, Set.of("tag_1", "tag_3"));
        FileMetadata file2 = mkRandomFileMetadata(userId1, Visibility.PUBLIC, Set.of("tag_2"));
        FileMetadata file3 = mkRandomFileMetadata(userId1, Visibility.PUBLIC, Set.of("tag_3", "tag_1"));
        repository.insert(file1);
        repository.insert(file2);
        repository.insert(file3);

        PageRequest pageReqDesc = PageRequest.of(0, 20, Sort.by("tags").descending());
        Page<FileMetadata> resultDesc = repository.findByOwnerId(userId1, pageReqDesc);
        assertEquals(file3, resultDesc.iterator().next());

        PageRequest pageReqAsc = PageRequest.of(0, 20, Sort.by("tags").ascending());
        Page<FileMetadata> resultAsc = repository.findByOwnerId(userId1, pageReqAsc);
        assertEquals(file1, resultAsc.iterator().next());
    }
}
