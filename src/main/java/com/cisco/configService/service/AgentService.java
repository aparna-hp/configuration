package com.cisco.configService.service;

import com.cisco.collectionService.model.srPce.status.CollectionWorkerDto;
import com.cisco.collectionService.model.srPce.status.XtcAgentStatusDto;
import com.cisco.configService.entity.Agents;
import com.cisco.configService.enums.AgentActionTypes;
import com.cisco.configService.enums.AgentTypes;
import com.cisco.configService.exception.CustomException;
import com.cisco.configService.exception.IpAddressConstraintValidator;
import com.cisco.configService.exception.ValidationService;
import com.cisco.configService.model.ConfigParams;
import com.cisco.configService.model.netflow.agent.NetflowAgent;
import com.cisco.configService.model.netflow.status.NetflowClusterStatus;
import com.cisco.configService.model.preConfig.AgentData;
import com.cisco.configService.model.preConfig.AllAgentData;
import com.cisco.configService.model.srPce.SrPceAgent;
import com.cisco.configService.model.srPce.status.WorkerStatus;
import com.cisco.configService.model.trafficPoller.CPStatusResponse;
import com.cisco.configService.model.trafficPoller.ContinuosPollerAgent;
import com.cisco.configService.repository.AgentRepository;
import com.cisco.configService.webClient.NetflowWebClient;
import com.cisco.configService.webClient.SrPceWebClient;
import com.cisco.configService.webClient.TrafficPollerWebClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
public class AgentService {

    @Autowired
    AgentRepository agentRepository;

    @Autowired
    SchedulerService schedulerService;

    @Autowired
    SrPceWebClient srPceWebClient;

    @Autowired
    NetflowWebClient netflowWebClient;

    @Autowired
    TrafficPollerWebClient trafficPollerWebClient;

    @Autowired
    ValidationService<ConfigParams> validationService;

    @Autowired
    CryptoService cryptoService;

    public void addAgent(AgentData agentData, boolean... override) {
       Optional<Agents> agentsOptional = getAgentByName(agentData.getName());
        if (agentsOptional.isPresent()) {
            if(null != override && override.length>0 && override[0]){
                agentData.setId(agentsOptional.get().getId());
                updateAgent(agentData);
                return;
            } else {
                throw new CustomException(HttpStatus.BAD_REQUEST, "Invalid Agent Name",
                        "Agent with name " + agentData.getName() + " already exists !");
            }
        }

        if(agentData.getType().equals(AgentTypes.NETFLOW_AGENT) && getAgentByType(agentData.getType()).size() > 0) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Netflow Agent already exists", "Netflow Agent already exists !");
        }

        validateAgentParams(agentData);
        agentData.setUpdateDate(LocalDate.now());
        agentData.setId(null);

        Agents agents = new Agents();
        BeanUtils.copyProperties(agentData, agents);

        agentRepository.save(agents);
        agentData.setId(agents.getId());

