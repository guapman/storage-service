package com.burjkhalifacorp.storage;

import com.burjkhalifacorp.storage.common.Visibility;
import com.burjkhalifacorp.storage.persist.models.FileMetadata;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public abstract class TestBase {
    protected final String userId1 = "Bob";
    protected final String userId2 = "Roman";
    protected final String userId3 = "Alice";
    protected final String hackerId = "DarthVader";
    protected final Set<String> tags1 = Set.of("scuba", "ocean", "movie");
    protected final Set<String> tags2 = Set.of("photo");


    protected FileMetadata mkRandomFileMetadata(
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
        file.setFilename("file_%d.dat".formatted(random.nextInt(Integer.MAX_VALUE)));
        file.setHash(hashHex);
        file.setSize(random.nextLong(Long.MAX_VALUE));
        file.setContentType("application/octet-stream");
        file.setVisibility(visibility);
        file.setOwnerId(ownerId);
        file.setTags(tags);
        file.setUploadDate(getCurrentTime());
        return file;
    }

    protected Instant getCurrentTime() {
        // Truncated because mongo do not store ns and after read it will be slightly different
        return Instant.now().truncatedTo(ChronoUnit.MILLIS);
    }
}
