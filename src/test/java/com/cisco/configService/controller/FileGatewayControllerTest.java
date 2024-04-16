package com.cisco.configService.controller;

import com.cisco.configService.service.FileGatewayService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
public class FileGatewayControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    FileGatewayService fileGatewayService;

    private static final Logger logger =
            LogManager.getLogger(FileGatewayControllerTest.class);

    @Test
    @DisplayName("Test Upload file")
    void testUploadFile() throws Exception {

        MockMultipartFile jsonFile = new MockMultipartFile("test.json", "", "application/json",
                "{\"key1\": \"value1\",\"key2\": \"value2\"}".getBytes());

        Mockito.doReturn("bucket#filename").when(fileGatewayService).upload(Mockito.any(), Mockito.any(),
                Mockito.any());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/file-gateway")
                        .file("file", jsonFile.getBytes())
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Test Download file")
    void testDownloadFile() throws Exception {

        Mockito.doReturn(null).when(fileGatewayService).downloadFile(Mockito.any(),
                Mockito.any());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/file-gateway")
                        .param("fileName", "test.json")
                        .contentType(MediaType.TEXT_PLAIN_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Test get plan file path")
    void testGetPlanFilePath() throws Exception {

        Mockito.doReturn(new ArrayList<>()).when(fileGatewayService).getPlanFilePath(Mockito.any(),
                Mockito.any(), Mockito.any());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/file-gateway/plan-file-path")
                        .param("networkName", "test")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful());
    }
}


