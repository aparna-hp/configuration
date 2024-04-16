package com.cisco.configService.showtech;

import com.cisco.configService.AppPropertiesReader;
import com.cisco.configService.model.preConfig.AllAgentData;
import com.cisco.configService.service.AgentService;
import com.cisco.configService.service.ConfigurationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
@Slf4j
public class ShowtechService {

    @Autowired
    ConfigurationService configurationService;

    @Autowired
    AgentService agentService;

    @Autowired
    AppPropertiesReader appPropertiesReader;

    public final static String FILE_SEPERATOR = "/",
            COLLECTOR_CONFIG_FILE = "collector_configurations.txt",
            AGENT_STATUS_FILE = "agent_status.txt",
            LINE_SEPARATOR = System.lineSeparator();

    public void collectShowtechFiles(String baseDir){
        File baseDirFile = new File(baseDir);
        if(!baseDirFile.exists()){
            return;
        }

        //1. Collect all the collector configuration stored in Datastore.
        String configurations = configurationService.exportConfig();
        try {
            log.debug("Copying the collector configurations to showtech directory");
            Files.writeString(Path.of(baseDir + FILE_SEPERATOR + COLLECTOR_CONFIG_FILE),
                    configurations, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Error writing the collector configurations to showtech directory" , e);
        }

        //2. Collect all the plan files stored by collector tools.
        File sourceDir = new File(appPropertiesReader.getMountDirectory() + appPropertiesReader.getNetworkDirectory());
        log.debug("Verify if Source {}  exits " , sourceDir );
        if(sourceDir.exists()) {
            File destDir = new File(baseDir + FILE_SEPERATOR + appPropertiesReader.getNetworkDirectory());
            try {
                log.debug("Copying the collector plan files to showtech directory");
                FileUtils.copyDirectory(sourceDir, destDir);
            } catch (IOException e) {
                log.error("Error copying the collector plan files to showtech directory ", e);
            }
        }

        //3. Collect all the data files stored by collector tools.
        sourceDir = new File(appPropertiesReader.getMountDirectory() + appPropertiesReader.getDataDirectory());
        log.debug("Verify if Source {}  exits " , sourceDir );
        if(sourceDir.exists()) {
            File destDir = new File(baseDir + FILE_SEPERATOR + appPropertiesReader.getDataDirectory());
            try {
                log.error("Copying the collector data files to showtech directory");
                FileUtils.copyDirectory(sourceDir, destDir);
            } catch (IOException e) {
                log.error("Error copying the collector data files to showtech directory ", e);
            }
        }

        //4. Collect the status of all the agents.
        List<AllAgentData> agentDataList = agentService.getAgentInfo(null);
        StringBuilder agentStatusStr = new StringBuilder();
        agentDataList.forEach(agentData ->
            agentStatusStr.append(agentService.getStatus(agentData.getId())).append(LINE_SEPARATOR)
        );

        try {
            log.error("Copying the agent status to showtech directory");
            Files.writeString(Path.of(baseDir + FILE_SEPERATOR + AGENT_STATUS_FILE),
                    agentStatusStr, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Error writing the agent status to showtech directory" , e);
        }

        //5. Collect SR PCE agent db and record files
        sourceDir = new File(appPropertiesReader.getMountDirectory() + appPropertiesReader.getAgentDirectory()
                            + appPropertiesReader.getSrpceAgentDirectory());
        log.debug("Verify if Source {}  exits " , sourceDir );

        if(sourceDir.exists()) {
            File destDir = new File(baseDir + FILE_SEPERATOR
                    + appPropertiesReader.getAgentDirectory()+appPropertiesReader.getSrpceAgentDirectory());
            try {
                log.debug("Copying the sr pce db and record files to showtech directory");
                FileUtils.copyDirectory(sourceDir, destDir);
            } catch (IOException e) {
                log.error("Error copying the sr pce db and record files to showtech directory ", e);
            }
        }

    }
}
