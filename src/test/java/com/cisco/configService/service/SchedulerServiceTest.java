package com.cisco.configService.service;

import com.cisco.configService.entity.Agents;
import com.cisco.configService.entity.Collector;
import com.cisco.configService.enums.AgentActionTypes;
import com.cisco.configService.enums.AgentTypes;
import com.cisco.configService.enums.CollectorTypes;
import com.cisco.configService.enums.WorkFlowManagerActions;
import com.cisco.configService.model.composer.ApiResponseDetail;
import com.cisco.configService.model.composer.NetworkDataInfo;
import com.cisco.configService.model.scheduler.SchedulerConfigData;
import com.cisco.configService.model.scheduler.TaskConfigData;
import com.cisco.configService.webClient.WorkflowManagerWebClient;
import com.cisco.workflowmanager.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@SpringBootTest
public class SchedulerServiceTest {

    @Autowired
    SchedulerService schedulerService;

    @MockBean
    NetworkService networkService;

    @MockBean
    WorkflowManagerWebClient webClient;

    private static final Logger log =
            LogManager.getLogger(SchedulerServiceTest.class);

    @Test
    @DisplayName("Add Update GET Delete a scheduler")
    void testAddScheduler(){
        JobInfo jobInfo = new JobInfo();
        jobInfo.setName("Collector-Service-scheduler");
        jobInfo.setScheduleStatus(ScheduleStatus.Active);
        jobInfo.setNetworkId(1L);

        //Mocking the response from Workflow Manager.
        Mockito.doReturn(Optional.of(jobInfo)).when(webClient).createScheduler(Mockito.any());
        Mockito.doReturn(Optional.of(jobInfo)).when(webClient).updateScheduler(Mockito.any());
        Mockito.doReturn(Optional.of(new NetworkStatus())).when(webClient).getNetworkStatus(Mockito.any());
        Mockito.doReturn(true).when(webClient).deleteScheduler(Mockito.any());

        SchedulerConfigData schedulerConfigData = new SchedulerConfigData();
        schedulerConfigData.setName("Collector-Service-scheduler");
        List<ApiResponseDetail> apiResponseDetails = schedulerService
                .addUpdateScheduler(List.of(schedulerConfigData), false);
        Assertions.assertEquals(1, apiResponseDetails.size());
        Assertions.assertFalse(apiResponseDetails.get(0).isStatus());

        schedulerConfigData.setNetworkId(1L);
        apiResponseDetails =  schedulerService.addUpdateScheduler(List.of(schedulerConfigData),true);
        Assertions.assertEquals(1, apiResponseDetails.size());
        Assertions.assertFalse(apiResponseDetails.get(0).isStatus());

        TaskConfigData taskConfigData = new TaskConfigData();
        taskConfigData.setCollectorId(1L);

        schedulerConfigData.setTaskConfigDataList(List.of(taskConfigData));
        apiResponseDetails =  schedulerService.addUpdateScheduler(List.of(schedulerConfigData),true);
        Assertions.assertEquals(1, apiResponseDetails.size());
        Assertions.assertTrue(apiResponseDetails.get(0).isStatus());

        NetworkStatus networkStatus = schedulerService.getNetworkStatus(1L);
        Assertions.assertNotNull(networkStatus);

        schedulerService.deleteScheduler(1L);
    }

    @Test
    @DisplayName("Test get Network Status ")
    public void testGetNetworkStatus(){
        NetworkDataInfo allNetworkData = new NetworkDataInfo();
        allNetworkData.setId(0L);
        allNetworkData.setName("StatusNetwork");

        NetworkStatus networkStatus = new NetworkStatus();

        JobTaskHistory jobTaskHistory = new JobTaskHistory(1L);

        Mockito.doReturn(Optional.of(networkStatus)).when(webClient).getNetworkStatus(Mockito.any());

        Mockito.doReturn(Optional.of(jobTaskHistory)).when(webClient).getTaskHistory(Mockito.any());

        Mockito.doReturn(List.of(allNetworkData)).when(networkService).getAllNetworkData();

        Assertions.assertDoesNotThrow(() -> networkService.getNetworkStatus());

        Assertions.assertDoesNotThrow(() -> schedulerService.getTaskHistory(1L));
    }

