package com.cisco.configService.service;

import com.cisco.configService.entity.SnmpGroup;
import com.cisco.configService.enums.SnmpSecurityLevel;
import com.cisco.configService.model.preConfig.SnmpGroupData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SnmpGroupTest {

    @Autowired
    SnmpGroupService snmpGroupService;

    private static final Logger logger = LogManager.getLogger(SnmpGroupTest.class);


    @Test
    @Order(1)
    @DisplayName("Add, Get, Delete the snmp group")
    void deleteAllSnmpGroup(){
        SnmpGroupData snmpGroup = new SnmpGroupData();
        snmpGroup.setName("snmpV2Test");
        snmpGroup.setSnmpType(SnmpGroupData.SnmpType.SNMPv2c);
        snmpGroup.setRoCommunity("communityString");

        snmpGroupService.addSnmpGroup(snmpGroup);

        Assertions.assertNotNull(snmpGroup.getId());
        Assertions.assertNotNull(snmpGroup.getUpdateDate());

        List<SnmpGroupData> snmpGroupDataList = snmpGroupService.getAllSnmpGroups();
        Assertions.assertTrue(snmpGroupDataList.size() > 0);

        Optional<Long> optionalId = snmpGroupService.deleteSnmpGroup(snmpGroup.getId());
        Assertions.assertTrue(optionalId.isPresent());
        Assertions.assertEquals(snmpGroup.getId(), optionalId.get());
    }

    @Test
    @Order(2)
    void testAddSnmpGroup() {
        Optional<SnmpGroupData> optionalSnmpGroupData = snmpGroupService.getSnmpGroupByName("snmpV2Test");
        optionalSnmpGroupData.ifPresent(snmpGroupData -> snmpGroupService.deleteSnmpGroup(snmpGroupData.getId()));

        optionalSnmpGroupData = snmpGroupService.getSnmpGroupByName("snmpv3Test");
        optionalSnmpGroupData.ifPresent(snmpGroupData -> snmpGroupService.deleteSnmpGroup(snmpGroupData.getId()));

        SnmpGroupData snmpGroup = new SnmpGroupData();
        snmpGroup.setName("snmpV2Test");
        snmpGroup.setSnmpType(SnmpGroupData.SnmpType.SNMPv2c);
        snmpGroup.setRoCommunity("communityString");

        snmpGroupService.addSnmpGroup(snmpGroup);

        Assertions.assertNotNull(snmpGroup.getId());

        snmpGroup = new SnmpGroupData();
        snmpGroup.setName("snmpv3Test");
        snmpGroup.setSnmpType(SnmpGroupData.SnmpType.SNMPv3);
        snmpGroup.setSecurityLevel(SnmpSecurityLevel.AUTH_NOPRIV);
        snmpGroup.setUsername("username");
        snmpGroup.setAuthenticationProtocol(SnmpGroupData.AuthenticationProtocol.MD5);
        snmpGroup.setAuthenticationPassword("authPassword");
        snmpGroup.setEncryptionProtocol(SnmpGroupData.EncryptionProtocol.AES);
        snmpGroup.setEncryptionPassword("encryPassword");

        snmpGroupService.addSnmpGroup(snmpGroup);

        Assertions.assertNotNull(snmpGroup.getId());
        Optional<SnmpGroupData> snmpGroupDataOptional = snmpGroupService.getSnmpGroup(snmpGroup.getId());
        Assertions.assertTrue(snmpGroupDataOptional.isPresent());
        logger.info("Snmp group Get = " + snmpGroupDataOptional.get());
        Assertions.assertEquals(snmpGroupDataOptional.get().getAuthenticationPassword(), snmpGroup.getAuthenticationPassword());
        Assertions.assertEquals(snmpGroupDataOptional.get().getEncryptionPassword(), snmpGroup.getEncryptionPassword());
        Assertions.assertEquals(snmpGroupDataOptional.get().getSecurityLevel(), snmpGroup.getSecurityLevel());

    }

    @Test
    @Order(3)
    @DisplayName("Get all the SnmpGroups")
    void testGetAllSnmpGroup() {
        List<SnmpGroupData> snmpGroupDataIterable = snmpGroupService.getAllSnmpGroups();
        Assertions.assertTrue(snmpGroupDataIterable.size() > 0);
        snmpGroupDataIterable.forEach(snmpGroupData -> {
            logger.info("Snmp group " + snmpGroupData);
            Assertions.assertNotNull(snmpGroupData.getId());
            Assertions.assertNotNull(snmpGroupData.getUpdateDate());
        });
    }

    @Test
    @Order(4)
    void testGetSnmpGroupNameByNodeListId() {
        Optional<String> snmpGroupName = snmpGroupService.getSnmpGroupNameById(1L);
        logger.info("Snmp Group Name =" + snmpGroupName);
        Assertions.assertTrue(snmpGroupName.isPresent());
    }

    @Test
    @Order(5)
    void testIsSnmpGroupExists() {
        Optional<SnmpGroup> snmpGroup = snmpGroupService.isSnmpGroupExists("authGroup");
        logger.info("Snmp Group present =" + snmpGroup.isPresent());
        Assertions.assertTrue(snmpGroup.isEmpty());

        snmpGroup = snmpGroupService.isSnmpGroupExists("snmp_group");
        logger.info("Snmp Group present =" + snmpGroup.isPresent());
        Assertions.assertTrue(snmpGroup.isPresent());
    }

}
