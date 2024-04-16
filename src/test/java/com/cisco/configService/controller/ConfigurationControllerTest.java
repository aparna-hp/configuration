package com.cisco.configService.controller;

import com.cisco.configService.entity.ImportHistory;
import com.cisco.configService.entity.Network;
import com.cisco.configService.model.AllConfigurations;
import com.cisco.configService.service.ConfigurationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.assertj.core.util.Lists;
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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@SpringBootTest
@AutoConfigureMockMvc
public class ConfigurationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ConfigurationService configurationService;

    private static final Logger logger =
            LogManager.getLogger(ConfigurationControllerTest.class);

    @Test
    @DisplayName("Test migrate config")
    void testMigrateConfig() throws Exception {

        MockMultipartFile jsonFile = new MockMultipartFile("test.json", "", "application/json",
                "{\"key1\": \"value1\",\"key2\": \"value2\"}".getBytes());

        Mockito.doReturn("").when(configurationService).migrateConfigurations(Mockito.any(), Mockito.anyBoolean());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/configurations/import/wae")
                        .file("file", jsonFile.getBytes())
                        .param("override", "true")
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Test import config")
    void testImport() throws Exception {

        MockMultipartFile jsonFile = new MockMultipartFile("test.json", "", "application/json",
                "{\"key1\": \"value1\",\"key2\": \"value2\"}".getBytes());

        Mockito.doReturn("").when(configurationService).migrateConfigurations(Mockito.any(), Mockito.anyBoolean());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/configurations/import")
                        .file("file", jsonFile.getBytes())
                        .param("override", "true")
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Test import cp config file")
    void testImportCpFile() throws Exception {

        MockMultipartFile jsonFile = new MockMultipartFile("test.json", "", "application/json",
                "{\"key1\": \"value1\",\"key2\": \"value2\"}".getBytes());

        Mockito.doReturn("").when(configurationService).migrateConfigurations(Mockito.any(), Mockito.anyBoolean());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/configurations/import/cp/file")
                        .file("file", jsonFile.getBytes())
                        .param("override", "true")
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Test export configurations")
    void testExportConfig() throws Exception {

        Mockito.doReturn("").when(configurationService).exportConfig();

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/configurations")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }


    @Test
    @DisplayName("Test import configurations")
    void testImportConfigurationController() throws Exception {

        Mockito.doReturn("").when(configurationService).importCPConfigurations(Mockito.any(AllConfigurations.class), Mockito.any());

        ObjectMapper mapper = new ObjectMapper();
        String requestJson = mapper.writeValueAsString(new AllConfigurations());

        logger.info("Input Json = " + requestJson);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/configurations/import/cp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Test clear configurations")
    void testClearConfigController() throws Exception {

        Mockito.doNothing().when(configurationService).clearConfigurations();

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/configurations"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Test clear import history")
    void testClearHistoryController() throws Exception {

        Mockito.doNothing().when(configurationService).clearConfigurations();

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/configurations/import-history"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Test get Import History")
    void testGetImportHistory() throws Exception {

        ImportHistory importHistory = new ImportHistory();
        importHistory.setStatus(ImportHistory.Status.SUCCESS);
        importHistory.setId(1L);

        Mockito.doReturn(Lists.newArrayList(importHistory)).when(configurationService).getHistory();

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/configurations/history"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$" , hasSize(1)))
                .andExpect(jsonPath("$[0].id" , is(1)));
    }

}


