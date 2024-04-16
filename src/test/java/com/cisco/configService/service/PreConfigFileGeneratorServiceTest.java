package com.cisco.configService.service;

import com.cisco.configService.enums.SnmpSecurityLevel;
import com.cisco.configService.model.preConfig.AuthGroupData;
import com.cisco.configService.model.preConfig.NodeListData;
import com.cisco.configService.model.preConfig.NodeProfileData;
import com.cisco.configService.model.preConfig.SnmpGroupData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@SpringBootTest
public class PreConfigFileGeneratorServiceTest {

    @Autowired
    PreConfigFileGeneratorService preConfigFileGeneratorService;

    @MockBean
    FileGatewayService fileGatewayService;

    @MockBean
    AuthGroupService authGroupService;

    @MockBean
    SnmpGroupService snmpGroupService;

    private static final Logger logger =
            LogManager.getLogger(PreConfigFileGeneratorServiceTest.class);

    @Test
    public void testManageIp(){
        Mockito.doReturn(true).when(fileGatewayService).saveFileStream(Mockito.any(),
                Mockito.any(),Mockito.any());
        NodeProfileData nodeProfileData = new NodeProfileData();
        nodeProfileData.setName("ManageIpNodeProfile");
        nodeProfileData.setId(123L);

        Set<NodeListData> nodeList = new HashSet<>();
        for(int i=0; i<4; i++) {
            NodeListData nodeListData = new NodeListData();
            switch (i) {
                case 0:
                    nodeListData.setNodeIp("10.0.225.43");
                    nodeListData.setNodeManagementIp("10.225.120.43");
                    break;
                case 1:
                    nodeListData.setNodeIp("10.0.225.44");
                    nodeListData.setNodeManagementIp("10.225.120.44");
                    break;
                case 2:
                    nodeListData.setNodeIp("10.0.225.62");
                    nodeListData.setNodeManagementIp("10.225.120.62");
                    break;
                case 3:
                    nodeListData.setNodeIp("10.0.225.63");
                    nodeListData.setNodeManagementIp("10.225.120.63");
                    break;
            }
            nodeListData.setAuthGroupName("auth_group");
            nodeListData.setSnmpGroupName("snmp_group");
            nodeList.add(nodeListData);
        }

        nodeProfileData.setNodeLists(nodeList);

        logger.info("Generating Manage IP ");
        Assertions.assertTrue(preConfigFileGeneratorService.generateManageIpFile(nodeProfileData));
    }

    @Test
    public void testAuthFile() {

        AuthGroupData authGroupData = new AuthGroupData();
        authGroupData.setName("auth_group");
        authGroupData.setLoginType(AuthGroupData.LoginType.TELNET);
        authGroupData.setUsername("cisco");
        authGroupData.setPassword("cisco");
        authGroupData.setConfirmPassword("cisco");

        SnmpGroupData snmpGroup = new SnmpGroupData();
        snmpGroup.setName("snmp_group");
        snmpGroup.setSnmpType(SnmpGroupData.SnmpType.SNMPv2c);
        snmpGroup.setUsername("cisco");
        snmpGroup.setRoCommunity("cisco");

        Mockito.doReturn(true).when(fileGatewayService).saveFileStream(Mockito.any(),
                Mockito.any(), Mockito.any());

        Mockito.doReturn(Optional.of(authGroupData)).when(authGroupService).getAuthGroupByName(Mockito.any());

        Mockito.doReturn(Optional.of(snmpGroup)).when(snmpGroupService).getSnmpGroupByName(Mockito.any());

        NodeProfileData nodeProfileData = new NodeProfileData();
        nodeProfileData.setName("ManageIpNodeProfile");
        nodeProfileData.setId(123L);

        Set<NodeListData> nodeList = new HashSet<>();
        for(int i=0; i<4; i++) {
            NodeListData nodeListData = new NodeListData();
            switch (i) {
                case 0:
                    nodeListData.setNodeIp("10.0.255.43");
                    nodeListData.setNodeManagementIp("10.225.120.43");
                    break;
                case 1:
                    nodeListData.setNodeIp("10.0.255.44");
                    nodeListData.setNodeManagementIp("10.225.120.44");
                    break;
                case 2:
                    nodeListData.setNodeIp("10.0.255.62");
                    nodeListData.setNodeManagementIp("10.225.120.62");
                    break;
                case 3:
                    nodeListData.setNodeIp("10.0.255.63");
                    nodeListData.setNodeManagementIp("10.225.120.63");
                    break;
            }
            nodeListData.setAuthGroupName("auth_group");
            nodeListData.setSnmpGroupName("snmp_group");
            nodeList.add(nodeListData);
        }
        nodeProfileData.setNodeLists(nodeList);

        logger.info("Generating Auth file ");
        Assertions.assertTrue(preConfigFileGeneratorService.generateAuthFile(nodeProfileData));
    }

