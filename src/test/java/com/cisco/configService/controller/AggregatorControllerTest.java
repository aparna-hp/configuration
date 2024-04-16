package com.cisco.configService.controller;

import com.cisco.configService.model.aggregator.Purge;
import com.cisco.configService.service.AggregatorService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
public class AggregatorControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    AggregatorService aggregatorService;

    private static final Logger logger =
            LogManager.getLogger(AggregatorControllerTest.class);

    @Test
    @DisplayName("Test Add Purge")
    void testAddPurge() throws Exception {

        Mockito.doReturn(true).when(aggregatorService).updatePurgeConfig(Mockito.any(Purge.class));

        ObjectMapper mapper = new ObjectMapper();
        String requestJson=mapper.writeValueAsString(new Purge());

        logger.info("Input Json = " + requestJson);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/aggregator/purge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test reset aggregator config")
    void testResetAggrConfig() throws Exception {

        Mockito.doReturn(true).when(aggregatorService).resetAggrConfig();

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/aggregator/reset")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test get global aggregator config ")
    void testGetGlobalAggrConfig() throws Exception {

        Mockito.doReturn(Optional.empty()).when(aggregatorService).getAggrConfig();

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/aggregator")
                        .contentType(MediaType.TEXT_PLAIN_VALUE))
                .andExpect(status().isNotFound());

    }

    @Test
    @DisplayName("Test download global aggregator config ")
    void testDownloadGlobalAggrConfig() throws Exception {

        Mockito.doReturn(Optional.empty()).when(aggregatorService).getAggrConfig();

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/aggregator/download")
                        .contentType(MediaType.TEXT_PLAIN_VALUE))
                .andExpect(status().isNoContent());

    }

    @Test
    @DisplayName("Test get purge config ")
    void testGetPurgeConfig() throws Exception {

        Mockito.doReturn(new Purge()).when(aggregatorService).getPurgeConfig();

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/aggregator/purge")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

    }
}
