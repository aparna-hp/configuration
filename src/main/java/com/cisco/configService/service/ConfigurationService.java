package com.cisco.configService.service;

import com.cisco.collectionService.utils.StringUtil;
import com.cisco.configService.entity.ImportHistory;
import com.cisco.configService.exception.CustomException;
import com.cisco.configService.migration.MigrationService;
import com.cisco.configService.model.AllConfigurations;
import com.cisco.configService.model.composer.NetworkDataView;
import com.cisco.configService.model.preConfig.*;
import com.cisco.configService.repository.ImportHistoryRepository;
import com.cisco.workflowmanager.JobInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
public class ConfigurationService {

    @Autowired
    SnmpGroupService snmpGroupService;

    @Autowired
    AuthGroupService authGroupService;

    @Autowired
    AgentService agentService;

    @Autowired
    NodeProfileService nodeProfileService;

    @Autowired
    NetworkService networkService;

    @Autowired
    SchedulerService schedulerService;

    @Autowired
    MigrationService migrationService;

    @Autowired
    ImportHistoryRepository importHistoryRepository;

    public static String NEW_LINE = System.lineSeparator();

    public ImportHistory importConfigurations(MultipartFile file, ImportHistory.Type importType, boolean override){
        if(isImportInProgress()){
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Please wait till the existing import completes.",
                    "An import is in progress. Please wait.");
        }
        ImportHistory importHistory = new ImportHistory();
        importHistory.setType(importType);
        importHistory.setStatus(ImportHistory.Status.IN_PROGRESS);
        importHistory.setStartTime(System.currentTimeMillis());
        ImportHistory savedHistory = importHistoryRepository.save(importHistory);
        log.debug("Saved History ::" + savedHistory);

        new Thread(() -> {
            String errorReport;
            if(null != importType && importType.equals(ImportHistory.Type.WAE)){
                errorReport = migrateConfigurations(file, override);
            } else {
                errorReport = importCPConfigurations(file, override);
            }
            if(StringUtil.isEmpty(errorReport)){
                savedHistory.setStatus(ImportHistory.Status.SUCCESS);
            } else {
                savedHistory.setStatus(ImportHistory.Status.FAILED);
            }
            importHistory.setFailureReport(errorReport);
            importHistory.setEndTime(System.currentTimeMillis());

            importHistoryRepository.save(savedHistory);
            log.debug("Saved History ::" + savedHistory);
        }).start();