    @Test
    @DisplayName("Test execute scheduler actions ")
    public void testGetTaskHistory(){
        Mockito.doReturn(true).when(webClient).executeScheduler(Mockito.any());
        Mockito.doReturn(true).when(webClient).pauseScheduler(Mockito.any());
        Mockito.doReturn(true).when(webClient).resumeScheduler(Mockito.any());
        Mockito.doReturn(true).when(webClient).abortScheduler(Mockito.any());

        Assertions.assertDoesNotThrow(() -> schedulerService.executeActions(1L,
                WorkFlowManagerActions.EXECUTE_JOB));

        Assertions.assertDoesNotThrow(() -> schedulerService.executeActions(1L,
                WorkFlowManagerActions.PAUSE_JOB));

        Assertions.assertDoesNotThrow(() -> schedulerService.executeActions(1L,
                WorkFlowManagerActions.RESUME_JOB));

        Assertions.assertDoesNotThrow(() -> schedulerService.executeActions(1L,
                WorkFlowManagerActions.ABORT_JOB));
    }

    @Test
    public void testAddSrpceAgentTask(){
        //Mocking the response from Workflow Manager.
        Mockito.doReturn(Optional.of(new JobInfo())).when(webClient).createAgentJob(Mockito.any());

        Agents agents = new Agents();
        agents.setId(1L);
        agents.setType(AgentTypes.SR_PCE_AGENT);
        agents.setName("Sr_pce_agent");

        Assertions.assertDoesNotThrow(() ->schedulerService.addAgentTask(agents, AgentActionTypes.RESTART));
    }

    @Test
    public void testGetAllSchedulerOfNetwork(){
        List<JobInfo> jobInfos = new ArrayList<>();
        JobInfo jobInfo = new JobInfo();
        jobInfos.add(jobInfo);

        Mockito.doReturn(jobInfos).when(webClient).getAllSchedulers();

        Assertions.assertDoesNotThrow(() ->schedulerService.getSchedulersOfNetwork(1L));
    }

    @Test
    @DisplayName("Test Get the rsync info associated with all networks.")
    public void testGetRsyncJobs(){

        Mockito.doReturn(new ArrayList<>()).when(webClient).getRsyncJobs();

        Assertions.assertDoesNotThrow(() ->schedulerService.getNetworkRsyncInfo());
    }

    @Test
    @DisplayName("Test Get the job stats.")
    public void testGetJobStats(){

        Mockito.doReturn(Optional.empty()).when(webClient).getSchedulerStats();

        Assertions.assertDoesNotThrow(() ->schedulerService.getSchedulerStatistics());
    }

    @Test
    @DisplayName("Test create the rsync job.")
    public void testCreateRsyncJobs(){

        NetworkResyncInfo networkResyncInfo = new NetworkResyncInfo();
        networkResyncInfo.setNetworkId(1L);

        Mockito.doReturn(Optional.of(networkResyncInfo)).when(webClient).rsyncNetwork(Mockito.any());

        Assertions.assertDoesNotThrow(() ->schedulerService.rsyncNetwork(networkResyncInfo));
    }

