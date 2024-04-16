package com.cisco.configService.service;

import com.cisco.collectionService.utils.StringUtil;
import com.cisco.configService.enums.AgentTypes;
import com.cisco.configService.migration.MigrationService;
import com.cisco.configService.migration.wae7xConfig.Config;
import com.cisco.configService.migration.wae7xConfig.Nimos;
import com.cisco.configService.migration.wae7xConfig.Wae;
import com.cisco.configService.migration.wae7xConfig.netAccess.*;
import com.cisco.configService.model.AllConfigurations;
import com.cisco.configService.model.preConfig.AgentData;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SpringBootTest
public class MigrateServiceTest {

    @Autowired
    MigrationService migrationService;

    @Autowired
    ConfigurationService configurationService;

    @MockBean
    AgentService agentService;

    @MockBean
    SchedulerService schedulerService;

    private static final Logger logger = LogManager.getLogger(MigrateServiceTest.class);

    @Test
    @DisplayName("Test migration of Auth group")
    public void testMigrateAuth(){
        List<Group> authgroups = new ArrayList<>();

        Assertions.assertEquals(migrationService.migrateAuthGroup(authgroups).size(), 0);

        Group authgroup = new Group();
        authgroup.setName("AuthGroup");
        DefaultMap defaultMap = new DefaultMap();
        defaultMap.setRemoteName("cisco");
        defaultMap.setRemotePassword("cisco");
        defaultMap.setRemoteSecondaryPassword("cisco");
        authgroup.setDefaultMap(defaultMap);

        authgroups.add(authgroup);

        Group authgroup2 = new Group();
        authgroup2.setName("AuthGroup2");
        authgroup2.setDefaultMap(defaultMap);

        authgroups.add(authgroup2);

        Assertions.assertEquals(migrationService.migrateAuthGroup(authgroups).size(), 2);

    }

    @Test
    @DisplayName("Test migration of Snnmp Group")
    public void testMigrateSnmpGroup(){
        List<SnmpGroup> snmpGroups = new ArrayList<>();

        Assertions.assertEquals(migrationService.migrateSnmpGroup(snmpGroups).size(), 0);

        SnmpGroup snmpGroup = new SnmpGroup();
        snmpGroup.setName("Snmp Group");

        SnmpGroupDefaultMap defaultMap = new SnmpGroupDefaultMap();
        defaultMap.setCommunityName("cisco");
        snmpGroup.setDefaultMap(defaultMap);

        snmpGroups.add(snmpGroup);

        Assertions.assertEquals(migrationService.migrateSnmpGroup(snmpGroups).size(), 1);

        SnmpGroup snmpGroup2 = new SnmpGroup();
        snmpGroup2.setName("Snmp Group2");

        SnmpGroupDefaultMap defaultMap2 = new SnmpGroupDefaultMap();
        Usm usm = new Usm();
        usm.setRemoteName("cisco");
        usm.setSecurityLevel("noauth-priv");
        defaultMap2.setUsm(usm);
        snmpGroup2.setDefaultMap(defaultMap2);

        snmpGroups.add(snmpGroup2);

        Assertions.assertEquals(migrationService.migrateSnmpGroup(snmpGroups).size(), 2);

    }

    @Test
    @DisplayName("Test migration of network access")
    public void testNetworkAccessMigration(){
        List<NetworkAccess> networkAccessList = new ArrayList<>();

        Assertions.assertEquals(migrationService.migrateNetworkAccess(networkAccessList).size(), 0);

        NetworkAccess networkAccess = new NetworkAccess();
        networkAccess.setName("NetAccess");
        networkAccess.setDefaultAuthGroup("auth");
        networkAccess.setDefaultSnmpGroup("snmp");

        NodeAccess nodeAccess = new NodeAccess();
        nodeAccess.setIpAddress("1.1.1.1");
        nodeAccess.setIpManage("10.10.10.10");

        NodeAccess nodeAccess2 = new NodeAccess();
        nodeAccess2.setIpAddress("2.2.2.2");
        nodeAccess2.setIpManage("20.20.20.20");

        networkAccess.setNodeAccess(List.of(nodeAccess, nodeAccess2));
        networkAccessList.add(networkAccess);
        Assertions.assertEquals(migrationService.migrateNetworkAccess(networkAccessList).size(), 1);


    }

