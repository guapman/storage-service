package com.burjkhalifacorp.storage;

import com.burjkhalifacorp.storage.common.Visibility;
import com.burjkhalifacorp.storage.persist.FileMetadataRepository;
import com.burjkhalifacorp.storage.persist.models.FileMetadata;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.ComposeContainer;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.wait.strategy.Wait;
import org.springframework.dao.DuplicateKeyException;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.security.MessageDigest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@ActiveProfiles("test")
@Tag("integration")
public class FileMetadataRepositoryTest {

    private final String userId1 = "bob";
    private final String userId2 = "roman";
    private final String userId3 = "alice";
    private final Set<String> tags1 = Set.of("movie", "scuba", "ocean");
    private final Set<String> tags2 = Set.of("photo");

    @Autowired
    private FileMetadataRepository repository;

    private static ComposeContainer env;

    @BeforeAll
    static void setUpEnv() {
        env = new ComposeContainer(new File("docker-compose-tests.yml"))
                .withExposedService("mongo", 27017, Wait.forListeningPort());
        env.start();
    }

    @AfterAll
    static void tearDown() {
        env.stop();
    }

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    private FileMetadata mkRandFile(
            String ownerId,
            Visibility visibility,
            Set<String> tags
    ) {
        Random random = new Random();

        byte[] randomBytes = new byte[32];
        random.nextBytes(randomBytes);
        MessageDigest digest;

        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            return null;
        }

        byte[] hashBytes = digest.digest(randomBytes);
        String hashHex = HexFormat.of().formatHex(hashBytes);

        FileMetadata file = new FileMetadata();
        file.setExternalId(UUID.randomUUID());
        file.setFilename("file_%d.dat".formatted(random.nextInt()));
        file.setHash(hashHex);
        file.setSize(random.nextInt(10000));
        file.setContentType("application/octet-stream");
        file.setVisibility(visibility);
        file.setOwnerId(ownerId);
        file.setTags(tags);
        file.setUploadDate(LocalDateTime.now());
        return file;
    }

    @Test
    void testSaveAndFindByOwnerId() {
        FileMetadata file1 = mkRandFile(userId1, Visibility.PRIVATE, tags1);
        FileMetadata file2 = mkRandFile(userId1, Visibility.PUBLIC, tags2);
        FileMetadata file3 = mkRandFile(userId2, Visibility.PUBLIC, tags1);

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
        FileMetadata file1 = mkRandFile(userId1, Visibility.PUBLIC, tags1);
        FileMetadata file2 = mkRandFile(userId1, Visibility.PUBLIC, tags2);
        FileMetadata file3 = mkRandFile(userId2, Visibility.PUBLIC, tags1);
        FileMetadata file4 = mkRandFile(userId2, Visibility.PUBLIC, tags1);
        FileMetadata file_private = mkRandFile(userId2, Visibility.PRIVATE, tags1);

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

    @Disabled
    @Test
    void testSaveSameName() {
        FileMetadata file1 = mkRandFile(userId1, Visibility.PUBLIC, tags1);
        FileMetadata file2 = mkRandFile(userId1, Visibility.PUBLIC, tags2);
        // another user
        FileMetadata file3 = mkRandFile(userId2, Visibility.PUBLIC, tags2);

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

    @Disabled
    @Test
    void testSaveSameHash() {
        FileMetadata file1 = mkRandFile(userId1, Visibility.PUBLIC, tags1);
        FileMetadata file2 = mkRandFile(userId1, Visibility.PUBLIC, tags2);
        // another user
        FileMetadata file3 = mkRandFile(userId2, Visibility.PUBLIC, tags2);

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
}