    @Test
    public void testGetJobInfo() {
        SchedulerConfigData schedulerConfigData = new SchedulerConfigData();
        schedulerConfigData.setName("Collector-Service-scheduler");
        schedulerConfigData.setActive(true);
        schedulerConfigData.setNetworkId(1L);
        schedulerConfigData.setRunNow(false);
        schedulerConfigData.setCronExpression("0 0 0 ? * * *");


        List<TaskConfigData> dataCollectionInfoList = new ArrayList<>();

        TaskConfigData dataCollectionInfo = new TaskConfigData();
        dataCollectionInfo.setCollectorId(1L);
        dataCollectionInfo.setAggreagate(false);
        dataCollectionInfo.setTaskName(TaskType.TOPO_IGP.name());
        dataCollectionInfoList.add(dataCollectionInfo);

        schedulerConfigData.setTaskConfigDataList(dataCollectionInfoList);

        JobInfo jobInfo = schedulerService.getJobInfoForCollectorTasks(schedulerConfigData);
        //Tasks + archive task inserted. Hence, size is 2
        log.info(jobInfo.getName() + "Tasks " + jobInfo.getTasks().size());
        Assertions.assertEquals(1, jobInfo.getTasks().size());

        schedulerConfigData = schedulerService.getSchedulerConfigData(jobInfo);
        log.info("Scheduler config data " + schedulerConfigData);
        Assertions.assertFalse(schedulerConfigData.getTaskConfigDataList().get(0).isAggreagate());

        TaskConfigData dataCollectionInfo2 = new TaskConfigData();
        dataCollectionInfo2.setCollectorId(1L);
        dataCollectionInfo2.setAggreagate(true);
        dataCollectionInfo2.setTaskName(TaskType.TOPO_BGP.name());

        dataCollectionInfoList = new ArrayList<>();
        dataCollectionInfoList.add(dataCollectionInfo2);
        schedulerConfigData.setTaskConfigDataList(dataCollectionInfoList);

        jobInfo = schedulerService.getJobInfoForCollectorTasks(schedulerConfigData);
        log.info(jobInfo.getName() + "Tasks " + jobInfo.getTasks().size());
        Assertions.assertEquals(1, jobInfo.getTasks().size());

        schedulerConfigData = schedulerService.getSchedulerConfigData(jobInfo);
        log.info("Scheduler config data " + schedulerConfigData);
        Assertions.assertTrue(schedulerConfigData.getTaskConfigDataList().get(0).isAggreagate());

        TaskConfigData dataCollectionInfo3 = new TaskConfigData();
        dataCollectionInfo3.setArchive(true);
        dataCollectionInfo3.setCollectorType(CollectorTypes.DARE);

        dataCollectionInfoList = new ArrayList<>();
        dataCollectionInfoList.add(dataCollectionInfo3);
        dataCollectionInfoList.add(dataCollectionInfo2);
        schedulerConfigData.setTaskConfigDataList(dataCollectionInfoList);

        jobInfo = schedulerService.getJobInfoForCollectorTasks(schedulerConfigData);
        log.info(jobInfo.getName() + "Tasks " + jobInfo.getTasks().size());
        //Verify that 2 tasks are added with one dare archive task.
        Assertions.assertEquals(2, jobInfo.getTasks().size());

        schedulerConfigData = schedulerService.getSchedulerConfigData(jobInfo);
        log.info("Scheduler config data " + schedulerConfigData);

    }

    @Test
    public void testStatusCheckDuringUpdate(){
        JobInfo jobInfo = new JobInfo();
        jobInfo.setName("Collector-Service-scheduler");
        jobInfo.setScheduleStatus(ScheduleStatus.Active);
        jobInfo.setNetworkId(1L);

        JobStatus runningStatus = new JobStatus();
        JobExecutionStatus executionStatus = new JobExecutionStatus();
        executionStatus.setStatus(ExecutionStatus.RUNNING);
        runningStatus.setLastExecution(executionStatus);

        //Mocking the response from Workflow Manager.
        Mockito.doReturn(Optional.of(jobInfo)).when(webClient).updateScheduler(Mockito.any());
        Mockito.doReturn(Optional.of(runningStatus)).when(webClient).getSchedulerStatus(1L);

        SchedulerConfigData schedulerConfigData = new SchedulerConfigData();
        schedulerConfigData.setName("Collector-Service-scheduler");
        schedulerConfigData.setId(1L);
        schedulerConfigData.setNetworkId(1L);

        TaskConfigData taskConfigData = new TaskConfigData();
        taskConfigData.setCollectorId(1L);

        schedulerConfigData.setTaskConfigDataList(List.of(taskConfigData));

        List<ApiResponseDetail> apiResponseDetails = schedulerService
                .addUpdateScheduler(List.of(schedulerConfigData), true);
        Assertions.assertEquals(1, apiResponseDetails.size());
        Assertions.assertFalse(apiResponseDetails.get(0).isStatus());
    }

