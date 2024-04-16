package com.cisco.configService.controller;

import com.cisco.configService.enums.AgentTypes;
import com.cisco.configService.model.preConfig.AgentData;
import com.cisco.configService.model.preConfig.AllAgentData;
import com.cisco.configService.service.AgentService;
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
public class AgentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    AgentService agentService;

    private static final Logger logger =
            LogManager.getLogger(AgentControllerTest.class);

    @Test
    @DisplayName("Test Add Update Agent")
    void testAddAgent() throws Exception {

        AgentData agent = new AgentData();
        agent.setName("testAgent");

        AllAgentData allAgentData = new AllAgentData();
        allAgentData.setId(1L);
        allAgentData.setName("agent");
        allAgentData.setType(AgentTypes.SR_PCE_AGENT);

        Mockito.doNothing().when(agentService).addAgent(Mockito.any(AgentData.class));
        Mockito.doReturn(Optional.of(1L)).when(agentService).updateAgent(Mockito.any(AgentData.class));


        ObjectMapper mapper = new ObjectMapper();
        String requestJson=mapper.writeValueAsString(agent);

        logger.info("Input Json = " + requestJson);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/networks/collectors/agents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated());

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/networks/collectors/agents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test get all agents ")
    void testGetAllAgents() throws Exception {

        AllAgentData allAgentData = new AllAgentData();
        allAgentData.setId(1L);
        allAgentData.setName("agent");
        allAgentData.setType(AgentTypes.SR_PCE_AGENT);

        Mockito.doReturn(List.of(allAgentData)).when(agentService).getAllAgents();

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/networks/collectors/agents")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    @Test
    @DisplayName("Test get agent status ")
    void testGetAgentsStatus() throws Exception {

        AllAgentData allAgentData = new AllAgentData();
        allAgentData.setId(1L);
        allAgentData.setName("agent");
        allAgentData.setType(AgentTypes.SR_PCE_AGENT);

        Mockito.doReturn(List.of(allAgentData)).when(agentService).getAllAgentStatus();
        Mockito.doReturn(Optional.of(allAgentData)).when(agentService).getStatus(1L);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/networks/collectors/agents/status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/networks/collectors/agents/status/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/networks/collectors/agents/status/2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

    }

    @Test
    @DisplayName("Test delete agent")
    void testDeleteAgent() throws Exception {

        Mockito.doReturn(Optional.of(1L)).when(agentService).deleteAgent(1L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/networks/collectors/agents?id=1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/networks/collectors/agents?id=6")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
