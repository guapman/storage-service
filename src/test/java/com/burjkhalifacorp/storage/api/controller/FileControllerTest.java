package com.burjkhalifacorp.storage.api.controller;

import com.burjkhalifacorp.storage.TestBase;
import com.burjkhalifacorp.storage.api.models.FileMetadataDto;
import com.burjkhalifacorp.storage.common.Visibility;
import com.burjkhalifacorp.storage.config.DownloadConfig;
import com.burjkhalifacorp.storage.mappers.FileMetadataMapper;
import com.burjkhalifacorp.storage.persist.models.FileMetadata;
import com.burjkhalifacorp.storage.service.FileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = FileController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({FileMetadataMapper.class, DownloadConfig.class})
public class FileControllerTest extends TestBase {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileService fileService;

    @Autowired
    FileMetadataMapper fileMetadataMapper;

    @Test
    void shouldListPublicFiles() throws Exception {
        List<FileMetadataDto> files = IntStream.range(0, 10)
                .mapToObj(i -> fileMetadataMapper.toDto(mkRandomFileMetadata(userId1, Visibility.PUBLIC, tags1)))
                .toList();
        doReturn(new PageImpl<>(files))
                .when(fileService)
                .listPublicFiles(any(), any());

        mockMvc.perform(get("/api/v1/files/public").queryParam("userId", userId1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(10));
    }

    @Test
    void shouldListPrivateFiles() throws Exception {
        List<FileMetadataDto> files = IntStream.range(0, 10)
                .mapToObj(i -> fileMetadataMapper.toDto(mkRandomFileMetadata(userId1, Visibility.PRIVATE, tags1)))
                .toList();
        doReturn(new PageImpl<>(files))
                .when(fileService)
                .listUserFiles(eq(userId1), any(), any());

        mockMvc.perform(get("/api/v1/files/my").queryParam("userId", userId1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(10));
    }

    @Test
    void shouldRenameFile() throws Exception {
        FileMetadataDto fileDto = fileMetadataMapper.toDto(
                mkRandomFileMetadata(userId1, Visibility.PRIVATE, tags1));

        doReturn(fileDto)
                .when(fileService)
                .renameFile(eq(userId1), any(), any());

        ResultActions response = mockMvc.perform(patch("/api/v1/files/{id}", fileDto.getId())
                .queryParam("userId", userId1).queryParam("filename", "new_name"));
        MvcResult result = response.andExpect(status().isOk()).andReturn();
        String jsonString = result.getResponse().getContentAsString();
        FileMetadataDto fileDtoResponse = objectMapper.readValue(jsonString, FileMetadataDto.class);
        assertEquals(fileDto, fileDtoResponse);
    }

    @Test
    void shouldDeleteFile() throws Exception {
        FileMetadata file = mkRandomFileMetadata(userId1, Visibility.PRIVATE, tags1);
        mockMvc.perform(delete("/api/v1/files/{id}", file.getExternalId())
                .queryParam("userId", userId1))
                .andExpect(status().isOk());
    }
}
