package com.cisco.configService.service;

import com.cisco.configService.model.AllConfigurations;
import com.cisco.configService.showtech.ShowtechService;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.shadow.com.univocity.parsers.common.input.LineSeparatorDetector;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@SpringBootTest
public class ShowtechServiceTest {

    @Autowired
    ShowtechService showtechService;

    private static final Logger logger = LogManager.getLogger(ShowtechServiceTest.class);

    @TempDir
    static Path tempDir;

    @MockBean
    SchedulerService schedulerService;

    @MockBean
    AgentService agentService;

    @Test
    @DisplayName("Test collect show tech files")
    void testCollectShowtechFiles() throws IOException {
        Mockito.doReturn(List.of()).when(schedulerService).getSchedulersOfNetwork(Mockito.any());
        Mockito.doReturn(List.of()).when(agentService).getAllAgentStatus();

        Path tempBaseDir = tempDir.resolve("showtech");
        Assertions.assertDoesNotThrow(() ->showtechService.collectShowtechFiles(tempBaseDir.toString()));
        try {
            Files.createDirectories(tempBaseDir);
            showtechService.collectShowtechFiles(tempBaseDir.toString());
            Path collectorConfigFile = Path.of(tempBaseDir + ShowtechService.FILE_SEPERATOR + ShowtechService.COLLECTOR_CONFIG_FILE);
            Path agentStatusFile = Path.of(tempBaseDir + ShowtechService.FILE_SEPERATOR + ShowtechService.AGENT_STATUS_FILE);
            Assertions.assertTrue(Files.exists(collectorConfigFile));
            Assertions.assertTrue(Files.exists(agentStatusFile));
        } catch (IOException e) {
            logger.error("Error writing the show tech files to temp directory");
        } finally {
            logger.debug("Deleting the temp directory.");
            FileUtils.deleteDirectory(new File(String.valueOf(tempBaseDir)));
        }

    }
}