    @Test
    public void testMigrateConfig() throws IOException {

        File file = new File("./src/test/resources", "wae_networks.xml");
        Path path = Path.of(file.getPath());
        MockMultipartFile multipartFile = new MockMultipartFile("wae_networks.xml", "wae_networks.xml",
                "application/xml", Files.readAllBytes(path));

        AllConfigurations allConfigurations = migrationService.migrateConfigurations(multipartFile);
        String report =  configurationService.importCPConfigurations(allConfigurations, true);
        Assertions.assertTrue(StringUtil.isEmpty(report));
        logger.debug("Configurations imported " + configurationService.exportConfig());

        Assertions.assertEquals(2, allConfigurations.getAuthGroupDataList().size());
        Assertions.assertEquals(3, allConfigurations.getSnmpGroupDataList().size());
        Assertions.assertEquals(1, allConfigurations.getNodeProfileDataList().size());
        Assertions.assertEquals(1, allConfigurations.getNetworkDataList().size());
        Assertions.assertEquals(1, allConfigurations.getSchedulerConfigDataList().size());

        Assertions.assertFalse(allConfigurations.getSchedulerConfigDataList().get(0).isActive());
    }

    @Test
    public void testMigrateConfig2() throws IOException {

        AgentData agentData = new AgentData();
        agentData.setId(1L);
        agentData.setType(AgentTypes.SR_PCE_AGENT);
        agentData.setName("virl");
        Mockito.doReturn(Optional.of(agentData)).when(agentService).getAgentByName(Mockito.anyString());
        Mockito.doReturn(List.of()).when(schedulerService).importScheduler(Mockito.any());


        File file = new File("./src/test/resources", "wae_network_49.xml");
        Path path = Path.of(file.getPath());
        MockMultipartFile multipartFile = new MockMultipartFile("wae_networks.xml", "wae_networks.xml",
                "application/xml", Files.readAllBytes(path));

        AllConfigurations allConfigurations = migrationService.migrateConfigurations(multipartFile);
        String report =  configurationService.importCPConfigurations(allConfigurations, true);

        logger.debug("Configurations imported " + configurationService.exportConfig());

        Assertions.assertEquals(6, allConfigurations.getAuthGroupDataList().size());
        Assertions.assertEquals(3, allConfigurations.getSnmpGroupDataList().size());
        Assertions.assertEquals(2, allConfigurations.getNodeProfileDataList().size());
        Assertions.assertEquals(2, allConfigurations.getAgentDataList().size());
        Assertions.assertEquals(2, allConfigurations.getNetworkDataList().size());
        Assertions.assertEquals(1, allConfigurations.getSchedulerConfigDataList().size());

        Assertions.assertFalse(allConfigurations.getSchedulerConfigDataList().get(0).isActive());
    }

    @Test
    public void testUpgrade713() throws IOException {

        Mockito.doReturn(List.of()).when(schedulerService).importScheduler(Mockito.any());


        File file = new File("./src/test/resources", "wae_networks_713.cfg");
        Path path = Path.of(file.getPath());
        MockMultipartFile multipartFile = new MockMultipartFile("wae_networks.xml", "wae_networks.xml",
                "application/xml", Files.readAllBytes(path));

        AllConfigurations allConfigurations = migrationService.migrateConfigurations(multipartFile);
        String report =  configurationService.importCPConfigurations(allConfigurations, true);

        logger.debug("Configurations imported " + configurationService.exportConfig());

        Assertions.assertEquals(4, allConfigurations.getAuthGroupDataList().size());
        Assertions.assertEquals(2, allConfigurations.getSnmpGroupDataList().size());
        Assertions.assertEquals(2, allConfigurations.getNodeProfileDataList().size());
        Assertions.assertEquals(1, allConfigurations.getNetworkDataList().size());
    }

    @Test
    public void testUpgrade712() throws IOException {

        Mockito.doReturn(List.of()).when(schedulerService).importScheduler(Mockito.any());


        File file = new File("./src/test/resources", "712_wae_config.cfg");
        Path path = Path.of(file.getPath());
        MockMultipartFile multipartFile = new MockMultipartFile("wae_networks.xml", "wae_networks.xml",
                "application/xml", Files.readAllBytes(path));

        AllConfigurations allConfigurations = migrationService.migrateConfigurations(multipartFile);
        String report =  configurationService.importCPConfigurations(allConfigurations, true);

        logger.debug("Configurations imported " + configurationService.exportConfig());

        Assertions.assertEquals(2, allConfigurations.getAuthGroupDataList().size());
        Assertions.assertEquals(2, allConfigurations.getSnmpGroupDataList().size());
        Assertions.assertEquals(1, allConfigurations.getNodeProfileDataList().size());
    }

