package com.cisco.configService.service;

import com.cisco.collectionService.utils.StringUtil;
import com.cisco.configService.entity.AuthGroup;
import com.cisco.configService.entity.ImportHistory;
import com.cisco.configService.entity.SnmpGroup;
import com.cisco.configService.migration.MigrationService;
import com.cisco.configService.model.AllConfigurations;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class ConfigurationServiceTest {

    @Autowired
    ConfigurationService configurationService;

    @MockBean
    SchedulerService schedulerService;

    @MockBean
    AuthGroupService authGroupService;

    @MockBean
    MigrationService migrationService;

    @MockBean
    SnmpGroupService snmpGroupService;

    private static final Logger logger = LogManager.getLogger(ConfigurationServiceTest.class);

    @Test
    @DisplayName("Export, Delete & Import configurations")
    void testConfig() {

        Mockito.doReturn(List.of()).when(schedulerService).getAllSchedulers();
        Mockito.doReturn(Optional.of(new SnmpGroup())).when(snmpGroupService).isSnmpGroupExists(Mockito.any());
        Mockito.doReturn(Optional.of(new AuthGroup())).when(authGroupService).isAuthGroupExist(Mockito.any());

        String configurations = configurationService.exportConfig();
        logger.debug("Collected configurations " + configurations);
        Assertions.assertTrue(configurations.length() > 0);

        Assertions.assertDoesNotThrow(() -> configurationService.clearConfigurations());
        String clearConfig = configurationService.exportConfig();
        logger.debug("Cleared config " + clearConfig);

        ObjectMapper objectMapper = new ObjectMapper();
        Assertions.assertDoesNotThrow(() -> {
            AllConfigurations allConfigurations = objectMapper.readValue(configurations, AllConfigurations.class);
            String report = configurationService.importCPConfigurations(allConfigurations, true);
            Assertions.assertTrue(StringUtil.isEmpty(report));
        });
    }


    @Test
    @DisplayName("Test Import History")
    void testImportHistory() throws IOException {
        Mockito.doReturn(Optional.of(new SnmpGroup())).when(snmpGroupService).isSnmpGroupExists(Mockito.any());
        Mockito.doReturn(Optional.of(new AuthGroup())).when(authGroupService).isAuthGroupExist(Mockito.any());

        String config = configurationService.exportConfig();
        MockMultipartFile jsonFile = new MockMultipartFile("CP_config.json", "CP_config.json", "application/json",
                config.getBytes());

        ImportHistory importHistory = configurationService.importConfigurations(jsonFile, ImportHistory.Type.CP, true);
        logger.debug("Import History ::" + importHistory);
        Assertions.assertDoesNotThrow(()->configurationService.isImportInProgress());
        Long cpHistoryId = importHistory.getId();
        Assertions.assertDoesNotThrow(()->configurationService.deleteImportHistory(cpHistoryId));

        ObjectMapper objectMapper = new ObjectMapper();
        AllConfigurations allConfigurations = objectMapper.readValue(config, AllConfigurations.class);
        Mockito.doReturn(allConfigurations).when(migrationService).migrateConfigurations(Mockito.any());

        importHistory = configurationService.importConfigurations(jsonFile, ImportHistory.Type.WAE, true);
        logger.debug("Import History ::" + importHistory);

        List<ImportHistory> importHistoryList = configurationService.getHistory();
        Assertions.assertTrue(importHistoryList.size() > 1);
    }

}

