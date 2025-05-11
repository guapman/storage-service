package com.burjkhalifacorp.storage;

import com.burjkhalifacorp.storage.api.models.ErrorResponse;
import com.burjkhalifacorp.storage.api.models.FileMetadataDto;
import com.burjkhalifacorp.storage.persist.FileMetadataRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Tag("integration")
public class StorageServiceIntegrationTest extends TestBase {
    @LocalServerPort
    private int port;

    @Container
    private static ComposeContainer env = new ComposeContainer(new File("docker-compose-tests.yml"))
            .withExposedService("mongo", 27017, Wait.forListeningPort())
            .withExposedService("minio", 9000, Wait.forListeningPort());

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Autowired
    private FileMetadataRepository repository;

    @Test
    void shouldBeAbleUpload2GbFile() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        final long size = 2L * 1024 * 1024 * 1024;

        final String fileName = "huge_file.dat";

        HttpRequest request = mkFileUploadRequest(userId1, fileName, size, (byte) random.nextInt(256));
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        FileMetadataDto metadata = objectMapper.readValue(response.body(), FileMetadataDto.class);
        assertEquals(size, metadata.getSize());
    }

    @Test
    void shouldCreateSingleFileWhenParallelUploadsWithSameName() throws Exception {
        final int PARALLEL_NUM = 20;
        Stream<Boolean> results = runParallelUploads(userId1, PARALLEL_NUM, true, false);
        assertEquals(1, results.filter(Boolean::booleanValue).count());
    }

    @Test
    void shouldCreateSingleFileWhenParallelUploadsWithSameContent() throws Exception {
        final int PARALLEL_NUM = 20;
        Stream<Boolean> results = runParallelUploads(userId1, PARALLEL_NUM, false, true);
        assertEquals(1, results.filter(Boolean::booleanValue).count());
    }

    @RepeatedTest(5)
    void shouldBeAbleToDownloadFile() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        final long size = random.nextLong(8192) + 2048;
        final String fileName = "file_for_download.dat";

        final byte byteContent = (byte) random.nextInt(256);
        HttpRequest request = mkFileUploadRequest(userId1, fileName, size, byteContent);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        FileMetadataDto metadata = objectMapper.readValue(response.body(), FileMetadataDto.class);
        assertEquals(size, metadata.getSize());

        HttpRequest requestDownload = mkFileDownloadRequest(userId1, metadata.getId());
        HttpResponse<byte[]> responseDownload = client.send(requestDownload, HttpResponse.BodyHandlers.ofByteArray());

        assertEquals(200, responseDownload.statusCode());
        byte [] dataDownloaded = responseDownload.body();
        assertEquals(size, dataDownloaded.length);
        for(int i = 0; i < dataDownloaded.length; ++i) {
            assertEquals(byteContent, dataDownloaded[i]);
        }
    }

    @Test
    void shouldReturnErrorWhenDeleteOtherUserFile() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        final long size = random.nextLong(8192) + 2048;
        final String fileName = "file_for_deletion.dat";

        final byte byteContent = (byte) random.nextInt(256);
        HttpRequest request = mkFileUploadRequest(userId1, fileName, size, byteContent);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        FileMetadataDto metadata = objectMapper.readValue(response.body(), FileMetadataDto.class);
        assertEquals(size, metadata.getSize());

        HttpRequest requestDelete = mkFileDeleteRequest(hackerId, metadata.getId());
        HttpResponse<String> responseDelete = client.send(requestDelete, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpStatus.UNAUTHORIZED.value(), responseDelete.statusCode());
        ErrorResponse errorResponse = objectMapper.readValue(responseDelete.body(), ErrorResponse.class);
        assertFalse(errorResponse.getError().isEmpty());
    }

    @Test
    void shouldBeAbleListPublicFiles() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        final int FILES_NUM = 10;
        final long size = random.nextLong(8192) + 2048;

        Stream<Boolean> results = runParallelUploads(userId1, FILES_NUM, false, false);
        assertEquals(FILES_NUM, results.filter(Boolean::booleanValue).count());

        HttpRequest request = mkFileListPublicRequest(userId1);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JsonNode node = objectMapper.readTree(response.body());
        assertEquals(FILES_NUM,node.get("page").get("totalElements").asInt());
    }

    @Test
    void shouldBeAbleListUserFiles() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        final int FILES_NUM = 10;
        final long size = random.nextLong(8192) + 2048;

        Stream<Boolean> results = runParallelUploads(userId1, FILES_NUM, false, false);
        assertEquals(FILES_NUM, results.filter(Boolean::booleanValue).count());

        HttpRequest request = mkFileListUserRequest(userId1);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JsonNode node = objectMapper.readTree(response.body());
        assertEquals(FILES_NUM, node.get("page").get("totalElements").asInt());
    }

    @Test
    void shouldBeAbleRenameFiles() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        final int FILES_NUM = 10;
        final long size = random.nextLong(8192) + 2048;
        final String filename = "file_to_rename.dat";
        final String filenameNew = "file_new_name.zip";

        HttpRequest request = mkFileUploadRequest(userId1, filename, size, (byte) random.nextInt(256));
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        FileMetadataDto metadata = objectMapper.readValue(response.body(), FileMetadataDto.class);

        HttpRequest requestRename = mkFileRenameRequest(userId1, metadata.getId(), filenameNew);
        HttpResponse<String> responseRename = client.send(requestRename, HttpResponse.BodyHandlers.ofString());

        FileMetadataDto metadataAfterRename = objectMapper.readValue(responseRename.body(), FileMetadataDto.class);
        assertEquals(filenameNew, metadataAfterRename.getFilename());
    }

    private URI getEndpointUri(String path) {
        return URI.create("http://localhost:" + port + "/api/v1/files" + path);
    }

    private HttpRequest mkFileUploadRequest(String userId, String filename, long contentSize, byte content) {
        InputStream fakeFileStream = new InputStream() {
            private long bytesLeft = contentSize;
            @Override
            public int read() {
                if (bytesLeft-- <= 0) return -1;
                return content;
            }
        };
        return HttpRequest.newBuilder()
                .uri(getEndpointUri("/upload?userId=%s&filename=%s&visibility=PUBLIC".formatted(userId, filename)))
                .header("Content-Type", "application/octet-stream")
                .POST(HttpRequest.BodyPublishers.ofInputStream(() -> fakeFileStream))
                .build();
    }

    private HttpRequest mkFileDownloadRequest(String userId, UUID fileId) {
        return HttpRequest.newBuilder()
                .uri(getEndpointUri("/%s?userId=%s".formatted(fileId.toString(), userId)))
                .GET().build();
    }

    private HttpRequest mkFileDeleteRequest(String userId, UUID fileId) {
        return HttpRequest.newBuilder()
                .uri(getEndpointUri("/%s?userId=%s".formatted(fileId.toString(), userId)))
                .DELETE().build();
    }

    private HttpRequest mkFileListPublicRequest(String userId) {
        return HttpRequest.newBuilder()
                .uri(getEndpointUri("/public?userId=%s".formatted(userId)))
                .GET().build();
    }

    private HttpRequest mkFileListUserRequest(String userId) {
        return HttpRequest.newBuilder()
                .uri(getEndpointUri("/my?userId=%s".formatted(userId)))
                .GET().build();
    }

    private HttpRequest mkFileRenameRequest(String userId, UUID fileId, String newName) {
        return HttpRequest.newBuilder()
                .uri(getEndpointUri("/%s?userId=%s&filename=%s".formatted(fileId.toString(), userId, newName)))
                .method("PATCH", HttpRequest.BodyPublishers.noBody() ).build();
    }

    Stream<Boolean> runParallelUploads(String userId, int parallelNum, boolean sameName, boolean sameContent) {
        ExecutorService executorService = Executors.newFixedThreadPool(parallelNum);
        List<Future<Boolean>> futuresList = new ArrayList<>();
        for(int i = 0; i < parallelNum; ++i) {
            final String filename = sameName ? "file_same_name.dat" : "file_%d.dat".formatted(i);
            final byte content = sameContent ? 0x55 : (byte) i;

            Callable<Boolean> uploadTask = () -> {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = mkFileUploadRequest(userId, filename,512, content);
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                return response.statusCode() == 200;
            };
            futuresList.add(executorService.submit(uploadTask));
        }
        Stream<Boolean> finishedOk = futuresList.stream().map(f -> { try {
            return f.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("upload future failed");
        }});

        return finishedOk;
    }
}