    @Test
    public void testUpgradeQA() throws IOException {

        Mockito.doReturn(List.of()).when(schedulerService).importScheduler(Mockito.any());


        File file = new File("./src/test/resources", "qa_wae_networks.xml");
        Path path = Path.of(file.getPath());
        MockMultipartFile multipartFile = new MockMultipartFile("wae_networks.xml", "wae_networks.xml",
                "application/xml", Files.readAllBytes(path));

        AllConfigurations allConfigurations = migrationService.migrateConfigurations(multipartFile);
        String report =  configurationService.importCPConfigurations(allConfigurations, true);

        logger.debug("Configurations imported " + configurationService.exportConfig());

        Assertions.assertEquals(3, allConfigurations.getAuthGroupDataList().size());
        Assertions.assertEquals(4, allConfigurations.getSnmpGroupDataList().size());
        Assertions.assertEquals(2, allConfigurations.getNodeProfileDataList().size());
        Assertions.assertEquals(1, allConfigurations.getAgentDataList().size());
        Assertions.assertEquals(1, allConfigurations.getNetworkDataList().size());
    }


    @Test
    public void testUpgrade712WithAggr() throws IOException {

        Mockito.doReturn(List.of()).when(schedulerService).importScheduler(Mockito.any());


        File file = new File("./src/test/resources", "712_wae_config_aggr.cfg");
        Path path = Path.of(file.getPath());
        MockMultipartFile multipartFile = new MockMultipartFile("wae_networks.xml", "wae_networks.xml",
                "application/xml", Files.readAllBytes(path));

        AllConfigurations allConfigurations = migrationService.migrateConfigurations(multipartFile);
        String report =  configurationService.importCPConfigurations(allConfigurations, true);

        logger.debug("Configurations imported " + configurationService.exportConfig());

        Assertions.assertEquals(2, allConfigurations.getAuthGroupDataList().size());
        Assertions.assertEquals(2, allConfigurations.getSnmpGroupDataList().size());
        Assertions.assertEquals(1, allConfigurations.getNodeProfileDataList().size());
    }


    @Test
    public void testUpgrade75() throws IOException {

        Mockito.doReturn(List.of()).when(schedulerService).importScheduler(Mockito.any());


        File file = new File("./src/test/resources", "75_wae_config.cfg");
        Path path = Path.of(file.getPath());
        MockMultipartFile multipartFile = new MockMultipartFile("wae_networks.xml", "wae_networks.xml",
                "application/xml", Files.readAllBytes(path));

        AllConfigurations allConfigurations = migrationService.migrateConfigurations(multipartFile);
        String report =  configurationService.importCPConfigurations(allConfigurations, true);

        logger.debug("Configurations imported " + configurationService.exportConfig());

        Assertions.assertEquals(4, allConfigurations.getAuthGroupDataList().size());
        Assertions.assertEquals(2, allConfigurations.getSnmpGroupDataList().size());
        Assertions.assertEquals(1, allConfigurations.getNodeProfileDataList().size());
        Assertions.assertEquals(3, allConfigurations.getAgentDataList().size());
    }

    @Test
    public void testImportCP1() throws IOException {

        Mockito.doReturn(List.of()).when(schedulerService).importScheduler(Mockito.any());


        File file = new File("./src/test/resources", "config_cp_1.json");
        Path path = Path.of(file.getPath());
        MockMultipartFile multipartFile = new MockMultipartFile("cp.json", "cp.json",
                "application/json", Files.readAllBytes(path));

        String report =configurationService.importCPConfigurations(multipartFile, false);
        logger.debug("Configurations imported " + configurationService.exportConfig());
    }




    @Test
    public void testJaxbModels() throws Exception {
        Config config = new Config();
        Wae wae = new Wae();
        Nimos nimos = new Nimos();
        wae.setNimos(nimos);
        config.setWae(wae);

        // create JAXB context and instantiate marshaller
        JAXBContext context = JAXBContext.newInstance(Config.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        StringWriter stringWriter = new StringWriter();
        Assertions.assertDoesNotThrow(() ->m.marshal(config, stringWriter));
        logger.info(stringWriter);
    }
}
