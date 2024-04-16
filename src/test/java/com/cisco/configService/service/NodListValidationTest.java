package com.cisco.configService.service;

import com.cisco.configService.model.preConfig.NodeListData;
import com.cisco.configService.model.preConfig.NodeListResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@SpringBootTest
public class NodListValidationTest {

    @Autowired
    NodeListService nodeListService;

    private static final Logger logger = LogManager.getLogger(NodListValidationTest.class);

    @Test
    @DisplayName("Test Add Update Delete node list and test pagination for node list")
    void testAddUpdateDeleteNodeList() {

        List<NodeListData> nodeLists = new ArrayList<>();

        for (int i = 1; i < 256; i++) {
            NodeListData nodeList = new NodeListData();
            nodeList.setNodeIp("NodeIp_" + String.format("%04d", i));
            nodeList.setNodeManagementIp(nodeList.getNodeIp());
            nodeLists.add(nodeList);
        }

        NodeListResponse nodeListResponse = nodeListService.addUpdateNodeListsToProfile(1L, nodeLists);
        Assertions.assertFalse(nodeListResponse.getStatus());
        Assertions.assertEquals(0, nodeListResponse.getValidNodeListCount());
        Assertions.assertEquals(255, nodeListResponse.getInvalidNodeListCount());
        Assertions.assertEquals(0, nodeListResponse.getAuthMismatchNodListCount());
        Assertions.assertEquals(0, nodeListResponse.getSnmpMismatchNodListCount());

        nodeLists = new ArrayList<>();
        for (int i = 1; i < 256; i++) {
            NodeListData nodeList = new NodeListData();
            nodeList.setNodeIp(i + "." + i + "." + i + "." + i);
            nodeLists.add(nodeList);
        }

        nodeListResponse = nodeListService.addUpdateNodeListsToProfile(1L, nodeLists);
        Assertions.assertFalse(nodeListResponse.getStatus());
        Assertions.assertEquals(0, nodeListResponse.getValidNodeListCount());
        Assertions.assertEquals(255, nodeListResponse.getInvalidNodeListCount());
        Assertions.assertEquals(0, nodeListResponse.getAuthMismatchNodListCount());
        Assertions.assertEquals(0, nodeListResponse.getSnmpMismatchNodListCount());

        List<NodeListData> nodeListDataList = nodeListService.getPaginatedNodeList(
                1L, "nodeIp", true, null, null, null);
        Assertions.assertEquals(1, nodeListDataList.size());

        for (NodeListData nodeListData : nodeLists) {
            nodeListData.setNodeManagementIp(nodeListData.getNodeIp());
        }

        nodeListResponse = nodeListService.addUpdateNodeListsToProfile(1L, nodeLists);
        Assertions.assertFalse(nodeListResponse.getStatus());
        Assertions.assertEquals(0, nodeListResponse.getValidNodeListCount());
        Assertions.assertEquals(0, nodeListResponse.getInvalidNodeListCount());
        Assertions.assertEquals(255, nodeListResponse.getAuthMismatchNodListCount());
        Assertions.assertEquals(255, nodeListResponse.getSnmpMismatchNodListCount());

        nodeListDataList = nodeListService.getPaginatedNodeList(
                1L, "nodeIp", true, null, null, null);
        Assertions.assertEquals(100, nodeListDataList.size());
    }
}
