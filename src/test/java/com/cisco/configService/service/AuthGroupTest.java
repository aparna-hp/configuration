package com.cisco.configService.service;

import com.cisco.configService.entity.AuthGroup;
import com.cisco.configService.model.preConfig.AuthGroupData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthGroupTest {

    @Autowired
    AuthGroupService authGroupService;

    private static final Logger logger = LogManager.getLogger(AuthGroupTest.class);


    @Test
    @Order(1)
    @DisplayName("Add, Get and Delete the auth group test")
    void testDeleteAuthGroups(){
        AuthGroupData authGroupData = new AuthGroupData();
        authGroupData.setName("authGroupTest");
        authGroupData.setLoginType(AuthGroupData.LoginType.TELNET);
        authGroupData.setUsername("username");
        authGroupData.setPassword("pwsd");
        authGroupData.setConfirmPassword("pwsd");
        authGroupService.addAuthGroup(authGroupData);

        Assertions.assertNotNull(authGroupData.getId());

        Optional<AuthGroupData> authGroupDataOptional = authGroupService.getAuthGroup(authGroupData.getId());
        Assertions.assertTrue(authGroupDataOptional.isPresent());
        Assertions.assertEquals(authGroupDataOptional.get().getPassword(), authGroupData.getPassword());
        Assertions.assertEquals(authGroupDataOptional.get().getConfirmPassword(), authGroupData.getConfirmPassword());

        Optional<Long> optionalId = authGroupService.deleteAuthGroup(authGroupData.getId());
        Assertions.assertTrue(optionalId.isPresent());
        Assertions.assertEquals(authGroupData.getId(), optionalId.get());
    }

    @Test
    @Order(2)
    void testAddAuthGroup() {
        Optional<AuthGroupData> optionalAuthGroupData = authGroupService.getAuthGroupByName("authGroupTest");
        AuthGroupData authGroupData;
        if (optionalAuthGroupData.isPresent()) {
            authGroupData = optionalAuthGroupData.get();
            authGroupService.deleteAuthGroup(authGroupData.getId());
        }
        authGroupData = new AuthGroupData();
        authGroupData.setName("authGroupTest");
        authGroupData.setLoginType(AuthGroupData.LoginType.TELNET);
        authGroupData.setUsername("username");
        authGroupData.setPassword("pwsd");
        authGroupData.setConfirmPassword("pwsd");

        authGroupService.addAuthGroup(authGroupData);

        Assertions.assertNotNull(authGroupData.getId());
        Assertions.assertNotNull(authGroupData.getUpdateDate());
    }

    @Test
    @Order(3)
    @DisplayName("Get all the AuthGroups")
    void testGetAllAuthGroup() {
        List<AuthGroupData> authGroupDataIterable = authGroupService.getAllAuthGroups();
        Assertions.assertTrue(authGroupDataIterable.size() > 0);
        authGroupDataIterable.forEach(authGroupData -> {
            logger.info("Snmp group " + authGroupData);
            Assertions.assertNotNull(authGroupData.getId());
            Assertions.assertNotNull(authGroupData.getUpdateDate());
        });
    }

    @Test
    @Order(5)
    void testGetAuthGroupNameByNodeListId() {
        Optional<String> authGroupName = authGroupService.getAuthGroupNameById(1L);
        logger.info("Snmp Group Name =" + authGroupName);
         Assertions.assertTrue(authGroupName.isPresent());
    }

    @Test
    @Order(4)
    void testIsAuthGroupExists() {
        Optional<AuthGroup> authGroup = authGroupService.isAuthGroupExist("snmpGroup");
        logger.info("Auth Group present =" + authGroup.isPresent());
        Assertions.assertTrue(authGroup.isEmpty());

        authGroup = authGroupService.isAuthGroupExist("auth_group");
        logger.info("Snmp Group present =" + authGroup.isPresent());
        Assertions.assertTrue(authGroup.isPresent());
    }

}