        //Invoke agent restart using workflow manager.
        schedulerService.addAgentTask(agents, AgentActionTypes.RESTART);
    }

    public Optional<AgentData> getAgent(Long id) {
        Optional<Agents> agents = agentRepository.findById(id);
        if (agents.isEmpty()) {
            return Optional.empty();
        }
        AgentData agentData = new AgentData();
        BeanUtils.copyProperties(agents.get(), agentData);
        decryptCredentials(agentData);
        return Optional.of(agentData);
    }

    public List<Agents> getAgentByCollectorId(Long collectorId) {
        log.info("Get agent associated with collector id " + collectorId);
        return agentRepository.findAgentByCollectorId(collectorId);

    }

    public List<AllAgentData> getAgentInfo(AgentTypes agentType) {
        List<AllAgentData> agentDataList = new ArrayList<>();
        Iterable<Agents> agentsIterable;
        if(Optional.ofNullable(agentType).isPresent()){
            agentsIterable = agentRepository.findAgentByType(agentType);
        } else {
            agentsIterable = agentRepository.findAll();
        }
        for (Agents agents : agentsIterable) {
            AllAgentData allAgentData = new AllAgentData();
            BeanUtils.copyProperties(agents, allAgentData);
            agentDataList.add(allAgentData);
        }
        return agentDataList;
    }

    public List<AllAgentData> getAllAgentStatus() {
        List<AllAgentData> agentListStatus = new ArrayList<>();
        Map<Long, AllAgentData> srpceAgentDataMap = new HashMap<>();

        Iterable<Agents> agentsIterable = agentRepository.findAll();
        for (Agents agents : agentsIterable) {
            AllAgentData agentDataInfo = new AllAgentData();
            BeanUtils.copyProperties(agents, agentDataInfo);

            if(agents.getType().equals(AgentTypes.SR_PCE_AGENT)) {
                agentDataInfo.setLspConnectionStatus(WorkerStatus.ConnectionStatusEnum.not_connected);
                agentDataInfo.setTopoConnectionStatus(WorkerStatus.ConnectionStatusEnum.not_connected);
                srpceAgentDataMap.put(agents.getId(), agentDataInfo);
            } else if(agents.getType().equals(AgentTypes.NETFLOW_AGENT)) {
                agentDataInfo.setNetflowStatus(false);
                populateNetflowStatus(agentDataInfo);
                agentListStatus.add(agentDataInfo);
            } else if(agents.getType().equals(AgentTypes.TRAFFIC_POLLER)) {
                agentDataInfo.setTrafficPollerStatus(new CPStatusResponse(false));
                populateTrafficPollerStatus(agents, agentDataInfo);
                agentListStatus.add(agentDataInfo);
            }
        }

        populateSrpceStatus(srpceAgentDataMap);
        agentListStatus.addAll(srpceAgentDataMap.values());

        return agentListStatus;
    }

    public List<AgentData> getAllAgents() {
        List<AgentData> agentDataList = new ArrayList<>();
        Iterable<Agents> agentsIterable = agentRepository.findAll();
        for (Agents agents : agentsIterable) {
            AgentData agentData = new AgentData();
            BeanUtils.copyProperties(agents, agentData);
            agentDataList.add(agentData);
        }

        log.info("No. of agents " + agentDataList.size());
        return agentDataList;
    }

    public Optional<Long> updateAgent(AgentData agentData) {
        Optional<Agents> optionalAgents = agentRepository.findById(agentData.getId());
        if (optionalAgents.isEmpty()) {
            return Optional.empty();
        }

        validateAgentParams(agentData);
        agentData.setUpdateDate(LocalDate.now());

        Agents agent = optionalAgents.get();
        if(!agent.getName().equals(agentData.getName())  &&
                getAgentByName(agentData.getName()).isPresent()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Invalid Agent Name",
                    "Agent with name " + agentData.getName() + " already exists !");
        }

        BeanUtils.copyProperties(agentData, agent);
        agentRepository.save(agent);

        //Invoke agent restart using workflow manager.
        schedulerService.addAgentTask(agent, AgentActionTypes.RESTART);

        return Optional.of(agent.getId());
    }

    public Optional<Long> deleteAgent(Long id) {
        Optional<Agents> agent = agentRepository.findById(id);
        if (agent.isEmpty()) {
            return Optional.empty();
        }
        agentRepository.delete(agent.get());
        schedulerService.addAgentTask(agent.get(), AgentActionTypes.STOP);

        return Optional.of(agent.get().getId());
    }

    public void invokeAgent(Long id, AgentActionTypes agentActionType) {
        Optional<Agents> agent = agentRepository.findById(id);
        if (agent.isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "INVALID AGENT ID." + "AGENT WITH THE PROVIDED ID DOES NOT EXIST");
        }
        schedulerService.addAgentTask(agent.get(), agentActionType);
    }

    private void validateAgentParams(AgentData agentData) {
        log.info("Validating the Agent " + agentData.getName());

        //Validate the agent configuration parameters
        final AgentTypes agentType = agentData.getType();
        ConfigParams formBeans;

        try {
            switch (agentType) {
                case SR_PCE_AGENT:
                    if (null == agentData.getParams()) {
                        throw new CustomException(HttpStatus.BAD_REQUEST, "Agent parameters are empty. Agent " + agentData.getName() + " cannot be saved !");
                    }
                    formBeans = new ObjectMapper().
                            readValue(agentData.getParams(), SrPceAgent.class);
                    SrPceAgent srPceAgent = (SrPceAgent) formBeans;
                    encryptCredentials(srPceAgent);
                    /*if(!IpAddressConstraintValidator.isValidInet4Address(srPceAgent.getXtcHostIP())) {
                        throw new CustomException(HttpStatus.BAD_REQUEST, "Invalid SR PCE Host IP. Agent " + agentData.getName() + " cannot be saved !");
                    }*/

                    break;

                case NETFLOW_AGENT:
                    formBeans = agentData.getParams() == null ? new NetflowAgent() : new ObjectMapper().
                            readValue(agentData.getParams(), NetflowAgent.class);
                    break;
                case TRAFFIC_POLLER:
                    formBeans = agentData.getParams() == null ? new NetflowAgent() : new ObjectMapper().
                            readValue(agentData.getParams(), ContinuosPollerAgent.class);
                    break;

                default:
                    formBeans = new ObjectMapper()
                            .readValue(agentData.getParams(), ConfigParams.class);
            }

            ObjectMapper Obj = new ObjectMapper();
            // Converting the Java object into a JSON string
            String agentParams = Obj.writeValueAsString(formBeans);
            log.info("Agent params with default value = " + agentParams);
            agentData.setParams(agentParams);

        } catch (JsonProcessingException jsonMappingException) {
            log.error("Error validating the agent parameters ", jsonMappingException);
            throw new CustomException(HttpStatus.BAD_REQUEST, "Invalid Agent parameters! Cannot save agent " + agentData.getName(),
                    jsonMappingException.getMessage());
        }

        validationService.validateInput(formBeans);

    }

    public void deleteAll() {
        agentRepository.deleteAll();
    }

    public Optional<Agents> getAgentByName(String agentName) {
        return agentRepository.findAgentByName(agentName);
    }

    public List<Agents> getAgentByType(AgentTypes agentType) {
        return agentRepository.findAgentByType(agentType);
    }

    public Optional<AllAgentData> getStatus(Long id) {
        Optional<Agents> agentsOptional = agentRepository.findById(id);
        if (agentsOptional.isEmpty()) {
            return Optional.empty();
        }

        AllAgentData agentDataInfo = new AllAgentData();
        BeanUtils.copyProperties(agentsOptional.get(), agentDataInfo);

        if (agentDataInfo.getType().equals(AgentTypes.SR_PCE_AGENT)) {
            populateSrpceStatus(agentDataInfo);
        } else if (agentDataInfo.getType().equals(AgentTypes.NETFLOW_AGENT)) {
            populateDetailNetflowStatus(agentDataInfo);
        } else if (agentDataInfo.getType().equals(AgentTypes.TRAFFIC_POLLER)) {
            populateTrafficPollerStatus(agentsOptional.get(), agentDataInfo);
        }
        return Optional.of(agentDataInfo);
    }

    public void populateNetflowStatus(AllAgentData netflowAgent) {

        try {
            ResponseEntity<NetflowClusterStatus> responseEntity = netflowWebClient.getNetflowStatus();

            log.debug("Status Code: " + responseEntity.getStatusCode());
            if (null == responseEntity.getBody()) {
                return;
            }
            NetflowClusterStatus status = responseEntity.getBody();
            log.debug("Netflow status result body : " + status);

            if (null != status && status.getClusterSummary() != null) {
                netflowAgent.setNetflowStatus(status.getClusterSummary().getClusterAllOk());
            } else {
                netflowAgent.setNetflowStatus(false);
            }
        } catch (CustomException customException) {
            log.error("Error getting the netflow status. Setting the status as down. ", customException);
        }
    }

    public void populateDetailNetflowStatus(AllAgentData netflowAgent) {

        try {
            ResponseEntity<NetflowClusterStatus> responseEntity = netflowWebClient.getNetflowStatus();

            log.debug("Status Code: " + responseEntity.getStatusCode());
            if (null == responseEntity.getBody()) {
                return;
            }
            NetflowClusterStatus status = responseEntity.getBody();
            log.debug("Netflow status result body : " + status);

            if (null != status) {
                netflowAgent.setNetflowDetailStatus(status);
            }
        } catch (CustomException customException) {
            log.error("Error getting the netflow status ", customException);
        }
    }

    public void populateSrpceStatus(AllAgentData srpceAgent) {
        try {
            ResponseEntity<XtcAgentStatusDto> responseEntity = srPceWebClient.getSrPceAgentStatus(srpceAgent.getId());

            log.debug("Status Code: " + responseEntity.getStatusCode());
            if (null == responseEntity.getBody()) {
                return;
            }
            XtcAgentStatusDto status = responseEntity.getBody();
            log.debug("sr pce status result body : " + status);

            if (null != status) {
                srpceAgent.setSrPceDetailStatus(status);
            }
        } catch (CustomException customException){
            log.error("Error getting the srpce agent status " , customException);
        }
    }

    public void populateSrpceStatus(Map<Long, AllAgentData> allAgentDataMap) {
        try {
            ResponseEntity<XtcAgentStatusDto[]> responseEntity = srPceWebClient.getAllSrPceAgentStatus();

            log.debug("Status Code: " + responseEntity.getStatusCode());
            if (null == responseEntity.getBody()) {
                return;
            }
            List<XtcAgentStatusDto> allStatus = Arrays.asList(responseEntity.getBody());
            log.debug("sr pce status result body : " + allStatus);

            for (XtcAgentStatusDto status : allStatus) {
                if(allAgentDataMap.containsKey(status.getAgentID())) {
                    AllAgentData allAgentData = allAgentDataMap.get(status.getAgentID());
                    for (CollectionWorkerDto collectionWorkerDto : status.getCollectionWorkers()) {
                        if (WorkerStatus.WorkerType.TOPOLOGY.equals(collectionWorkerDto.getWorkerType())) {
                            allAgentData.setTopoConnectionStatus(collectionWorkerDto.getWorkerStatus().getConnectionStatus());
                        } else {
                            allAgentData.setLspConnectionStatus(collectionWorkerDto.getWorkerStatus().getConnectionStatus());
                        }
                    }
                }
            }
        } catch (CustomException e) {
            log.error("Error getting the status for the sr pce agents. Setting the status as not connected. ", e);
        }
    }

    public void populateTrafficPollerStatus(Agents agentEntity, AllAgentData trafficPollerData) {
        try {
            String params = agentEntity.getParams();
            ContinuosPollerAgent continuosPollerAgent;

            continuosPollerAgent = new ObjectMapper().readValue(params, ContinuosPollerAgent.class);

            ResponseEntity<CPStatusResponse> responseEntity = trafficPollerWebClient.getTrafficPollerStatus(continuosPollerAgent.getNetworkId());

            log.debug("Status Code: " + responseEntity.getStatusCode());
            if (null == responseEntity.getBody()) {
                return;
            }
            CPStatusResponse status = responseEntity.getBody();
            log.debug("Traffic poller status result body : " + status);

            if (null != status) {
                trafficPollerData.setTrafficPollerStatus(status);
            }
        } catch (Exception e) {
            log.error("Error getting the status for the traffic poller agent .", e);
        }
    }

    public String getDefaultConfigParams(AgentTypes agentTypes) {
        String parameterDetails = "";

        switch (agentTypes) {
            case SR_PCE_AGENT:
                parameterDetails = getJson(new SrPceAgent());
                break;
            case NETFLOW_AGENT:
                parameterDetails = getJson(new NetflowAgent());
                break;
        }
        return parameterDetails;
    }

    private String getJson(ConfigParams agentParams) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String config = mapper.writeValueAsString(agentParams);
            log.info("Agent Config = " + config);
            return config;
        } catch (Exception e) {
            throw new CustomException("Error forming the default parameters for agent");
        }
    }

    private void encryptCredentials(SrPceAgent srPceAgent) {
        byte[] encrypted;
        if (null != srPceAgent.getPassword()) {
            encrypted = cryptoService.aesEncrypt(srPceAgent.getPassword());
            if (null != encrypted) {
                srPceAgent.setEncodedPassword(encrypted);
                srPceAgent.setPassword(null);
            }
        }
    }

    private void decryptCredentials(AgentData agentData) {
        byte[] decrypted;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            SrPceAgent srPceAgent = objectMapper.readValue(agentData.getParams(), SrPceAgent.class);
            if (null != srPceAgent.getEncodedPassword() && srPceAgent.getEncodedPassword().length > 0) {
                decrypted = cryptoService.aesDecrypt(srPceAgent.getEncodedPassword());
                if (null != decrypted) {
                    srPceAgent.setPassword(new String(decrypted));
                }
                agentData.setParams(objectMapper.writeValueAsString(srPceAgent));
            }
        } catch (JsonProcessingException e){
            log.error("Error reading the SR pce parameters.",e);
        }
    }

}
