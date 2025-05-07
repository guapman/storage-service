package com.burjkhalifacorp.storage;

import com.burjkhalifacorp.storage.persist.FileMetadataRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {MongoAutoConfiguration.class})
@Tag("integration")
class StorageApplicationTests {
	@MockitoBean
	private FileMetadataRepository repository;

	@Test
	void contextLoads() {
	}

}
