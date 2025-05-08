package com.burjkhalifacorp.storage;

import com.burjkhalifacorp.storage.api.models.FileMetadataDto;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.stream.Stream;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Tag("integration")
public class StorageServiceIntegrationTest extends TestBase {
    @LocalServerPort
    private int port;

    private ObjectMapper objectMapper;
    private Random random = new Random();

    @Container
    private static ComposeContainer env = new ComposeContainer(new File("docker-compose-tests.yml"))
            .withExposedService("mongo", 27017, Wait.forListeningPort())
            .withExposedService("minio", 9000, Wait.forListeningPort());

    public StorageServiceIntegrationTest() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testUploadHugeFile() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        final long size = 2L * 1024 * 1024 * 1024;

        final String userId = "1";
        final String fileName = "huge_file.dat";

        HttpRequest request = mkFileUploadRequest(userId, fileName, size, random.nextInt(256));
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        FileMetadataDto metadata = objectMapper.readValue(response.body(), FileMetadataDto.class);
        assertEquals(size, metadata.getSize());
    }

    @Test
    void testParallelUploadWithSameName() throws Exception {
        final String userId = "2";
        final int PARALLEL_NUM = 20;
        Stream<Boolean> results = runParallelUploads(userId, PARALLEL_NUM, false);
        assertEquals(1, results.filter(Boolean::booleanValue).count());
    }

    @Test
    void testParallelUploadWithSameContent() throws Exception {
        final String userId = "3";
        final int PARALLEL_NUM = 20;
        Stream<Boolean> results = runParallelUploads(userId, PARALLEL_NUM, true);
        assertEquals(1, results.filter(Boolean::booleanValue).count());
    }

    private URI getEndpointUri(String path) {
        return URI.create("http://localhost:" + port + "/api/v1/files" + path);
    }

    private HttpRequest mkFileUploadRequest(String userId, String filename, long contentSize, int content) {
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

    Stream<Boolean> runParallelUploads(String userId, int parallelNum, boolean randomizeByName) {
        ExecutorService executorService = Executors.newFixedThreadPool(parallelNum);
        List<Future<Boolean>> futuresList = new ArrayList<>();
        for(int i = 0; i < parallelNum; ++i) {
            String filename;
            int content;
            if(randomizeByName) {
                filename = "file_%d.dat".formatted(i);
                content = 0x55;
            } else {
                filename = "file_same_name.dat";
                content = i;
            }

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
