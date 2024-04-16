package com.cisco.configService.controller;

import com.cisco.configService.exception.CustomException;
import com.cisco.configService.model.preConfig.NodeListData;
import com.cisco.configService.model.preConfig.NodeListResponse;
import com.cisco.configService.service.NodeListService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
public class NodeListControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    NodeListService nodeListService;

    private static final Logger logger =
            LogManager.getLogger(NodeListControllerTest.class);

    @Test
    @DisplayName("Test add update Node Lists to a node profile")
    void testAddUpdateNodeListController() throws Exception {

        List<NodeListData>  nodeLists  = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            NodeListData nodeList = new NodeListData();
            nodeList.setNodeIp("NodeIp_" + String.format("%04d", i));
            nodeLists.add(nodeList);
        }

        Mockito.doReturn(new NodeListResponse()).when(nodeListService).addUpdateNodeListsToProfile(Mockito.any(), Mockito.any());

        ObjectMapper mapper = new ObjectMapper();
        String requestJson=mapper.writeValueAsString(nodeLists);

        logger.info("Input Json = " + requestJson);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/node-profiles/node-lists")
                .contentType(MediaType.APPLICATION_JSON)
                        .param("nodeProfileId", "1")
                .content(requestJson))
                .andExpect(status().isCreated());

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/node-profiles/node-lists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("nodeProfileId", "1")
                        .content(requestJson))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test add update Node Lists with the mismatch to a node profile .")
    void testAddUpdateNodeListWithMismatch() throws Exception {

        List<NodeListData>  nodeLists  = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            NodeListData nodeList = new NodeListData();
            nodeList.setNodeIp("NodeIp_" + String.format("%04d", i));
            nodeLists.add(nodeList);
        }

        NodeListResponse nodeListResponse = new NodeListResponse();
        nodeListResponse.setInvalidNodeListCount(10);

        Mockito.doReturn(nodeListResponse).when(nodeListService).addUpdateNodeListsToProfile(Mockito.any(), Mockito.any());

        ObjectMapper mapper = new ObjectMapper();
        String requestJson=mapper.writeValueAsString(nodeLists);

        logger.info("Input Json = " + requestJson);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/node-profiles/node-lists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("nodeProfileId", "1")
                        .content(requestJson))
                .andExpect(status().isMultiStatus());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/node-profiles/node-lists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("nodeProfileId", "1")
                        .content(requestJson))
                .andExpect(status().isMultiStatus());
    }

    @Test
    @DisplayName("Test Import csv file")
    void testImportNodes() throws Exception {

        MockMultipartFile csvFile = new MockMultipartFile("node.csv", "node.csv", "text/csv",
                ("???\"Node IP Address\",\"SNMP Credential\",\"Authentication Credential\",\"IP Manage\"").getBytes());

        Mockito.doReturn(new NodeListResponse()).when(nodeListService).importNodeLists(Mockito.any(), Mockito.any(), Mockito.any());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/node-profiles/node-lists/import")
                        .file("csvFile",csvFile.getBytes())
                        .param("nodeProfileId", "1")
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isCreated());

    }

    @Test
    @DisplayName("Test Import csv file and return partial success.")
    void testImportNodesWithPartialSuccess() throws Exception {

        MockMultipartFile csvFile = new MockMultipartFile("node.csv", "node.csv", "text/csv",
                ("???\"Node IP Address\",\"SNMP Credential\",\"Authentication Credential\",\"IP Manage\"").getBytes());
        NodeListResponse nodeListResponse = new NodeListResponse();
        nodeListResponse.setInvalidNodeListCount(10);

        Mockito.doReturn(nodeListResponse).when(nodeListService).importNodeLists(Mockito.any(), Mockito.any(), Mockito.any());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/node-profiles/node-lists/import")
                        .file("csvFile",csvFile.getBytes())
                        .param("nodeProfileId", "1")
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isMultiStatus());

    }

    @Test
    @DisplayName("Test Import csv file")
    void testImportNodesException() throws Exception {

        MockMultipartFile csvFile = new MockMultipartFile("node.csv", "node.csv", "text/csv",
                ("This is an invalid csv file.").getBytes());

        Mockito.doThrow(new CustomException(HttpStatus.BAD_REQUEST, "Invalid format")).when(nodeListService)
                .importNodeLists(Mockito.any(), Mockito.any(), Mockito.any());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/node-profiles/node-lists/import")
                        .file("csvFile",csvFile.getBytes())
                        .param("nodeProfileId", "1")
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isBadRequest());

    }


    @Test
    @DisplayName("Test get node Lists")
    void testGetNetworkList() throws Exception {

        List<NodeListData>  nodeLists  = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            NodeListData nodeList = new NodeListData();
            nodeList.setNodeIp("NodeIp_" + String.format("%02d", i));
            nodeLists.add(nodeList);
        }

        Mockito.doReturn(10L).when(nodeListService).getNodeListCount(
                1L, null);

        Mockito.doReturn(nodeLists).when(nodeListService).getPaginatedNodeList(
                1L, null,null,null,null,null);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/node-profiles/node-lists/filteredList")
                        .param("nodeProfileId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("count" , is(10)))
                .andExpect(jsonPath("nodeLists[0].nodeIp" , is("NodeIp_00")))
                .andExpect(jsonPath("nodeLists[9].nodeIp" , is("NodeIp_09")));

    }

    @Test
    @DisplayName("Test export node list")
    void testExportConfig() throws Exception {

        Mockito.doReturn("").when(nodeListService).getAllNodeListsOfProfile(Mockito.any());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/node-profiles/node-lists/export")
                        .param("nodeProfileId", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Test get node Lists count")
    void testGetNetworkListCount() throws Exception {

        Mockito.doReturn(200L).when(nodeListService).getNodeListCount(
                1L,  null );

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/node-profiles/node-lists/count")
                        .param("nodeProfileId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    }

    @Test
    @DisplayName("Test delete node lists")
    void testDeleteNodeLists() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        String requestJson=mapper.writeValueAsString(List.of(1L));

        Mockito.doNothing().when(nodeListService).deleteNodeListsOfNodeProfile(Mockito.anyList());

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/node-profiles/node-lists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Test delete all the node lists belonging to node profile")
    void testDeleteAllNodeLists() throws Exception {

        Mockito.doNothing().when(nodeListService).deleteAllNodeListsOfNodeProfile(Mockito.anyLong());

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/node-profiles/node-lists/node-profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("nodeProfileId", "1"))
                .andExpect(status().isNoContent());
    }
}
