package com.cisco.configService.controller;

import com.cisco.configService.model.composer.ApiResponseDetail;
import com.cisco.configService.model.scheduler.SchedulerConfigData;
import com.cisco.configService.service.SchedulerService;
import com.cisco.workflowmanager.JobInfo;
import com.cisco.workflowmanager.NetworkResyncInfo;
import com.cisco.workflowmanager.NetworkStatus;
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

import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
public class SchedulerControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    SchedulerService schedulerService;

    private static final Logger logger =
            LogManager.getLogger(SchedulerControllerTest.class);

    @Test
    @DisplayName("Test Add Scheduler")
    void testPOSTSchedulerController() throws Exception {

        SchedulerConfigData jobInfo = new SchedulerConfigData();
        jobInfo.setId(1L);
        jobInfo.setName("Job_1");
        jobInfo.setNetworkId(1L);
        List<SchedulerConfigData> input = List.of(jobInfo);

        Mockito.doReturn(List.of(new ApiResponseDetail())).when(schedulerService).addUpdateScheduler(input, false);

        ObjectMapper mapper = new ObjectMapper();
        String requestJson = mapper.writeValueAsString(jobInfo);

        logger.info("Input Json = " + requestJson);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/schedulers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Test Add Resync Job")
    void testPostResyncJob() throws Exception {

        NetworkResyncInfo networkResyncInfo = new NetworkResyncInfo();
        networkResyncInfo.setId(1L);
        networkResyncInfo.setNetworkName("Job_1");
        networkResyncInfo.setNetworkId(1L);

        Mockito.doReturn(Optional.of(networkResyncInfo)).when(schedulerService).rsyncNetwork(Mockito.any());

        ObjectMapper mapper = new ObjectMapper();
        String requestJson = mapper.writeValueAsString(networkResyncInfo);

        logger.info("Input Json = " + requestJson);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/schedulers/resync")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated());
    }


    @Test
    @DisplayName("Test Get Scheduler")
    void testGetScheduler() throws Exception {

        SchedulerConfigData jobInfo = new SchedulerConfigData();
        jobInfo.setId(1L);
        jobInfo.setName("Job_1");
        jobInfo.setNetworkId(1L);

        Mockito.doReturn(Optional.of(jobInfo)).when(schedulerService).getScheduler(1L);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/schedulers/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/schedulers/2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Test Get Scheduler statistics")
    void testGetSchedulerStatistics() throws Exception {

        Mockito.doReturn(Optional.empty()).when(schedulerService).getSchedulerStatistics();

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/schedulers/statistics")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Test Get All Schedulers")
    void testGetSchedulers() throws Exception {

        JobInfo jobInfo = new JobInfo();
        jobInfo.setId(1L);
        jobInfo.setName("Job_1");
        jobInfo.setNetworkId(1L);
        List<JobInfo> input = List.of(jobInfo);

        Mockito.doReturn(input).when(schedulerService).getSchedulersOfNetwork(1L);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/schedulers")
                        .param("networkId","1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test Get Resync Scheduler info associated with all networks")
    void testGetResyncSchedulers() throws Exception {

        NetworkResyncInfo networkResyncInfo = new NetworkResyncInfo();
        networkResyncInfo.setId(1L);
        networkResyncInfo.setNetworkName("Job_1");
        networkResyncInfo.setNetworkId(1L);

        Mockito.doReturn(List.of(networkResyncInfo)).when(schedulerService).getNetworkRsyncInfo();

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/schedulers/resync")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test Get Scheduler status associated with the given network")
    void testGetSchedulerStatus() throws Exception {

        Mockito.doReturn(new NetworkStatus()).when(schedulerService).getNetworkStatus(Mockito.anyLong());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/schedulers/status/network")
                        .param("networkId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }


    @Test
    @DisplayName("Test task history")
    void testGetTaskHistory() throws Exception {

        Mockito.doReturn(Optional.empty()).when(schedulerService).getTaskHistory(1L);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/schedulers/task-history")
                        .param("schedulerId","1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