    @Test
    public void testMulticastAggrSource(){

        Long source = schedulerService.getMulticastAggregateSource(
                Map.of(CollectorTypes.LOGIN_FIND_MULTICAST, 1L, CollectorTypes.LOGIN_POLL_MULTICAST, 2L));
        Assertions.assertEquals(2, source);

        source = schedulerService.getMulticastAggregateSource(
                Map.of(CollectorTypes.SNMP_POLL_MULTICAST, 1L, CollectorTypes.SNMP_FIND_MULTICAST, 2L));
        Assertions.assertEquals(1, source);

        source = schedulerService.getMulticastAggregateSource(
                Map.of(CollectorTypes.SNMP_POLL_MULTICAST, 2L, CollectorTypes.SNMP_FIND_MULTICAST, 1L,
                        CollectorTypes.LOGIN_FIND_MULTICAST, 3L, CollectorTypes.LOGIN_POLL_MULTICAST, 4L));
        Assertions.assertEquals(4, source);

    }

    @Test
    public void testDemandAggrSource(){

        Collector collector = new Collector();
        collector.setType(CollectorTypes.TRAFFIC_DEMAND);
        collector.setParams("{\"demandSteps\":[{\"id\":83,\"name\":\"mesh\",\"stepNumber\":1,\"tool\":{\"dmd-mesh-creator\":{\"bothDirections\":true,\"deleteSameName\":true,\"serviceClass\":null,\"topology\":null,\"advanced\":{\"sourceList\":[],\"sourceNodes\":null,\"sourceSites\":null,\"sourceAs\":null,\"sourceEndpoints\":null,\"destNodes\":null,\"destSites\":null,\"destAs\":null,\"destEndpoints\":null,\"demandmesh-table\":null,\"demandmeshTable\":null,\"outDemandmeshTable\":null,\"outDemandsTable\":null,\"externalAsInterface-endpoints\":true,\"externalAsInterfaceEndpoints\":true,\"respectAsRelationships\":true,\"externalMesh\":\"RESPECT\",\"setName\":null,\"setTagList\":null,\"optionsFile\":null,\"noGlobalOptions\":false,\"destEqualSource\":false,\"destList\":[],\"destination\":\"\",\"includeDemandToSelf\":true,\"suppressProgress\":false,\"debug\":{\"verbosity\":30}}}},\"enabled\":true},{\"id\":84,\"name\":\"dmdlsps\",\"stepNumber\":2,\"tool\":{\"dmds-for-lsps\":{\"private\":false,\"setTraffic\":\"BW\",\"serviceClass\":null,\"advanced\":{\"lspsTable\":null,\"optionsFile\":null,\"noGlobalOptions\":false,\"suppressProgress\":false,\"debug\":{\"verbosity\":30}}}},\"enabled\":true},{\"id\":85,\"name\":\"dmdp2mplsps\",\"stepNumber\":3,\"tool\":{\"dmds-for-p2mplsps\":{\"setTraffic\":\"ZERO\",\"serviceClass\":null,\"advanced\":{\"p2mplspsTable\":null,\"optionsFile\":null,\"noGlobalOptions\":false,\"suppressProgress\":false,\"debug\":{\"verbosity\":30}}}},\"enabled\":true},{\"id\":86,\"name\":\"dmddeduct\",\"stepNumber\":4,\"tool\":{\"demand-deduction\":{\"fixDemandsWithTraffic\":false,\"demandUpperBound\":0.0,\"removeZeroBwDemands\":true,\"zeroBwTolerance\":0.0,\"zeroFlowTolerance\":0.0,\"measurements\":{\"nodes\":true,\"nodesPriority\":2,\"interfaces\":true,\"interfacesPriority\":1,\"lsps\":true,\"lspsPriority\":2,\"flows\":true,\"flowsPriority\":2},\"advanced\":{\"demandsTable\":\"\",\"fixDemandsTable\":\"\",\"fixMulticastDemands\":false,\"reportFile\":\"\",\"trafficLevel\":\"\",\"scaleMeasurements\":[],\"measErrors\":\"SPREAD\",\"maxPercentLinkUtil\":\"\",\"onlyTunnelAs\":\"\",\"optionsFile\":null,\"noGlobalOptions\":false,\"suppressProgress\":false,\"computationTime\":10,\"warnDynamicLsps\":false,\"warnUnroutedLsps\":false,\"debug\":{\"verbosity\":30}}}},\"enabled\":true},{\"id\":87,\"name\":\"copydmd\",\"stepNumber\":5,\"tool\":{\"copy-demands\":{\"sourceCollector\":null}},\"enabled\":true}]}");
        Long source = schedulerService.getTrafficDemandAggregateSource(collector);
        Assertions.assertEquals(87, source);

        collector.setParams("{\"demandSteps\":[{\"id\":83,\"name\":\"mesh\",\"stepNumber\":1,\"tool\":{\"dmd-mesh-creator\":{\"bothDirections\":true,\"deleteSameName\":true,\"serviceClass\":null,\"topology\":null,\"advanced\":{\"sourceList\":[],\"sourceNodes\":null,\"sourceSites\":null,\"sourceAs\":null,\"sourceEndpoints\":null,\"destNodes\":null,\"destSites\":null,\"destAs\":null,\"destEndpoints\":null,\"demandmesh-table\":null,\"demandmeshTable\":null,\"outDemandmeshTable\":null,\"outDemandsTable\":null,\"externalAsInterface-endpoints\":true,\"externalAsInterfaceEndpoints\":true,\"respectAsRelationships\":true,\"externalMesh\":\"RESPECT\",\"setName\":null,\"setTagList\":null,\"optionsFile\":null,\"noGlobalOptions\":false,\"destEqualSource\":false,\"destList\":[],\"destination\":\"\",\"includeDemandToSelf\":true,\"suppressProgress\":false,\"debug\":{\"verbosity\":30}}}},\"enabled\":true},{\"id\":84,\"name\":\"dmdlsps\",\"stepNumber\":2,\"tool\":{\"dmds-for-lsps\":{\"private\":false,\"setTraffic\":\"BW\",\"serviceClass\":null,\"advanced\":{\"lspsTable\":null,\"optionsFile\":null,\"noGlobalOptions\":false,\"suppressProgress\":false,\"debug\":{\"verbosity\":30}}}},\"enabled\":true},{\"id\":85,\"name\":\"dmdp2mplsps\",\"stepNumber\":3,\"tool\":{\"dmds-for-p2mplsps\":{\"setTraffic\":\"ZERO\",\"serviceClass\":null,\"advanced\":{\"p2mplspsTable\":null,\"optionsFile\":null,\"noGlobalOptions\":false,\"suppressProgress\":false,\"debug\":{\"verbosity\":30}}}},\"enabled\":true},{\"id\":86,\"name\":\"dmddeduct\",\"stepNumber\":4,\"tool\":{\"demand-deduction\":{\"fixDemandsWithTraffic\":false,\"demandUpperBound\":0.0,\"removeZeroBwDemands\":true,\"zeroBwTolerance\":0.0,\"zeroFlowTolerance\":0.0,\"measurements\":{\"nodes\":true,\"nodesPriority\":2,\"interfaces\":true,\"interfacesPriority\":1,\"lsps\":true,\"lspsPriority\":2,\"flows\":true,\"flowsPriority\":2},\"advanced\":{\"demandsTable\":\"\",\"fixDemandsTable\":\"\",\"fixMulticastDemands\":false,\"reportFile\":\"\",\"trafficLevel\":\"\",\"scaleMeasurements\":[],\"measErrors\":\"SPREAD\",\"maxPercentLinkUtil\":\"\",\"onlyTunnelAs\":\"\",\"optionsFile\":null,\"noGlobalOptions\":false,\"suppressProgress\":false,\"computationTime\":10,\"warnDynamicLsps\":false,\"warnUnroutedLsps\":false,\"debug\":{\"verbosity\":30}}}},\"enabled\":true}]}");
        source = schedulerService.getTrafficDemandAggregateSource(collector);
        Assertions.assertEquals(86, source);
    }

