package com.cisco.configService.service;

import com.cisco.configService.entity.AuthGroup;
import com.cisco.configService.entity.SnmpGroup;
import com.cisco.configService.exception.CustomException;
import com.cisco.configService.model.preConfig.AllNodeProfileData;
import com.cisco.configService.model.preConfig.NodeFilterData;
import com.cisco.configService.model.preConfig.NodeListData;
import com.cisco.configService.model.preConfig.NodeProfileData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NodeProfileTest {

    @Autowired
    NodeProfileService nodeProfileService;

    private static final Logger logger = LogManager.getLogger(NodeProfileTest.class);

    @MockBean
    AuthGroupService authGroupService;

    @MockBean
    SnmpGroupService snmpGroupService;

    @MockBean
    PreConfigFileGeneratorService preConfigFileGeneratorService;

    private static final String SNMP_GROUP_NAME = "snmp_group";
    private static final String AUTH_GROUP_NAME = "auth_group";

    @Test
    void testAddNodeProfile() {

        SnmpGroup snmpGroup = new SnmpGroup();
        snmpGroup.setId(1L);
        snmpGroup.setName("snmp_group");

        AuthGroup authGroup = new AuthGroup();
        authGroup.setId(1L);
        authGroup.setName("auth_group");

        Mockito.doReturn(Optional.of(snmpGroup)).when(snmpGroupService).isSnmpGroupExists(SNMP_GROUP_NAME);
        Mockito.doReturn(Optional.of(authGroup)).when(authGroupService).isAuthGroupExist(AUTH_GROUP_NAME);


        NodeProfileData nodeProfile = new NodeProfileData();
        nodeProfile.setName("nodeProfileTest");
        nodeProfile.setDefaultAuthGroup(AUTH_GROUP_NAME);
        nodeProfile.setDefaultSnmpGroup(SNMP_GROUP_NAME);

        NodeListData nodeList = new NodeListData();
        nodeList.setNodeIp("10.225.120.10");
        nodeList.setNodeManagementIp("172.168.1.1");
        nodeList.setAuthGroupName("auth_group");
        nodeList.setSnmpGroupName("snmp_group");

        nodeProfile.setUseNodeListAsIncludeFilter(true);

        nodeProfile.setNodeLists(Set.of(nodeList));

        nodeProfileService.addNodeProfile(nodeProfile,true);

        logger.info("Node profile " + nodeProfile);

        Assertions.assertNotNull(nodeProfile.getId());
        Assertions.assertEquals(1, nodeProfile.getNodeLists().size());
        Assertions.assertEquals(0, nodeProfile.getNodeFilters().size());
    }

    @Test
    void testAddNodeListWithoutAuthGroup() {
        {

            Mockito.doReturn(Optional.of(new SnmpGroup())).when(snmpGroupService).isSnmpGroupExists(SNMP_GROUP_NAME);
            Mockito.doReturn(Optional.of(new AuthGroup())).when(authGroupService).isAuthGroupExist(AUTH_GROUP_NAME);

            NodeProfileData nodeProfile = new NodeProfileData();
            nodeProfile.setName("nodeProfileWithoutAuthGroupTest");
            nodeProfile.setDefaultAuthGroup(AUTH_GROUP_NAME);
            nodeProfile.setDefaultSnmpGroup(SNMP_GROUP_NAME);

            NodeListData nodeList = new NodeListData();
            nodeList.setNodeIp("10.225.120.10");
            nodeList.setNodeManagementIp("172.168.1.1");
            nodeList.setSnmpGroupName("snmpGroup");
            nodeList.setAuthGroupName("authGroup");

            NodeFilterData nodeFilter = new NodeFilterData();
            nodeFilter.setType(NodeFilterData.Type.HOST_REGEX);
            nodeFilter.setCondition(NodeFilterData.Condition.INCLUDE);
            nodeFilter.setValue("host*");

            nodeProfile.setNodeLists(Set.of(nodeList));
            nodeProfile.setNodeFilters(Set.of(nodeFilter));

            nodeProfileService.addNodeProfile(nodeProfile);

            Assertions.assertNotNull(nodeProfile.getId());

            logger.info("Node profile " + nodeProfile);

            nodeProfile.getNodeLists().forEach(nodeListData -> {
                Assertions.assertTrue(nodeListData.isAuthGroupMismatch());
                Assertions.assertTrue(nodeListData.isSnmpGroupMismatch());
            });

        }
    }

    @Test
    @DisplayName("Verify the node profile with NodeList update use case")
    void testUpdateNodeProfile() {

        SnmpGroup snmpGroup = new SnmpGroup();
        snmpGroup.setId(1L);
        snmpGroup.setName("snmp_group");

        AuthGroup authGroup = new AuthGroup();
        authGroup.setId(1L);
        authGroup.setName("auth_group");

        Mockito.doReturn(Optional.of(snmpGroup)).when(snmpGroupService).isSnmpGroupExists(SNMP_GROUP_NAME);
        Mockito.doReturn(Optional.of(authGroup)).when(authGroupService).isAuthGroupExist(AUTH_GROUP_NAME);

        NodeProfileData nodeProfile = new NodeProfileData();
        nodeProfile.setName("nodeProfileUpdateTest");
        nodeProfile.setDefaultAuthGroup(AUTH_GROUP_NAME);
        nodeProfile.setDefaultSnmpGroup(SNMP_GROUP_NAME);

        NodeListData nodeList = new NodeListData();
        nodeList.setNodeIp("10.225.120.10");
        nodeList.setNodeManagementIp("172.168.1.1");


        NodeFilterData nodeFilter = new NodeFilterData();
        nodeFilter.setType(NodeFilterData.Type.HOST_REGEX);
        nodeFilter.setCondition(NodeFilterData.Condition.INCLUDE);
        nodeFilter.setValue("host*");

        Set<NodeListData> nodeListSet = new HashSet<>();
        nodeListSet.add(nodeList);

        nodeProfile.setNodeLists(nodeListSet);
        nodeProfile.setNodeFilters(Set.of(nodeFilter));

        nodeProfileService.addNodeProfile(nodeProfile);

        logger.info("Node profile " + nodeProfile);
        Assertions.assertEquals(1, nodeProfile.getNodeLists().size());

        NodeListData nodeList2 = new NodeListData();
        nodeList2.setNodeIp("10.225.120.20");
        nodeList2.setNodeManagementIp("172.168.1.1");

        nodeListSet.add(nodeList2);

        nodeProfileService.updateNodeProfile(nodeProfile);
        Assertions.assertEquals(2, nodeProfile.getNodeLists().size());

        logger.info("Node profile data with 2 Node Lists : " + nodeProfile);
        nodeProfile.getNodeLists().forEach(nodeListData -> {
            Assertions.assertTrue(nodeListData.isAuthGroupMismatch());
            Assertions.assertTrue(nodeListData.isSnmpGroupMismatch());
        });

        nodeList.setAuthGroupName("auth_group");
        nodeList.setSnmpGroupName("snmp_group");

        nodeList2.setAuthGroupName("auth_group");
        nodeList2.setSnmpGroupName("snmp_group");

        nodeProfileService.updateNodeProfile(nodeProfile);
        logger.info("Node profile Data with updated authGroup snmpGroup node list " + nodeProfile);
        nodeProfile.getNodeLists().forEach(nodeListData -> {
            Assertions.assertFalse(nodeListData.isAuthGroupMismatch());
            Assertions.assertFalse(nodeListData.isSnmpGroupMismatch());
        });

        Set<NodeListData> nodeListSet2 = new HashSet<>();
        nodeListSet2.add(nodeList);

        nodeProfile.setNodeLists(nodeListSet2);
        nodeProfileService.updateNodeProfile(nodeProfile);
        logger.info("Node profile Data with deleted node list " + nodeProfile);
        Assertions.assertEquals(1, nodeProfile.getNodeLists().size());

    }

    @Test
    @DisplayName("Verify the node profile update use case with CRUD NodeFilter operations")
    void testUpdateNodeFilter() {

        SnmpGroup snmpGroup = new SnmpGroup();
        snmpGroup.setId(1L);
        snmpGroup.setName("snmp_group");

        AuthGroup authGroup = new AuthGroup();
        authGroup.setId(1L);
        authGroup.setName("auth_group");

        Mockito.doReturn(Optional.of(snmpGroup)).when(snmpGroupService).isSnmpGroupExists(SNMP_GROUP_NAME);
        Mockito.doReturn(Optional.of(authGroup)).when(authGroupService).isAuthGroupExist(AUTH_GROUP_NAME);

        NodeProfileData nodeProfile = new NodeProfileData();
        nodeProfile.setName("nodeProfileUpdateNodeFilterTest");
        nodeProfile.setDefaultAuthGroup(AUTH_GROUP_NAME);
        nodeProfile.setDefaultSnmpGroup(SNMP_GROUP_NAME);

        NodeListData nodeList = new NodeListData();
        nodeList.setNodeIp("10.225.120.10");
        nodeList.setNodeManagementIp("172.168.1.1");


        NodeFilterData nodeFilter = new NodeFilterData();
        nodeFilter.setType(NodeFilterData.Type.HOST_REGEX);
        nodeFilter.setCondition(NodeFilterData.Condition.INCLUDE);
        nodeFilter.setValue("host*");

        NodeFilterData nodeFilter2 = new NodeFilterData();
        nodeFilter2.setType(NodeFilterData.Type.HOST_REGEX);
        nodeFilter2.setCondition(NodeFilterData.Condition.INCLUDE);
        nodeFilter2.setValue("host2*");

        Set<NodeListData> nodeListSet = new HashSet<>();
        nodeListSet.add(nodeList);

        Set<NodeFilterData> nodeFilterDataSet = new HashSet<>();
        nodeFilterDataSet.add(nodeFilter);
        nodeFilterDataSet.add(nodeFilter2);

        nodeProfile.setNodeLists(nodeListSet);
        nodeProfile.setNodeFilters(nodeFilterDataSet);

        nodeProfileService.addNodeProfile(nodeProfile);

        logger.info("Node profile " + nodeProfile);
        Assertions.assertEquals(2, nodeProfile.getNodeFilters().size());
        nodeProfile.getNodeFilters().forEach(nodeFilterData -> Assertions.assertNotNull(nodeFilterData.getId()));

        nodeFilter.setValue("host1*");

        NodeFilterData nodeFilter3 = new NodeFilterData();
        nodeFilter3.setType(NodeFilterData.Type.HOST_REGEX);
        nodeFilter3.setCondition(NodeFilterData.Condition.INCLUDE);
        nodeFilter3.setValue("host3*");

        nodeFilterDataSet.add(nodeFilter3);

        nodeProfileService.updateNodeProfile(nodeProfile);
        logger.info("Node profile Data with added node filter " + nodeProfile);
        Assertions.assertEquals(3, nodeProfile.getNodeFilters().size());
        nodeProfile.getNodeFilters().forEach(nodeFilterData -> Assertions.assertNotNull(nodeFilterData.getId()));

        Set<NodeFilterData> nodeFilterDataSet2 = new HashSet<>();
        nodeFilterDataSet2.add(nodeFilter);
        nodeFilterDataSet2.add(nodeFilter3);

        nodeProfile.setNodeFilters(nodeFilterDataSet2);
        nodeProfileService.updateNodeProfile(nodeProfile);

        logger.info("Node profile Data with deleted node filter " + nodeProfile);
        Assertions.assertEquals(2, nodeProfile.getNodeFilters().size());
        nodeProfile.getNodeFilters().forEach(nodeFilterData -> Assertions.assertNotNull(nodeFilterData.getId()));

        nodeFilter2.setId(null);
        nodeFilterDataSet2.add(nodeFilter2);

        NodeFilterData nodeFilter4 = new NodeFilterData();
        nodeFilter4.setType(NodeFilterData.Type.HOST_REGEX);
        nodeFilter4.setCondition(NodeFilterData.Condition.INCLUDE);
        nodeFilter4.setValue("host4*");

        nodeFilterDataSet2.add(nodeFilter4);
        nodeProfileService.updateNodeProfile(nodeProfile);

        logger.info("Node profile Data with 2 added node filter " + nodeProfile);
        Assertions.assertEquals(4, nodeProfile.getNodeFilters().size());
        nodeProfile.getNodeFilters().forEach(nodeFilterData -> Assertions.assertNotNull(nodeFilterData.getId()));

    }

    @Test
    @DisplayName("Verify the node profile basic info update use case with CRUD NodeFilter operations")
    void testUpdateNodProfileInofNodeFilter() {

        SnmpGroup snmpGroup = new SnmpGroup();
        snmpGroup.setId(1L);
        snmpGroup.setName("snmp_group");

        AuthGroup authGroup = new AuthGroup();
        authGroup.setId(1L);
        authGroup.setName("auth_group");

        Mockito.doReturn(Optional.of(snmpGroup)).when(snmpGroupService).isSnmpGroupExists(SNMP_GROUP_NAME);
        Mockito.doReturn(Optional.of(authGroup)).when(authGroupService).isAuthGroupExist(AUTH_GROUP_NAME);

        NodeProfileData nodeProfile = new NodeProfileData();
        nodeProfile.setName("nodeProfileUpdateWithNodeFilterTest");
        nodeProfile.setDefaultAuthGroup(AUTH_GROUP_NAME);
        nodeProfile.setDefaultSnmpGroup(SNMP_GROUP_NAME);

        NodeListData nodeList = new NodeListData();
        nodeList.setNodeIp("10.225.120.10");
        nodeList.setNodeManagementIp("172.168.1.1");


        NodeFilterData nodeFilter = new NodeFilterData();
        nodeFilter.setType(NodeFilterData.Type.HOST_REGEX);
        nodeFilter.setCondition(NodeFilterData.Condition.INCLUDE);
        nodeFilter.setValue("host*");

        NodeFilterData nodeFilter2 = new NodeFilterData();
        nodeFilter2.setType(NodeFilterData.Type.HOST_REGEX);
        nodeFilter2.setCondition(NodeFilterData.Condition.INCLUDE);
        nodeFilter2.setValue("host2*");

        Set<NodeListData> nodeListSet = new HashSet<>();
        nodeListSet.add(nodeList);

        Set<NodeFilterData> nodeFilterDataSet = new HashSet<>();
        nodeFilterDataSet.add(nodeFilter);
        nodeFilterDataSet.add(nodeFilter2);

        nodeProfile.setNodeLists(nodeListSet);
        nodeProfile.setNodeFilters(nodeFilterDataSet);

        nodeProfileService.addNodeProfile(nodeProfile);

        logger.info("Node profile " + nodeProfile);
        Assertions.assertEquals(2, nodeProfile.getNodeFilters().size());
        nodeProfile.getNodeFilters().forEach(nodeFilterData -> Assertions.assertNotNull(nodeFilterData.getId()));

        nodeFilter.setValue("host1*");

        NodeFilterData nodeFilter3 = new NodeFilterData();
        nodeFilter3.setType(NodeFilterData.Type.HOST_REGEX);
        nodeFilter3.setCondition(NodeFilterData.Condition.INCLUDE);
        nodeFilter3.setValue("host3*");

        nodeFilterDataSet.add(nodeFilter3);

        nodeProfileService.updateNodeProfileNodeFilter(nodeProfile);
        logger.info("Node profile Data with added node filter " + nodeProfile);
        Assertions.assertEquals(3, nodeProfile.getNodeFilters().size());
        nodeProfile.getNodeFilters().forEach(nodeFilterData -> Assertions.assertNotNull(nodeFilterData.getId()));

        Set<NodeFilterData> nodeFilterDataSet2 = new HashSet<>();
        nodeFilterDataSet2.add(nodeFilter);
        nodeFilterDataSet2.add(nodeFilter3);

        nodeProfile.setNodeFilters(nodeFilterDataSet2);
        nodeProfileService.updateNodeProfileNodeFilter(nodeProfile);

        logger.info("Node profile Data with deleted node filter " + nodeProfile);
        Assertions.assertEquals(2, nodeProfile.getNodeFilters().size());
        nodeProfile.getNodeFilters().forEach(nodeFilterData -> Assertions.assertNotNull(nodeFilterData.getId()));

        nodeFilter2.setId(null);
        nodeFilterDataSet2.add(nodeFilter2);

        NodeFilterData nodeFilter4 = new NodeFilterData();
        nodeFilter4.setType(NodeFilterData.Type.HOST_REGEX);
        nodeFilter4.setCondition(NodeFilterData.Condition.INCLUDE);
        nodeFilter4.setValue("host4*");

        nodeFilterDataSet2.add(nodeFilter4);
        nodeProfileService.updateNodeProfileNodeFilter(nodeProfile);

        logger.info("Node profile Data with 2 added node filter " + nodeProfile);
        Assertions.assertEquals(4, nodeProfile.getNodeFilters().size());
        nodeProfile.getNodeFilters().forEach(nodeFilterData -> Assertions.assertNotNull(nodeFilterData.getId()));

        Optional<NodeProfileData> optionalNodeProfileData = nodeProfileService.getNodeProfile(nodeProfile.getId());
        Assertions.assertTrue(optionalNodeProfileData.isPresent());
        Assertions.assertEquals(1, optionalNodeProfileData.get().getNodeLists().size());
        Assertions.assertEquals(4, optionalNodeProfileData.get().getNodeFilters().size());

        optionalNodeProfileData = nodeProfileService.getNodeProfileInfo(nodeProfile.getId());
        Assertions.assertTrue(optionalNodeProfileData.isPresent());
        Assertions.assertEquals(0, optionalNodeProfileData.get().getNodeLists().size());
        Assertions.assertEquals(0, optionalNodeProfileData.get().getNodeFilters().size());


    }


    @Test
    @DisplayName("Get all the Node profile names for landing page")
    void testGetAllNodeProfile() {

        Mockito.doReturn(Optional.of("snmp_group")).when(snmpGroupService).getSnmpGroupNameById(any());
        Mockito.doReturn(Optional.of("auth_group")).when(authGroupService).getAuthGroupNameById(any());


        List<AllNodeProfileData> allNodeProfileDataList = nodeProfileService.getAllNodeProfileData();
        logger.info("Node file size : " + allNodeProfileDataList.size());
        allNodeProfileDataList.forEach(allNodeProfileData -> {
            logger.info("Node profile " + allNodeProfileData);
            Optional<NodeProfileData> nodeProfileData = nodeProfileService.getNodeProfile(allNodeProfileData.getId());
            Assertions.assertTrue(nodeProfileData.isPresent());
            logger.info("Node profile " + nodeProfileData.get());
        });
    }

    @Test
    @DisplayName("Delete the node profile")
    void testDeleteNodeProfile(){

        Mockito.doReturn(Optional.of(new SnmpGroup())).when(snmpGroupService).isSnmpGroupExists(SNMP_GROUP_NAME);
        Mockito.doReturn(Optional.of(new AuthGroup())).when(authGroupService).isAuthGroupExist(AUTH_GROUP_NAME);

        NodeProfileData nodeProfile = new NodeProfileData();
        nodeProfile.setName("nodeProfileDeleteTest");
        nodeProfile.setDefaultAuthGroup(AUTH_GROUP_NAME);
        nodeProfile.setDefaultSnmpGroup(SNMP_GROUP_NAME);

        NodeListData nodeList = new NodeListData();
        nodeList.setNodeIp("10.225.120.10");
        nodeList.setNodeManagementIp("172.168.1.1");
        nodeList.setAuthGroupName("auth_group_2");
        nodeList.setSnmpGroupName("snmp_group_2");

        NodeFilterData nodeFilter = new NodeFilterData();
        nodeFilter.setType(NodeFilterData.Type.HOST_REGEX);
        nodeFilter.setCondition(NodeFilterData.Condition.INCLUDE);
        nodeFilter.setValue("host*");

        nodeProfile.setNodeLists(Set.of(nodeList));
        nodeProfile.setNodeFilters(Set.of(nodeFilter));

        nodeProfileService.addNodeProfile(nodeProfile);

        logger.info("Node profile " + nodeProfile + " Node List Id " + nodeProfile.getNodeLists().size());

        Assertions.assertNotNull(nodeProfile.getId());
        nodeProfile.getNodeLists().forEach(nodeListData -> {
            logger.info(nodeListData.isAuthGroupMismatch());
            logger.info(nodeListData.isSnmpGroupMismatch());
        });

        Optional<Long> optionalId = nodeProfileService.deleteNodeProfile(nodeProfile.getId());
        Assertions.assertTrue(optionalId.isPresent());
        Assertions.assertEquals(nodeProfile.getId(), optionalId.get());
    }

    @Test
    @DisplayName("Test empty default auth and snmp group node profile.Verify the override flag.")
    void testDefaultAuthSnmpAndOverride(){

        Mockito.doReturn(Optional.of(new SnmpGroup())).when(snmpGroupService).isSnmpGroupExists(SNMP_GROUP_NAME);
        Mockito.doReturn(Optional.of(new AuthGroup())).when(authGroupService).isAuthGroupExist(AUTH_GROUP_NAME);

        NodeProfileData nodeProfile = new NodeProfileData();
        nodeProfile.setName("nodeProfileDefaultSnmpAuth");

        NodeListData nodeList = new NodeListData();
        nodeList.setNodeIp("10.225.120.10");
        nodeList.setNodeManagementIp("172.168.1.1");
        nodeList.setAuthGroupName("auth_group_2");
        nodeList.setSnmpGroupName("snmp_group_2");

        NodeFilterData nodeFilter = new NodeFilterData();
        nodeFilter.setType(NodeFilterData.Type.HOST_REGEX);
        nodeFilter.setCondition(NodeFilterData.Condition.INCLUDE);
        nodeFilter.setValue("host*");

        nodeProfile.setNodeLists(Set.of(nodeList));
        nodeProfile.setNodeFilters(Set.of(nodeFilter));

        Assertions.assertThrows(CustomException.class, ()->nodeProfileService.addNodeProfile(nodeProfile));
        nodeProfile.setDefaultAuthGroup(AUTH_GROUP_NAME);

        Assertions.assertThrows(CustomException.class, ()->nodeProfileService.addNodeProfile(nodeProfile));
        nodeProfile.setDefaultSnmpGroup(SNMP_GROUP_NAME);

        Assertions.assertDoesNotThrow(()->nodeProfileService.addNodeProfile(nodeProfile));
        logger.info("Node profile " + nodeProfile + " Node List Id " + nodeProfile.getNodeLists().size());

        Assertions.assertNotNull(nodeProfile.getId());
        nodeProfile.getNodeLists().forEach(nodeListData -> {
            logger.info(nodeListData.isAuthGroupMismatch());
            logger.info(nodeListData.isSnmpGroupMismatch());
        });

        Assertions.assertDoesNotThrow(()->nodeProfileService.addNodeProfile(nodeProfile, true));
        Assertions.assertThrows(CustomException.class, ()->nodeProfileService.addNodeProfile(nodeProfile));

        Optional<Long> optionalId = nodeProfileService.deleteNodeProfile(nodeProfile.getId());
        Assertions.assertTrue(optionalId.isPresent());
        Assertions.assertEquals(nodeProfile.getId(), optionalId.get());
    }


}
