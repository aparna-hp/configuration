package com.cisco.configService.controller;

import com.cisco.configService.model.preConfig.NodeFilterData;
import com.cisco.configService.service.NodeFilterService;
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

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
public class NodeFilterControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    NodeFilterService nodeFilterService;

    private static final Logger logger =
            LogManager.getLogger(NodeFilterControllerTest.class);

    @Test
    @DisplayName("Test Add Node Filter to a profile")
    void testPOSTNodeListController() throws Exception {

        List<NodeFilterData>  nodeFilterDataList  = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            NodeFilterData nodeFilterData = new NodeFilterData();
            nodeFilterData.setCondition(NodeFilterData.Condition.INCLUDE);
            nodeFilterData.setType(NodeFilterData.Type.HOST_REGEX);
            nodeFilterData.setValue("Host_" + String.format("%04d", i));
            nodeFilterDataList.add(nodeFilterData);
        }

        Mockito.doNothing().when(nodeFilterService).addUpdateNodeFilterToProfile(Mockito.any(), Mockito.any(),
                Mockito.anyBoolean());

        ObjectMapper mapper = new ObjectMapper();
        String requestJson=mapper.writeValueAsString(nodeFilterDataList);

        logger.info("Input Json = " + requestJson);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/node-profiles/node-filters")
                .contentType(MediaType.APPLICATION_JSON)
                        .param("nodeProfileId", "1")
                .content(requestJson))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Test get node Filters")
    void testGetNetworkController() throws Exception {

        List<NodeFilterData>  nodeFilterDataList  = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            NodeFilterData nodeFilterData = new NodeFilterData();
            nodeFilterData.setCondition(NodeFilterData.Condition.INCLUDE);
            nodeFilterData.setType(NodeFilterData.Type.HOST_REGEX);
            nodeFilterData.setValue("Host_" + String.format("%04d", i));
            nodeFilterDataList.add(nodeFilterData);
        }

        Mockito.doReturn(nodeFilterDataList).when(nodeFilterService).getNodeFilter(1L);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/node-profiles/node-filters")
                        .param("nodeProfileId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$" , hasSize(10)))
                .andExpect(jsonPath("$[0].type" , is(NodeFilterData.Type.HOST_REGEX.name())));

    }
}