    /*
    Below Test cases are invoking the Workflow Manager Endpoint. Replaced with the mockito response.
    @Test
    @DisplayName("Add Update GET Delete a scheduler")
    void testAddScheduler(){
        JobInfo jobInfo = new JobInfo();
        jobInfo.setName("Collector-Service-scheduler");
        jobInfo.setScheduleStatus(ScheduleStatus.Active);

        ScheduleInfo scheduleInfo = new ScheduleInfo();
        scheduleInfo.setRecurrenceType(RecurrenceType.RUN_NOW);

        jobInfo.setScheduleInfo(scheduleInfo);
        List<DataCollectionInfo> dataCollectionInfoList = new ArrayList<>();

        DataCollectionInfo dataCollectionInfo = new DataCollectionInfo();
        dataCollectionInfo.setId(135L);
        dataCollectionInfo.setConsolidationType(ConsolidationType.DARE);
        dataCollectionInfo.setType(DataCollectorType.TOPO_IGP);
        dataCollectionInfoList.add(dataCollectionInfo);

        jobInfo.setTasks(dataCollectionInfoList);

        Assertions.assertThrows(CustomException.class, () -> schedulerService.addScheduler(jobInfo));
        jobInfo.setNetworkId(1L);

        Optional<JobInfo> jobInfoOptional =  schedulerService.addScheduler(jobInfo);
        Assertions.assertTrue(jobInfoOptional.isPresent());
        Assertions.assertEquals(jobInfo.getName(), jobInfoOptional.get().getName());
        Assertions.assertEquals(1, jobInfoOptional.get().getTasks().size());

        DataCollectionInfo dataCollectionInfo2 = new DataCollectionInfo();
        dataCollectionInfo2.setId(134L);
        dataCollectionInfo.setConsolidationType(ConsolidationType.DARE);
        dataCollectionInfo.setType(DataCollectorType.TOPO_BGP);
        dataCollectionInfoList.add(dataCollectionInfo2);

        jobInfo.setTasks(dataCollectionInfoList);
        jobInfo.setId(jobInfoOptional.get().getId());
        jobInfoOptional =  schedulerService.updateScheduler(jobInfo);
        Assertions.assertTrue(jobInfoOptional.isPresent());
        Assertions.assertEquals(2, jobInfoOptional.get().getTasks().size());

        List<JobInfo> jobInfoList = schedulerService.getAllSchedulers(1L);
        Assertions.assertTrue(jobInfoList.size() > 0);

        dataCollectionInfoList = new ArrayList<>();
        dataCollectionInfoList.add(dataCollectionInfo);
        jobInfo.setTasks(dataCollectionInfoList);
        jobInfoOptional =  schedulerService.updateScheduler(jobInfo);
        Assertions.assertTrue(jobInfoOptional.isPresent());
        Assertions.assertEquals(1, jobInfoOptional.get().getTasks().size());

        schedulerService.deleteScheduler(jobInfoOptional.get().getId());

    }

    @Test
    @DisplayName("Test get Network Status ")
    public void testGetNetworkStatus(){
        AllNetworkData allNetworkData = new AllNetworkData();
        allNetworkData.setId(0L);
        allNetworkData.setName("StatusNetwork");

        Mockito.doReturn(List.of(allNetworkData)).when(networkService).getAllNetworks();

        Assertions.assertDoesNotThrow(() -> schedulerService.getNetworkStatus());

        Assertions.assertDoesNotThrow(() -> schedulerService.getTaskHistory(1L));
    }

    @Test
    @DisplayName("Test execute scheduler actions ")
    public void testGetTaskHistory(){
        Assertions.assertDoesNotThrow(() -> schedulerService.executeActions(1L,
                WorkFlowManagerActions.EXECUTE_JOB));

        Assertions.assertDoesNotThrow(() -> schedulerService.executeActions(1L,
                WorkFlowManagerActions.PAUSE_JOB));

        Assertions.assertDoesNotThrow(() -> schedulerService.executeActions(1L,
                WorkFlowManagerActions.RESUME_JOB));
    }

     */

}