        return savedHistory;
    }

    public String importCPConfigurations(MultipartFile configFile, boolean override) {
        StringBuilder configStr = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(configFile.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                configStr.append(line);
            }

            log.debug("Configurations to be imported ::" + configStr);
            AllConfigurations configurations = new ObjectMapper().readValue(configStr.toString(), AllConfigurations.class);
            return importCPConfigurations(configurations, override);
        } catch (Exception e) {
            log.error("Error importing the config file ", e);
            return e.getMessage();
        }

    }

    public String importCPConfigurations(AllConfigurations allConfigurations, boolean... override) {
        StringBuilder importDetails = new StringBuilder();
        AllConfigurations errorConfigurations = new AllConfigurations();
        boolean status = true;

        log.info("Adding snmp group of size " + allConfigurations.getSnmpGroupDataList().size());
        ObjectMapper objectMapper = new ObjectMapper();

        for (SnmpGroupData snmpGroupData : allConfigurations.getSnmpGroupDataList()) {
            try {
                snmpGroupService.addSnmpGroup(snmpGroupData, override);
                importDetails.append(" Successfully imported snmp group configuration ::")
                        .append(snmpGroupData).append(NEW_LINE);
            } catch (Exception e) {
                errorConfigurations.getSnmpGroupDataList().add(snmpGroupData);
                status = false;
                log.error("Error importing snmp group configuration + " + snmpGroupData, e);
            }
        }

        log.info("Adding Auth group of size " + allConfigurations.getAuthGroupDataList().size());
        for (AuthGroupData authGroupData : allConfigurations.getAuthGroupDataList()) {
            try {
                authGroupService.addAuthGroup(authGroupData,override);
                importDetails.append(" Successfully imported auth group configuration ::")
                        .append(authGroupData).append(NEW_LINE);
            } catch (Exception e) {
                errorConfigurations.getAuthGroupDataList().add(authGroupData);
                status = false;
                log.error("Error importing auth group configuration " + authGroupData, e);
            }
        }

        log.info("Adding agents of size " + allConfigurations.getAgentDataList().size());
        for (AgentData agentData : allConfigurations.getAgentDataList()) {
            try {
                agentService.addAgent(agentData, override);
                importDetails.append(" Successfully imported agent configuration ::")
                        .append(agentData).append(NEW_LINE);
            } catch (Exception e) {
                errorConfigurations.getAgentDataList().add(agentData);
                status = false;
                log.error("Error importing agent configuration " + agentData, e);
            }
        }

        log.info("Adding node profile of size " + allConfigurations.getNodeProfileDataList().size());
        for (NodeProfileData nodeProfileData : allConfigurations.getNodeProfileDataList()) {
            try {
                nodeProfileService.addNodeProfile(nodeProfileData,override);
                importDetails.append(" Successfully imported node profile configuration + ")
                        .append(nodeProfileData).append(NEW_LINE);
            } catch (Exception e) {
                errorConfigurations.getNodeProfileDataList().add(nodeProfileData);
                status = false;
                log.error("Error importing nodeProfile configuration " + nodeProfileData, e);
            }
        }

        log.info("Adding networks of size " + allConfigurations.getNetworkDataList().size());
        for (NetworkDataView networkData : allConfigurations.getNetworkDataList()) {
            try {
                List<AgentData> agentDataList = agentService.getAllAgents();
                for(AgentData agentData : agentDataList) {
                    log.debug("Agents migrated ::  " + agentData);
                }
                networkService.importNetwork(networkData, override);
                importDetails.append(" Successfully imported network configuration + ")
                        .append(networkData).append(NEW_LINE);
            } catch (Exception e) {
                errorConfigurations.getNetworkDataList().add(networkData);
                status = false;
                log.error("Error importing network configuration " + networkData, e);
            }
        }

        log.info("Adding Schedulers of size " + allConfigurations.getSchedulerConfigDataList().size());
        try {
            errorConfigurations.getSchedulerConfigDataList().addAll(schedulerService.importScheduler(
                    allConfigurations.getSchedulerConfigDataList()));
            if(errorConfigurations.getSchedulerConfigDataList().size() > 0){
                status = false;
            }
        } catch (Exception e) {
            status = false;
            log.error("Error importing scheduler configuration ", e);
        }

        log.debug("Success report :: " + importDetails);
        String errorDetails = "";
        if(!status) {
            try {
                objectMapper.registerModule(new JavaTimeModule());
                errorDetails = objectMapper.writeValueAsString(errorConfigurations);
            } catch (JsonProcessingException e) {
                log.error("Error generating the error configuration string. " + errorConfigurations, e);
            }
        }
        log.debug("Error report " + errorDetails);
        return errorDetails;
    }

    public String exportConfig() {
        AllConfigurations allConfigurations = new AllConfigurations();
        try {
            allConfigurations.setSnmpGroupDataList(snmpGroupService.getAllSnmpGroups());
        } catch (Exception e) {
            log.error("Error exporting snmp group configuration", e);
        }

        try {
            allConfigurations.setAuthGroupDataList(authGroupService.getAllAuthGroups());
        } catch (Exception e) {
            log.error("Error exporting auth group configuration ", e);
        }

        try {
            allConfigurations.setAgentDataList(agentService.getAllAgents());
        } catch (Exception e) {
            log.error("Error exporting agent configuration ", e);
        }

        try {
            allConfigurations.setNodeProfileDataList(nodeProfileService.getAllNodeProfile());
        } catch (Exception e) {
            log.error("Error exporting nodeProfile configuration ", e);
        }

        try {
            allConfigurations.setNetworkDataList(networkService.getAllNetworks());
        } catch (Exception e) {
            log.error("Error exporting network configuration ", e);
        }

        try {
            for(NetworkDataView networkData : allConfigurations.getNetworkDataList()) {
                allConfigurations.getSchedulerConfigDataList().addAll(schedulerService.getSchedulersOfNetwork(networkData.getId()));
            }
        } catch (Exception e) {
            log.error("Error exporting scheduler configuration ", e);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.registerModule(new JavaTimeModule());
            return objectMapper.writeValueAsString(allConfigurations);
        } catch (JsonProcessingException e) {
            log.error("Error exporting the configuration ::" + allConfigurations, e);
        }
        return "";
    }

    public void clearConfigurations() {

        try {
            log.info("Deleting all the schedulers ");
            List<JobInfo> jobInfoList = schedulerService.getAllSchedulers();
            jobInfoList.forEach(jobInfo -> schedulerService.deleteScheduler(jobInfo.getId()));
        } catch (Exception e) {
            log.error("Error deleting scheduler configuration ", e);
        }

        try {
            log.info("Deleting all the networks ");
            List<NetworkDataView> networkDataList = networkService.getAllNetworks();
            networkDataList.forEach(networkData -> networkService.deleteNetwork(networkData.getId()));
        } catch (Exception e) {
            log.error("Error deleting network configuration ", e);
        }

        try {
            log.info("Delete all the node profiles");
            List<NodeProfileData> nodeProfileDataList = nodeProfileService.getAllNodeProfile();
            nodeProfileDataList.forEach(nodeProfileData -> nodeProfileService.deleteNodeProfile(nodeProfileData.getId()));
        } catch (Exception e) {
            log.error("Error deleting node profile configuration ", e);
        }

        try {
            log.info("Delete all the agents");
            agentService.deleteAll();
        } catch (Exception e) {
            log.error("Error deleting agent configuration ", e);
        }
        try {
            log.info("Delete all the auth groups");
            authGroupService.deleteAll();
        } catch (Exception e) {
            log.error("Error deleting auth group configuration ", e);
        }
        try {
            log.info("Delete all the snmp groups");
            snmpGroupService.deleteAll();
        } catch (Exception e) {
            log.error("Error deleting snmp group configuration ", e);
        }
    }

    public void deleteImportHistory(Long historyId){
        log.info("Delete the import history with id " + historyId);
        if(null == historyId){
            log.info("Clear all the import history.");
            importHistoryRepository.deleteAll();
            return;
        }

        importHistoryRepository.deleteById(historyId);
    }

    public String migrateConfigurations(MultipartFile file, boolean... override) {
        try {
            AllConfigurations configurations = migrationService.migrateConfigurations(file);
            return importCPConfigurations(configurations, override);
        } catch (Exception e) {
            log.error("Error migrating the previous configurations", e);
            return e.getMessage();
        }
    }

    public List<ImportHistory> getHistory() {
        List<ImportHistory> importHistoryList = (List<ImportHistory>) importHistoryRepository.findAll();
        importHistoryList.sort(Comparator.comparing(ImportHistory::getId).reversed());
        return importHistoryList;
    }

    public boolean isImportInProgress(){
        log.info("Verify if any imports are in progess.");
        List<Long> inProgressImports = importHistoryRepository.getInProgressImports();
        log.debug("In progress import size " + inProgressImports.size());
        return inProgressImports.size() > 0;
    }

}
