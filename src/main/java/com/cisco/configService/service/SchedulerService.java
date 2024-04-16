package com.cisco.configService.service;

import com.cisco.collectionService.utils.StringUtil;
import com.cisco.configService.entity.Agents;
import com.cisco.configService.entity.Collector;
import com.cisco.configService.entity.Network;
import com.cisco.configService.enums.AgentActionTypes;
import com.cisco.configService.enums.AgentTypes;
import com.cisco.configService.enums.CollectorTypes;
import com.cisco.configService.enums.WorkFlowManagerActions;
import com.cisco.configService.exception.CustomException;
import com.cisco.configService.model.composer.ApiResponseDetail;
import com.cisco.configService.model.demand.ui.DemandCollectorView;
import com.cisco.configService.model.demand.ui.DemandStepView;
import com.cisco.configService.model.multicast.ui.MulticastCollectorView;
import com.cisco.configService.model.scheduler.SchedulerConfigData;
import com.cisco.configService.model.scheduler.TaskConfigData;
import com.cisco.configService.model.trafficPoller.ContinuosPollerAgent;
import com.cisco.configService.repository.CollectorRepository;
import com.cisco.configService.repository.NetworkRepository;
import com.cisco.configService.webClient.WorkflowManagerWebClient;
import com.cisco.workflowmanager.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class SchedulerService {

    @Autowired
    WorkflowManagerWebClient webClient;

    @Autowired
    NetworkRepository networkRepository;

    @Autowired
    CollectorRepository collectorRepository;

    public static final String ARCHIVE_TASK = "Archive_task";

    /**
     * @return JobInfo
     * This method invokes the Workflow manager POST API to create a new Job.
     * Returns an Optional empty if job cannot be created.
     */
    public Optional<JobInfo> addScheduler(JobInfo jobInfo, boolean update) {

        Optional<JobInfo> optionalJobInfo ;
        if(update) {
            optionalJobInfo = webClient.updateScheduler(jobInfo);
        } else {
            optionalJobInfo = webClient.createScheduler(jobInfo);
        }

        return optionalJobInfo;
    }

    /**
     * @return Status of scheduler create/update
     * This method invokes the Workflow manager POST/UPDATE API to create/update Jobs.
     */
    public List<ApiResponseDetail> addUpdateScheduler(List<SchedulerConfigData> schedulerConfigDataList,
                                                      boolean update) {

        List<ApiResponseDetail> apiResponseList = new ArrayList<>();
        for (SchedulerConfigData schedulerConfigData : schedulerConfigDataList) {
            ApiResponseDetail apiResponse = new ApiResponseDetail();
            apiResponse.setName(schedulerConfigData.getName());

            try {
                log.info("Add/Update the scheduler {} to network {} ", schedulerConfigData, schedulerConfigData.getNetworkId());
                if(schedulerConfigData.getId() == null) {
                    update = false;
                }

                if(update) {
                    Optional<JobStatus> jobStatusResponse = webClient.getSchedulerStatus(schedulerConfigData.getId());

                    if(jobStatusResponse.isPresent() &&
                            jobStatusResponse.get().getLastExecution()!= null &&
                            jobStatusResponse.get().getLastExecution().getStatus() == ExecutionStatus.RUNNING) {
                        throw new CustomException(HttpStatus.FORBIDDEN,
                                "The scheduler update is not supported when the execution is in progress.");
                    }
                }

                JobInfo jobInfo = getJobInfoForCollectorTasks(schedulerConfigData);

                Optional<JobInfo> optionalJobInfo = addScheduler(jobInfo, update);
                if (optionalJobInfo.isPresent()) {
                    log.info("Successfully added/updated the scheduler with Id: " + optionalJobInfo.get().getId());
                    apiResponse.setId(optionalJobInfo.get().getId());
                    apiResponse.setStatus(true);
                } else {
                    log.error("Json info from scheduler is empty");
                    apiResponse.setStatus(false);
                    apiResponse.setErrorDetails("Error saving the scheduler.");
                }
            } catch (Exception e) {
                log.error("Error adding the scheduler " + schedulerConfigData.getName(),e);
                apiResponse.setStatus(false);
                apiResponse.setErrorDetails(e.getMessage());
            }
            apiResponseList.add(apiResponse);
        }
        return apiResponseList;
    }

    /**
     * @return List of failed scheduler configurations.
     * This method invokes the Workflow manager POST API to create Jobs.
     */
    public List<SchedulerConfigData> importScheduler(List<SchedulerConfigData> schedulerConfigDataList) {

        List<SchedulerConfigData> failedSchedulerList = new ArrayList<>();
        for (SchedulerConfigData schedulerConfigData : schedulerConfigDataList) {
               //Set the scheduler status as inactive during migration.
                schedulerConfigData.setActive(false);

            try {
                log.info("Import the scheduler {} to network {} ", schedulerConfigData, schedulerConfigData.getNetworkId());

                JobInfo jobInfo = getJobInfoForCollectorTasks(schedulerConfigData);

                Optional<JobInfo> optionalJobInfo = addScheduler(jobInfo, false);
                if (optionalJobInfo.isPresent()) {
                    log.info("Successfully added/updated the scheduler with Id: " + optionalJobInfo.get().getId());
                } else {
                    log.error("Json info from scheduler is empty");
                    failedSchedulerList.add(schedulerConfigData);
                }
            } catch (Exception e) {
                log.error("Error adding the scheduler " + schedulerConfigData.getName(),e);
                failedSchedulerList.add(schedulerConfigData);
            }
        }
        return failedSchedulerList;
    }

    /**
     * @return JobInfo
     * This method returns all the configured schdulers.
     * It invokes the Workflow manager GET API to get all the schedulers.
     */
    public List<JobInfo> getAllSchedulers() {

        List<JobInfo> jobInfos = webClient.getAllSchedulers();

        log.info("JobInfo result size : " + jobInfos.size());
        return jobInfos;
    }

    /**
     * @param networkId Network Id
     * @return JobInfo
     * This method is invokes the Workflow manager GET API to get the status of the given network.
     * Returns an empty if job cannot be found.
     */
    public NetworkStatus getNetworkStatus(Long networkId) {

        String networkName = verifyNetworkIdAndGetName(networkId);

        Optional<NetworkStatus> optionalNetworkStatus = webClient.getNetworkStatus(networkId);
        if(optionalNetworkStatus.isPresent()){
            NetworkStatus networkStatus = optionalNetworkStatus.get();
            log.debug("Setting the network name {} in the network status." , networkName);
            networkStatus.setName(networkName);
            return networkStatus;
        }
        return new NetworkStatus();
    }

    /**
     * @param networkId Network Id
     * @return JobInfo
     * This method is invokes the Workflow manager GET API to get the schedulers of the given network.
     * Returns an Optional empty if job cannot be found.
     */
    public List<SchedulerConfigData> getSchedulersOfNetwork(Long networkId) {

        verifyNetworkIdAndGetName(networkId);
        List<SchedulerConfigData> schedulerConfigDataList = new ArrayList<>();

        List<JobInfo> jobInfos = webClient.getAllSchedulers();

        for(JobInfo jobInfo : jobInfos){
            if(networkId == jobInfo.getNetworkId()) {
                log.debug("Scheduler obtained {} ", jobInfo.getName());
                schedulerConfigDataList.add(getSchedulerConfigData(jobInfo));
            }
        }
        log.info("No. of Schedulers belonging to the network " + schedulerConfigDataList.size());
        schedulerConfigDataList.forEach(schedulerConfigData -> log.info(schedulerConfigData.getName()));
        return schedulerConfigDataList;
    }

    /**
     * @param schedulerId scheduler Id
     * @return Scheduler Config Data
     */
    public Optional<SchedulerConfigData> getScheduler(Long schedulerId) {

       Optional<JobInfo> optionalJobInfo = webClient.getScheduler(schedulerId);

        return optionalJobInfo.map(this::getSchedulerConfigData);

    }

    /**
     * @param schedulerId Scheduler Id
     * @return NetworkStatus
     * This method is invokes the Workflow manager GET API to get the Job status details.
     *      for all the network.
     */
    public Optional<JobTaskHistory> getTaskHistory(Long schedulerId) {

            return webClient.getTaskHistory(schedulerId);
    }

    /**
     * @param id Scheduler Id
     * @return
     * This method is invokes the Workflow manager API to delete the Job.
     */
    public boolean deleteScheduler(Long id) {
        return webClient.deleteScheduler(id);
    }

    /*
    This method executes the different actions on the schduler.
     */
    public boolean executeActions(Long id, WorkFlowManagerActions action) {
        log.info("Executing the action {} for the schduler {}", action, id);

        switch (action) {
            case PAUSE_JOB -> {
                return webClient.pauseScheduler(id);
            }
            case RESUME_JOB -> {
                return webClient.resumeScheduler(id);
            }
            case EXECUTE_JOB -> {
                return webClient.executeScheduler(id);
            }
            case ABORT_JOB -> {
                return webClient.abortScheduler(id);
            }
            default -> {
                log.error("The scheduler action is not supported.");
                throw new CustomException(HttpStatus.FORBIDDEN,
                        "The scheduler action requested is not supported.");
            }
        }
    }

    public void addAgentTask(Agents agent, AgentActionTypes agentActionTypes) {
        long id = agent.getId();

        ActionType actionType = ActionType.START;
        if(agentActionTypes.equals(AgentActionTypes.RESTART)) {
            actionType = ActionType.RESTART;
        } else if(agentActionTypes.equals(AgentActionTypes.STOP)){
            actionType = ActionType.STOP;
        }

        AgentTypes agentType = AgentTypes.SR_PCE_AGENT;
        if(agent.getType().equals(AgentTypes.NETFLOW_AGENT)) {
            agentType = AgentTypes.NETFLOW_AGENT;
        } else if(agent.getType().equals(AgentTypes.TRAFFIC_POLLER)) {
           agentType = AgentTypes.TRAFFIC_POLLER;
            String params = agent.getParams();
            ContinuosPollerAgent continuosPollerAgent;
            try {
                continuosPollerAgent = new ObjectMapper().readValue(params, ContinuosPollerAgent.class);
            } catch (JsonProcessingException e) {
                throw new CustomException("Error starting the traffic poller.");
            }
            id = continuosPollerAgent.getNetworkId();
        }

        AgentAction agentAction = new AgentAction(id, agentType, actionType);
        log.info("Agent restart JobInfo details " + agentAction);


        Optional<JobInfo> job = webClient.createAgentJob(agentAction);
        job.ifPresent(info -> log.debug("Job added successfully  : " + info));

    }

    public Optional<NetworkResyncInfo> rsyncNetwork(NetworkResyncInfo networkResyncInfo) {
        return webClient.rsyncNetwork(networkResyncInfo);
    }

    /*
    Get the resync information associated with all the networks.
     */
    public List<JobInfo> getNetworkRsyncInfo() {
        Iterable<Network> networkList = networkRepository.findAll();
        List<JobInfo> jobInfoList = webClient.getRsyncJobs();
        List<Long> rsyncNetworkList = new ArrayList<>();

        for(JobInfo jobInfo : jobInfoList){
            rsyncNetworkList.add(jobInfo.getNetworkId());
        }

        for(Network network : networkList){
            if(!rsyncNetworkList.contains(network.getId())){
                JobInfo jobInfo = new JobInfo();
                jobInfo.setNetworkId(network.getId());
                jobInfo.setNetworkName(network.getName());
                jobInfo.setScheduleStatus(ScheduleStatus.Disabled);
                jobInfoList.add(jobInfo);
            }
        }
        return jobInfoList;
    }

    public Optional<JobStats> getSchedulerStatistics(){
        return webClient.getSchedulerStats();
    }

    /*
    This method validates if network id is valid.
    If not valid, exception is thrown. If valid, network name associated with the Id is returned.
     */
    private String verifyNetworkIdAndGetName(Long networkId){
        log.info("Verify if the network id {} is valid", networkId);
        if(null == networkId) {
            log.error("No network ID provided with scheduler ");
            throw new CustomException(HttpStatus.BAD_REQUEST, "Please specify the network associated with the scheduler.");
        }
        Optional<Network> networkOptional = networkRepository.findById(networkId);
        if(networkOptional.isEmpty()){
            log.error("There is no network associated with id " + networkId);
            throw new CustomException(HttpStatus.BAD_REQUEST, "Please specify a valid network associated with the scheduler.");
        }

        return networkOptional.get().getName();
    }

    /*
   This method returns the network ID associated with the name.
    */
    private Long getNetworkIdFromName(String networkName){
        log.info("Get the network Id associated with name{} ", networkName);
        if(StringUtil.isEmpty(networkName)) {
            log.error("No network name provided with scheduler ");
            throw new CustomException(HttpStatus.BAD_REQUEST, "Please specify the network associated with the scheduler.");
        }
        Optional<Network> networkOptional = networkRepository.findByName(networkName);
        if(networkOptional.isEmpty()){
            log.error("There is no network associated with name " + networkName);
            throw new CustomException(HttpStatus.BAD_REQUEST, "Please specify a valid network associated with the scheduler.");
        }

        return networkOptional.get().getId();
    }


    public JobInfo getJobInfoForCollectorTasks(SchedulerConfigData schedulerConfigData) {
        log.info("Add the scheduler {} to network {} ", schedulerConfigData, schedulerConfigData.getNetworkId());
        String networkName = schedulerConfigData.getNetworkName();
        Long networkId = schedulerConfigData.getNetworkId();
        if(null != networkId) {
            networkName = verifyNetworkIdAndGetName(schedulerConfigData.getNetworkId());
        } else {
            networkId = getNetworkIdFromName(networkName);
        }

        JobInfo jobInfo = new JobInfo();
        jobInfo.setName(schedulerConfigData.getName());
        jobInfo.setNetworkId(networkId);
        jobInfo.setNetworkName(networkName);

        if(null != schedulerConfigData.getId()) {
            jobInfo.setId(schedulerConfigData.getId());
        }

        ScheduleInfo scheduleInfo = new ScheduleInfo();
        if (schedulerConfigData.isRunNow() || null == schedulerConfigData.getCronExpression()
                || schedulerConfigData.getCronExpression().isEmpty()) {
            scheduleInfo.setRecurrenceType(RecurrenceType.RUN_NOW);

        } else {
            scheduleInfo.setCronExpr(schedulerConfigData.getCronExpression());
            scheduleInfo.setRecurrenceType(RecurrenceType.CRON_EXPR);
        }

        jobInfo.setScheduleInfo(scheduleInfo);

        List<DataCollectionInfo> dataCollectionInfoList = new ArrayList<>();
        for(TaskConfigData taskConfigData : schedulerConfigData.getTaskConfigDataList()) {
            Long collectorId = taskConfigData.getCollectorId();
            String collectorName = taskConfigData.getCollectorName();
            CollectorTypes collectorType = taskConfigData.getCollectorType();
            Collector collectorEntity = null;
            if(null != collectorId) {
                Optional<Collector> collectorsOptional = collectorRepository.findById(taskConfigData.getCollectorId());
                if (collectorsOptional.isEmpty()) {
                    log.error("The collector with id {} does not exists. Skip adding the task for scheduler",
                            taskConfigData.getCollectorId());
                    continue;
                }
                collectorEntity = collectorsOptional.get();
            } else if(null != collectorName) {
                List<Collector> collectorsByName = collectorRepository.findByName(collectorName);
                if (collectorsByName.isEmpty()) {
                    log.error("The collector with name {} does not exists. Skip adding the task for scheduler",
                            taskConfigData.getCollectorName());
                    continue;
                }
                collectorEntity = collectorsByName.get(0);
            } else if(null != collectorType && (collectorType.equals(CollectorTypes.DARE)
                    || collectorType.equals(CollectorTypes.SAGE)) && taskConfigData.isArchive() ){
                dataCollectionInfoList.add(insertArchiveTask(collectorType));
                continue;
            } else {
                log.error("The task {} is not associated with collector id or name. Skip adding the task for scheduler",
                        taskConfigData.getTaskName());
            }
            log.info("Adding task with Id " + taskConfigData.getCollectorId() );

            Long sourceCollector = null;

            if(null != collectorEntity && null != collectorEntity.getChildCollectorIds()
                    && collectorEntity.getChildCollectorIds().size() > 0) {
                Map<CollectorTypes, Long> childCollectorTypeToId = new HashMap<>();

                for(Long childCollectorId : collectorEntity.getChildCollectorIds()) {
                    Optional<Collector> childCollectorOptional = collectorRepository.findById(childCollectorId);
                    if(childCollectorOptional.isPresent()) {
                        Collector childCollector = childCollectorOptional.get();
                        if(null == childCollector.getSourceCollector()) {
                            childCollector.setSourceCollector(collectorEntity.getSourceCollector());
                        }
                        sourceCollector = Long.parseLong(childCollector.getSourceCollector());

                        DataCollectionInfo dataCollectionInfo = getDataCollectionInfo(childCollector, sourceCollector,
                                taskConfigData.getTaskName() + "_" + childCollector.getType(), true, false,false);
                        dataCollectionInfoList.add(dataCollectionInfo);
                        childCollectorTypeToId.put(childCollector.getType(), childCollectorId);
                    }
                }

                if(taskConfigData.isAggreagate()) {
                    if(collectorEntity.getType().equals(CollectorTypes.MULTICAST)){
                        sourceCollector = getMulticastAggregateSource(childCollectorTypeToId);
                    } else if(collectorEntity.getType().equals(CollectorTypes.TRAFFIC_DEMAND)){
                        sourceCollector = getTrafficDemandAggregateSource(collectorEntity);
                    }
                    DataCollectionInfo dataCollectionInfo = getDataCollectionInfo(collectorEntity, sourceCollector,
                            taskConfigData.getTaskName() + "_AGGREGATE" , false, true, taskConfigData.isArchive());
                    dataCollectionInfoList.add(dataCollectionInfo);
                }
            } else if(null != collectorEntity){
                if(null != collectorEntity.getSourceCollector()){
                    sourceCollector = Long.parseLong(collectorEntity.getSourceCollector());
                }
                DataCollectionInfo dataCollectionInfo = getDataCollectionInfo(collectorEntity, sourceCollector,
                        taskConfigData.getTaskName(),
                        taskConfigData.isCollect(), taskConfigData.isAggreagate(), taskConfigData.isArchive());
                dataCollectionInfoList.add(dataCollectionInfo);
            }
        }

        if(dataCollectionInfoList.size() == 0) {
            throw new CustomException("No valid tasks exist for the scheduler. Could not add scheduler."
                    + schedulerConfigData.getName());
        }

        jobInfo.setTasks(dataCollectionInfoList);
        log.debug("Job Info generated : " + jobInfo);
        return jobInfo;
    }

    //Get the scheduler config data from jobInfo
    public SchedulerConfigData getSchedulerConfigData(JobInfo jobInfo){
        SchedulerConfigData schedulerConfigData = new SchedulerConfigData();
        schedulerConfigData.setId(jobInfo.getId());
        schedulerConfigData.setName(jobInfo.getName());
        schedulerConfigData.setNetworkId(jobInfo.getNetworkId());
        schedulerConfigData.setNetworkName(jobInfo.getNetworkName());
        schedulerConfigData.setActive(jobInfo.getScheduleStatus().equals(ScheduleStatus.Active));
        if(null != jobInfo.getScheduleInfo() && null != jobInfo.getScheduleInfo().getRecurrenceType()) {
            schedulerConfigData.setRunNow(jobInfo.getScheduleInfo().getRecurrenceType().equals(RecurrenceType.RUN_NOW));
            schedulerConfigData.setCronExpression(jobInfo.getScheduleInfo().getCronExpr());
        }

        List<TaskConfigData> taskConfigDataList = new ArrayList<>();
        for(DataCollectionInfo dataCollectionInfo : jobInfo.getTasks()) {
            TaskConfigData taskConfigData = new TaskConfigData();
            taskConfigData.setTaskName(dataCollectionInfo.getName());

            if (dataCollectionInfo.getType().equals(TaskType.DARE)){
                taskConfigData.setCollectorType(CollectorTypes.DARE);
            } else if(dataCollectionInfo.getType().equals(TaskType.SAGE)){
                taskConfigData.setCollectorType(CollectorTypes.SAGE);
            } else {
                taskConfigData.setCollectorId(dataCollectionInfo.getId());
                Optional<Collector> collectorOptional = collectorRepository.findById(dataCollectionInfo.getId());
                collectorOptional.ifPresent(value -> {
                    taskConfigData.setCollectorName(value.getName());
                    taskConfigData.setCollectorType(value.getType());
                });
            }

            for (ActionType actionType : dataCollectionInfo.getActions()) {
                switch (actionType) {
                    case AGGREGATE -> taskConfigData.setAggreagate(true);
                    case COLLECT -> taskConfigData.setCollect(true);
                    case ARCHIVE -> taskConfigData.setArchive(true);
                }
            }
            taskConfigDataList.add(taskConfigData);
        }
        schedulerConfigData.setTaskConfigDataList(taskConfigDataList);
        log.debug("Scheduler config data " + schedulerConfigData);
        return schedulerConfigData;
    }

    private DataCollectionInfo getDataCollectionInfo(Collector collectorEntity, Long source,
                                                     String taskName,  boolean collect, boolean aggregate, boolean archive) {
        DataCollectionInfo dataCollectionInfo = new DataCollectionInfo();
        dataCollectionInfo.setId(collectorEntity.getId());
        dataCollectionInfo.setName(taskName);
        dataCollectionInfo.setType(TaskType.findByCollectorType(collectorEntity.getType()));
        dataCollectionInfo.setConsolidationType(collectorEntity.getConsolidationType());
        Optional.ofNullable(collectorEntity.getTimeout()).ifPresent(dataCollectionInfo::setTimeout);
        if (null != source) {
                dataCollectionInfo.setSource(source);
        }

        List<ActionType> actionTypeList = new ArrayList<>();
        if (collect) {
            actionTypeList.add(ActionType.COLLECT);
        }
        if (aggregate) {
            actionTypeList.add(ActionType.AGGREGATE);
        }
        if (archive) {
            actionTypeList.add(ActionType.ARCHIVE);
        }

        dataCollectionInfo.setActions(actionTypeList);

        return dataCollectionInfo;
    }

    public Long getMulticastAggregateSource(Map<CollectorTypes, Long> multicastChildTypeToIdMap) {
        log.debug("Determine the source of the multicast aggregation task.");
        if (multicastChildTypeToIdMap.containsKey(CollectorTypes.LOGIN_POLL_MULTICAST)) {
            log.debug("The aggregator source is " + CollectorTypes.LOGIN_POLL_MULTICAST);
            return multicastChildTypeToIdMap.get(CollectorTypes.LOGIN_POLL_MULTICAST);
        }

        if (multicastChildTypeToIdMap.containsKey(CollectorTypes.LOGIN_FIND_MULTICAST)) {
            log.debug("The aggregator source is " + CollectorTypes.LOGIN_FIND_MULTICAST);
            return multicastChildTypeToIdMap.get(CollectorTypes.LOGIN_FIND_MULTICAST);
        }

        if (multicastChildTypeToIdMap.containsKey(CollectorTypes.SNMP_POLL_MULTICAST)) {
            log.debug("The aggregator source is " + CollectorTypes.SNMP_POLL_MULTICAST);
            return multicastChildTypeToIdMap.get(CollectorTypes.SNMP_POLL_MULTICAST);
        }
        log.debug("The aggregator source is " + CollectorTypes.SNMP_FIND_MULTICAST);
        return multicastChildTypeToIdMap.get(CollectorTypes.SNMP_FIND_MULTICAST);
    }

    public Long getTrafficDemandAggregateSource(Collector collector) {
        log.debug("Determine the source of the traffic demand aggregation task.");
        String params = collector.getParams();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            DemandCollectorView demandCollectorView = objectMapper.readValue(params, DemandCollectorView.class);
            demandCollectorView.getDemandSteps().sort(Comparator.comparing(DemandStepView::getStepNumber, Comparator.reverseOrder()));
            for (DemandStepView demandStepView : demandCollectorView.getDemandSteps()) {
                log.debug("The aggregate source is tool id {} with step number {} and name {} " , demandStepView.getId(),
                        demandStepView.getName(), demandStepView.getStepNumber());
                return demandStepView.getId();
            }
        } catch (JsonProcessingException e) {
            log.error("Error determining the source of the aggregation task. Set it same as the source collector",e);
        }
        return Long.parseLong(collector.getSourceCollector());
    }



    /*
    Insert Archive task.
     */
    private DataCollectionInfo insertArchiveTask(CollectorTypes collectorType) {
        log.info("Inserting the archive task for collector type " + collectorType);
        DataCollectionInfo dataCollectionInfo = new DataCollectionInfo();
        dataCollectionInfo.setName(collectorType + "_" + ARCHIVE_TASK);
        dataCollectionInfo.setActions(List.of(ActionType.ARCHIVE));

        dataCollectionInfo.setType(TaskType.SAGE);
        if(collectorType.equals(CollectorTypes.DARE)){
            dataCollectionInfo.setType(TaskType.DARE);
        }
        return dataCollectionInfo;
    }
}
