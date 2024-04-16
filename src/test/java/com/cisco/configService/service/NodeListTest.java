package com.cisco.configService.service;

import com.cisco.configService.entity.AuthGroup;
import com.cisco.configService.entity.SnmpGroup;
import com.cisco.configService.exception.CustomException;
import com.cisco.configService.exception.ValidationService;
import com.cisco.configService.model.preConfig.FilterCriteria;
import com.cisco.configService.model.preConfig.NodeListData;
import com.cisco.configService.model.preConfig.NodeListResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NodeListTest {

    @Autowired
    NodeListService nodeListService;

    @MockBean
    AuthGroupService authGroupService;

    @MockBean
    SnmpGroupService snmpGroupService;

    @MockBean
    ValidationService<NodeListData> validationService;

    private static final Logger logger = LogManager.getLogger(NodeListTest.class);
    private static final Long nodeProfileId = 1L;

    @Test
    @Order(1)
    @DisplayName("Test Add Update Delete node list and test pagination for node list")
    void testAddUpdateDeleteNodeList() {
        Mockito.doNothing().when(validationService).validateInput(Mockito.any());

        List<NodeListData> nodeLists = new ArrayList<>();

        for (int i = 1; i < 3000; i++) {
            NodeListData nodeList = new NodeListData();
            nodeList.setNodeIp("NodeIp_" + String.format("%04d", i));
            nodeLists.add(nodeList);
        }

        nodeListService.addUpdateNodeListsToProfile(nodeProfileId, nodeLists);

        List<NodeListData> nodeListDataList = nodeListService.getPaginatedNodeList(
                nodeProfileId, "nodeIp", true, null, 30, null);
        logger.info("nodeListDataList.size() = " + nodeListDataList.size());
        Assertions.assertEquals(100, nodeListDataList.size());

        for (NodeListData nodeListData : nodeLists) {
            nodeListData.setNodeManagementIp("NodeManagementIp_" + String.format("%04d", nodeListData.getId()));
        }

        nodeListService.addUpdateNodeListsToProfile(nodeProfileId, nodeLists);

        Assertions.assertTrue(nodeListService.getNodeListCount(nodeProfileId, null) > 0);

        nodeListDataList = nodeListService.getPaginatedNodeList(
                nodeProfileId, "nodeIp", true, null, null, null);
        logger.info("nodeListDataList.size() = " + nodeListDataList.size());
        Assertions.assertEquals(100, nodeListDataList.size());

        Optional<NodeListData> minNodeIp = nodeListDataList.stream().min(Comparator.comparing(NodeListData::getNodeIp));
        Optional<NodeListData> maxNodeIp = nodeListDataList.stream().max(Comparator.comparing(NodeListData::getNodeIp));


        Assertions.assertTrue(minNodeIp.isPresent());
        logger.info("Min Node Ip " + minNodeIp.get());
        Assertions.assertEquals("1.1.1.1", minNodeIp.get().getNodeIp());
        Assertions.assertTrue(maxNodeIp.isPresent());
        logger.info("Max Node Ip " + maxNodeIp.get());
        Assertions.assertEquals("NodeIp_0099", maxNodeIp.get().getNodeIp());


        nodeListDataList = nodeListService.getPaginatedNodeList(
                nodeProfileId, null, true, null, 30, null);
        Assertions.assertEquals(100, nodeListDataList.size());

        minNodeIp = nodeListDataList.stream().min(Comparator.comparing(NodeListData::getNodeIp));
        maxNodeIp = nodeListDataList.stream().max(Comparator.comparing(NodeListData::getNodeIp));
        logger.info("Min Node Ip " + minNodeIp);
        logger.info("Max Node Ip " + maxNodeIp);

        Assertions.assertTrue(minNodeIp.isPresent());
        Assertions.assertEquals("NodeIp_2900", minNodeIp.get().getNodeIp());
        Assertions.assertTrue(maxNodeIp.isPresent());
        Assertions.assertEquals("NodeIp_2999", maxNodeIp.get().getNodeIp());

        nodeListService.deleteNodeListsOfNodeProfile(nodeListDataList.stream()
                .map(NodeListData::getId).collect(Collectors.toList()));

        nodeListDataList = nodeListService.getPaginatedNodeList(
                nodeProfileId, "nodeIp", true, null, 30, null);
        logger.info("nodeListDataList.size() = " + nodeListDataList.size());
        Assertions.assertEquals(0, nodeListDataList.size());

    }

    @Test
    @Order(2)
    void testNodeListCount() {
        Long count = nodeListService.getNodeListCount(nodeProfileId, null);
        logger.info("Count of the node list " + count);
        Assertions.assertTrue(count > 0);

        count = nodeListService.getNodeListCount(nodeProfileId, List.of(
                new FilterCriteria("nodeIp", "z")));
        logger.info("Count of the node list " + count);
        Assertions.assertEquals(0, (long) count);
    }

    @Test
    @Order(3)
    void testNodeListDeleteWithSnmpAuthGroup() {
        Mockito.doNothing().when(validationService).validateInput(Mockito.any());

        SnmpGroup snmpGroupData = new SnmpGroup();
        snmpGroupData.setId(1L);
        snmpGroupData.setName("snmp_group");

        AuthGroup authGroupData = new AuthGroup();
        authGroupData.setId(1L);
        authGroupData.setName("auth_group");

        Mockito.doReturn(Optional.of(snmpGroupData)).when(snmpGroupService).isSnmpGroupExists("snmp_group");
        Mockito.doReturn(Optional.of(authGroupData)).when(authGroupService).isAuthGroupExist("auth_group");

        List<NodeListData> nodeLists = new ArrayList<>();

        for (int i = 1; i < 10; i++) {
            NodeListData nodeList = new NodeListData();
            nodeList.setNodeIp("NodeIp_" + String.format("%04d", i));
            nodeList.setAuthGroupName("auth_group");
            nodeList.setSnmpGroupName("snmp_group");
            nodeLists.add(nodeList);
        }

        nodeListService.addUpdateNodeListsToProfile(nodeProfileId, nodeLists);

        nodeListService.deleteNodeListsOfNodeProfile(nodeLists.stream()
                .map(NodeListData::getId).collect(Collectors.toList()));

    }

    @Test
    @Order(4)
    public void testNodeListColumnFilter() {
        Mockito.doNothing().when(validationService).validateInput(Mockito.any());

        SnmpGroup snmpGroupData = new SnmpGroup();
        snmpGroupData.setId(1L);
        snmpGroupData.setName("snmp_group");

        AuthGroup authGroupData = new AuthGroup();
        authGroupData.setId(1L);
        authGroupData.setName("auth_group");

        Mockito.doReturn(Optional.of(snmpGroupData)).when(snmpGroupService).isSnmpGroupExists("snmp_group");
        Mockito.doReturn(Optional.of(authGroupData)).when(authGroupService).isAuthGroupExist("auth_group");

        List<NodeListData> nodeLists = new ArrayList<>();

        for (int i = 1; i < 10; i++) {
            NodeListData nodeList = new NodeListData();
            nodeList.setNodeIp("NodeIp_" + String.format("%04d", i));
            nodeList.setNodeManagementIp("ManagementIp_" + String.format("%04d", i));
            nodeList.setAuthGroupName("auth_group");
            nodeList.setSnmpGroupName("snmp_group");
            nodeLists.add(nodeList);
        }

        nodeListService.addUpdateNodeListsToProfile(nodeProfileId, nodeLists);

        List<NodeListData> nodeListData = nodeListService.getPaginatedNodeList(nodeProfileId, null, null,
                null, null, null);

        logger.info("NodeIp before filter :" + nodeListData.size());
        nodeListData.forEach(s -> logger.info(s.getNodeIp() + " " + s.getNodeManagementIp()));

        Assertions.assertTrue(nodeListData.size() >= 10);

        long count = nodeListService.getNodeListCount(nodeProfileId, null);
        Assertions.assertTrue(count >= 10);


        nodeListData = nodeListService.getPaginatedNodeList(nodeProfileId, null, null, null, null,
                List.of(new FilterCriteria("nodeIP", "1")));

        logger.info("NodeIp after filter :" + nodeListData.size());
        nodeListData.forEach(s -> logger.info(s.getNodeIp() + " " + s.getNodeManagementIp()));

        Assertions.assertTrue(nodeListData.size() >= 2);
        count = nodeListService.getNodeListCount(nodeProfileId, List.of(new FilterCriteria("nodeIP", "1")));
        Assertions.assertTrue(count >= 2);

        nodeListData = nodeListService.getPaginatedNodeList(nodeProfileId, null, null, null, null,
                List.of(new FilterCriteria("nodeIP", "1"),
                        new FilterCriteria("nodeManagementIP", "0001")));

        logger.info("NodeIp after filter :" + nodeListData.size());
        nodeListData.forEach(s -> logger.info(s.getNodeIp() + " " + s.getNodeManagementIp()));

        Assertions.assertEquals(1, nodeListData.size());
        count = nodeListService.getNodeListCount(nodeProfileId,
                List.of(new FilterCriteria("nodeIP", "1"),
                        new FilterCriteria("nodeManagementIP", "0001")));
        Assertions.assertTrue(count >= 1);

    }

    @Test
    @Order(10)
    public void testExportDeleteNodeList() {
        Mockito.doReturn(Optional.of("snmp-group")).when(snmpGroupService).getSnmpGroupNameById(Mockito.any());
        Mockito.doReturn(Optional.of("auth-group")).when(authGroupService).getAuthGroupNameById(Mockito.any());

        String output = nodeListService.getAllNodeListsOfProfile(1L);
        logger.debug("Node List exported " + output);
        String[] temp = output.split(System.lineSeparator());
        Assertions.assertTrue(temp.length > 1);

        nodeListService.deleteAllNodeListsOfNodeProfile(1L);
        output = nodeListService.getAllNodeListsOfProfile(1L);
        logger.debug("Node List exported after delete" + output);
        temp = output.split(System.lineSeparator());
        Assertions.assertEquals(1, temp.length);
    }

    @Test
    @Order(5)
    public void testImportNodes() {

        Mockito.doNothing().when(validationService).validateInput(Mockito.any());

        MockMultipartFile invalidCsvFile = new MockMultipartFile("node.csv", "node.csv", "text/csv",
                ("???\"IP Address\",\"SNMP Credential\",\"Authentication Credential\",\"IP Manage\"").getBytes());

        Assertions.assertThrows(CustomException.class, () -> nodeListService.importNodeLists(1L, invalidCsvFile, new ArrayList<>()));

        MockMultipartFile invalidCsvFile2 = new MockMultipartFile("node.csv", "node.csv", "text/csv",
                ("???\"Node IP Address\",\"SNMP Credential\",\"IP Manage\",\"Authentication Credential\"").getBytes());

        Assertions.assertThrows(CustomException.class, () -> nodeListService.importNodeLists(1L, invalidCsvFile2, new ArrayList<>()));

        MockMultipartFile csvFile = new MockMultipartFile("node.csv", "node.csv", "text/csv",
                ("???\"Node IP Address\",\"SNMP Profile\",\"Authentication Profile\",\"Management IP\"").getBytes());


        NodeListResponse nodeListResponse = nodeListService.importNodeLists(1L, csvFile, new ArrayList<>());
        Long count = nodeListService.getNodeListCount(1L, null);
        Assertions.assertTrue(nodeListResponse.getStatus());
        Assertions.assertTrue(count >= 1);


        csvFile = new MockMultipartFile("node.csv", "node.csv", "text/csv",
                ("???\"Node IP Address\",\"SNMP Profile\",\"Authentication Profile\",\"Management IP\"\n" +
                        "\"10.0.255.62\",\"snmp-group-lab\",\"auth-group-lab\",\"10.225.120.62\"\n" +
                        "\"10.0.255.44\",\"snmp-group-lab\",\"auth-group-lab\",\"10.225.120.44\"\n" +
                        "10.0.255.63\",\"snmp-group-lab\",\"auth-group-lab\",\"10.225.120.63\"\n" +
                        "\"10.0.255.43\",\"snmp-group-lab\",\"auth-group-lab\",\"10.225.120.43\"\n" +
                        "\"41.41.41.41\",\"snmp-group-lab\",\"auth-group-104\",\"10.104.56.50\"").getBytes());


        nodeListResponse = nodeListService.importNodeLists(1L, csvFile, new ArrayList<>());
        count = nodeListService.getNodeListCount(1L, null);
        Assertions.assertFalse(nodeListResponse.getStatus());
        Assertions.assertTrue(count >= 5);
    }
}
