package com.cisco.configService.controller;

import com.cisco.configService.model.preConfig.AllNodeProfileData;
import com.cisco.configService.model.preConfig.NodeProfileData;
import com.cisco.configService.service.NodeProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.ArrayList;
import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
public class NodeProfileControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    NodeProfileService nodeProfileService;

    @Test
    @DisplayName("Test get node profile.")
    void testNetworkProfile() throws Exception {

        Mockito.doReturn(Optional.of(new NodeProfileData())).when(nodeProfileService).getNodeProfile(Mockito.any());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/node-profiles/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Test get node profile info")
    void testNetworkProfileInfo() throws Exception {

        Mockito.doReturn(Optional.of(new NodeProfileData())).when(nodeProfileService).getNodeProfileInfo(Mockito.any());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/node-profiles/info")
                        .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Test get all node profiles")
    void testGetAllNodeProfile() throws Exception {

        Mockito.doReturn(new ArrayList<AllNodeProfileData>()).when(nodeProfileService).getAllNodeProfileData();

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/node-profiles")
                        .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Test Add Node Profile")
    void testAddNodeProfile() throws Exception {

        NodeProfileData nodeProfileData = new NodeProfileData();
        nodeProfileData.setName("nodeProfileData");

        Mockito.doNothing().when(nodeProfileService).addNodeProfile(Mockito.any(NodeProfileData.class));

        ObjectMapper mapper = new ObjectMapper();
        String requestJson=mapper.writeValueAsString(nodeProfileData);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/node-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Test Update Node Profile")
    void testUpdateNodeProfile() throws Exception {

        NodeProfileData nodeProfileData = new NodeProfileData();
        nodeProfileData.setName("nodeProfileData");

        Mockito.doReturn(Optional.empty()).when(nodeProfileService).updateNodeProfile(Mockito.any(NodeProfileData.class));

        ObjectMapper mapper = new ObjectMapper();
        String requestJson=mapper.writeValueAsString(nodeProfileData);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/node-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Test Update Node Profile Info and filter")
    void testUpdateNodeProfileInfo() throws Exception {

        NodeProfileData nodeProfileData = new NodeProfileData();
        nodeProfileData.setName("nodeProfileData");

        Mockito.doReturn(Optional.of(1L)).when(nodeProfileService).updateNodeProfileNodeFilter(Mockito.any(NodeProfileData.class));

        ObjectMapper mapper = new ObjectMapper();
        String requestJson=mapper.writeValueAsString(nodeProfileData);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/node-profiles/info-and-node-filter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());
    }
}