    @Test
    public void testAuthFileWithDefaultCred() {

        AuthGroupData authGroupData = new AuthGroupData();
        authGroupData.setName("auth_group");
        authGroupData.setLoginType(AuthGroupData.LoginType.TELNET);
        authGroupData.setUsername("cisco");
        authGroupData.setPassword("cisco");
        authGroupData.setConfirmPassword("cisco");

        SnmpGroupData snmpGroup = new SnmpGroupData();
        snmpGroup.setName("snmp_group");
        snmpGroup.setSnmpType(SnmpGroupData.SnmpType.SNMPv2c);
        snmpGroup.setUsername("cisco");
        snmpGroup.setRoCommunity("cisco");

        Mockito.doReturn(true).when(fileGatewayService).saveFileStream(Mockito.any(),
                Mockito.any(), Mockito.any());

        Mockito.doReturn(Optional.of(authGroupData)).when(authGroupService).getAuthGroupByName(Mockito.any());

        Mockito.doReturn(Optional.of(snmpGroup)).when(snmpGroupService).getSnmpGroupByName(Mockito.any());

        NodeProfileData nodeProfileData = new NodeProfileData();
        nodeProfileData.setName("ManageIpNodeProfile");
        nodeProfileData.setId(123L);
        nodeProfileData.setDefaultAuthGroup(authGroupData.getName());
        nodeProfileData.setDefaultSnmpGroup(snmpGroup.getName());

        Set<NodeListData> nodeList = new HashSet<>();
        for(int i=0; i<4; i++) {
            NodeListData nodeListData = new NodeListData();
            switch (i) {
                case 0:
                    nodeListData.setNodeIp("10.0.255.43");
                    nodeListData.setNodeManagementIp("10.225.120.43");
                    break;
                case 1:
                    nodeListData.setNodeIp("10.0.255.44");
                    nodeListData.setNodeManagementIp("10.225.120.44");
                    break;
                case 2:
                    nodeListData.setNodeIp("10.0.255.62");
                    nodeListData.setNodeManagementIp("10.225.120.62");
                    break;
                case 3:
                    nodeListData.setNodeIp("10.0.255.63");
                    nodeListData.setNodeManagementIp("10.225.120.63");
                    break;
            }
            nodeListData.setAuthGroupName("auth_group");
            nodeListData.setSnmpGroupName("snmp_group");
            nodeList.add(nodeListData);
        }
        nodeProfileData.setNodeLists(nodeList);

        logger.info("Generating Auth file ");
        Assertions.assertTrue(preConfigFileGeneratorService.generateAuthFile(nodeProfileData));
    }

    @Test
    public void testAuthFileWithSnmpv3() {

        AuthGroupData authGroupData = new AuthGroupData();
        authGroupData.setName("auth_group");
        authGroupData.setLoginType(AuthGroupData.LoginType.TELNET);
        authGroupData.setUsername("cisco");
        authGroupData.setPassword("cisco");
        authGroupData.setConfirmPassword("cisco");

        SnmpGroupData snmpGroup = new SnmpGroupData();
        snmpGroup.setName("snmp_group");
        snmpGroup.setSnmpType(SnmpGroupData.SnmpType.SNMPv3);
        snmpGroup.setUsername("v3cisco");
        snmpGroup.setAuthenticationProtocol(SnmpGroupData.AuthenticationProtocol.MD5);
        snmpGroup.setAuthenticationPassword("cisco");
        snmpGroup.setEncryptionProtocol(SnmpGroupData.EncryptionProtocol.AES);
        snmpGroup.setEncryptionPassword("cisco");
        snmpGroup.setSecurityLevel(SnmpSecurityLevel.AUTH_PRIV);

        Mockito.doReturn(true).when(fileGatewayService).saveFileStream(Mockito.any(),
                Mockito.any(), Mockito.any());

        Mockito.doReturn(Optional.of(authGroupData)).when(authGroupService).getAuthGroupByName(Mockito.any());

        Mockito.doReturn(Optional.of(snmpGroup)).when(snmpGroupService).getSnmpGroupByName(Mockito.any());

        NodeProfileData nodeProfileData = new NodeProfileData();
        nodeProfileData.setName("ManageIpNodeProfile");
        nodeProfileData.setId(123L);

        Set<NodeListData> nodeList = new HashSet<>();
        for(int i=0; i<4; i++) {
            NodeListData nodeListData = new NodeListData();
            switch (i) {
                case 0:
                    nodeListData.setNodeIp("10.0.255.43");
                    nodeListData.setNodeManagementIp("10.225.120.43");
                    break;
                case 1:
                    nodeListData.setNodeIp("10.0.255.44");
                    nodeListData.setNodeManagementIp("10.225.120.44");
                    break;
                case 2:
                    nodeListData.setNodeIp("10.0.255.62");
                    nodeListData.setNodeManagementIp("10.225.120.62");
                    break;
                case 3:
                    nodeListData.setNodeIp("10.0.255.63");
                    nodeListData.setNodeManagementIp("10.225.120.63");
                    break;
            }
            nodeListData.setAuthGroupName("auth_group");
            nodeListData.setSnmpGroupName("snmp_group");
            nodeList.add(nodeListData);
        }
        nodeProfileData.setNodeLists(nodeList);

        logger.info("Generating Auth file ");
        Assertions.assertTrue(preConfigFileGeneratorService.generateAuthFile(nodeProfileData));
    }

    @Test
    public void testNetAccess(){
        Mockito.doReturn(true).when(fileGatewayService).saveFileStream(Mockito.any(),
                Mockito.any(),Mockito.any());
        Assertions.assertTrue(preConfigFileGeneratorService.generateNetAccess(123L));
    }
}
