package com.cisco.configService.service;

import com.cisco.configService.entity.*;
import com.cisco.configService.enums.AgentTypes;
import com.cisco.configService.enums.CollectorTypes;
import com.cisco.configService.exception.CustomException;
import com.cisco.configService.model.composer.*;
import com.cisco.configService.model.custom.CustomCollector;
import com.cisco.configService.model.demand.ui.DemandCollectorView;
import com.cisco.configService.model.demand.ui.DemandStepView;
import com.cisco.configService.model.multicast.ui.*;
import com.cisco.configService.model.preConfig.AgentData;
import com.cisco.configService.model.preConfig.AllAgentData;
import com.cisco.configService.model.preConfig.AllNodeProfileData;
import com.cisco.configService.model.preConfig.NodeProfileData;
import com.cisco.configService.model.trafficPoller.ContinuosPollerAgent;
import com.cisco.configService.repository.CollectorCustomRepository;
import com.cisco.configService.repository.CollectorRepository;
import com.cisco.configService.repository.NetworkRepository;
import com.cisco.configService.webClient.SrPceWebClient;
import com.cisco.configService.webClient.WorkflowManagerWebClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class NetworkService {

    @Autowired
    NetworkRepository networkRepository;

    @Autowired
    CollectorRepository collectorRepository;

    @Autowired
    CollectorCustomRepository collectorCustomRepository;

    @Autowired
    NodeProfileService nodeProfileService;

    @Autowired
    AgentService agentService;

    @Autowired
    CollectorValidationService collectorValidationService;

    @Autowired
    SchedulerService schedulerService;

    @Autowired
    AggregatorService aggregatorService;

    @Autowired
    WorkflowManagerWebClient workflowManagerWebClient;

    @Autowired
    SrPceWebClient srPceWebClient;


    /*
    Method to save a new network with collectors.
     */
    public NetworkApiResponse saveNetwork(NetworkDataView networkData) {
        log.info("Saving network " + networkData.getName());

        if (networkRepository.findByName(networkData.getName()).isPresent()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Invalid Network Name",
                    "Network with name " + networkData.getName() + " already exists !");
        }

        Network network = new Network();
        network.setName(networkData.getName());
        network.setId(null);

        //Copy network profile reference
        verifyNetworkProfile(networkData.getNodeProfileData().getId());
        network.addNodeProfileRef(networkData.getNodeProfileData().getId());
        NetworkApiResponse networkApiResponse = new NetworkApiResponse();

        log.debug("Network is draft " + networkData.isDraft());
        if(networkData.isDraft()) {
            network.setDraft(true);
            network.setDraftConfig(networkData.getDraftConfig());
        } else {
            saveCollectors(networkData, network, networkApiResponse, false);
        }

        network = networkRepository.save(network);
        log.info("Saved Network Id " + network.getId()
                + " Saved collectors " + Arrays.toString(network.getCollectorIds().toArray()));

        networkApiResponse.setId(network.getId());
        networkData.setId(network.getId());

        log.debug("Update aggregator properties if external executor is present.");
        updateAggPropertiesForAllCustomCollectors(networkData);
        return networkApiResponse;
    }

    /*
    Method to get the network for the given Network Id
     */
    public Optional<NetworkDataView> getNetwork(Long id) {
        log.info("Get network by Id = " + id);
        Optional<Network> network = networkRepository.findById(id);
        if (network.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(convertToNetworkData(network.get()));
    }

    /*
    Method to convert from Network to Network Data format
     */
    public NetworkDataView convertToNetworkData(Network network) {
        NetworkDataView networkDataView = new NetworkDataView();
        BeanUtils.copyProperties(network, networkDataView);

        Optional<NodeProfile> nodeProfileOptional = nodeProfileService.getNodeProfileByNetworkId(network.getId());
        if(nodeProfileOptional.isPresent()){
            AllNodeProfileData allNodeProfileData = new AllNodeProfileData();
            NodeProfile nodeProfile = nodeProfileOptional.get();
            allNodeProfileData.setName(nodeProfile.getName());
            allNodeProfileData.setId(nodeProfile.getId());
            networkDataView.setNodeProfileData(allNodeProfileData);
        }

        for(CollectorRef collectorRef : network.getCollectorRefs()) {
            log.info("Get Collector by Id : " + collectorRef.getCollectorId());
            Long collectorId = collectorRef.getCollectorId();
            if (null == collectorId) {
                continue;
            }
            Optional<Collector> optionalCollector = collectorRepository.findById(collectorId);
            if (optionalCollector.isEmpty()){
                log.debug("Collector by Id is null");
                continue;
            }
            CollectorDataView collectorDataView = new CollectorDataView();
            BeanUtils.copyProperties(optionalCollector.get(), collectorDataView);
            if(optionalCollector.get().getSourceCollector() != null){
                SourceCollector sourceCollectorData = new SourceCollector();
                log.debug("Source collector from entity " + optionalCollector.get());
                if(optionalCollector.get().getSourceCollector().equals(CollectorService.DARE_COLLECTOR_ID.toString())) {
                    log.debug("Setting the source collector as Dare");
                    sourceCollectorData.setType(CollectorTypes.DARE);
                    sourceCollectorData.setName(CollectorTypes.DARE.name());
                    sourceCollectorData.setId(CollectorService.DARE_COLLECTOR_ID);
                    collectorDataView.setSourceCollector(sourceCollectorData);

                }
                else {
                    Optional<Collector> sourceCollector = collectorRepository.findById(Long.parseLong
                            (optionalCollector.get().getSourceCollector()));
                    if (sourceCollector.isPresent()) {
                        sourceCollectorData.setId(sourceCollector.get().getId());
                        sourceCollectorData.setName(sourceCollector.get().getName());
                        sourceCollectorData.setType(sourceCollector.get().getType());
                        collectorDataView.setSourceCollector(sourceCollectorData);
                    }
                }
            }

            for(Agents agents : agentService.getAgentByCollectorId(collectorId)) {
                log.info("Get Agent : " + agents.getId());
                AllAgentData allAgentData = new AllAgentData();
                BeanUtils.copyProperties(agents, allAgentData);
                collectorDataView.getAgents().add(allAgentData);
            }

            networkDataView.getCollectors().add(collectorDataView);
        }
        return networkDataView;
    }

    /*
    This method returns all the network Id and name.
     */
    public List<NetworkDataInfo> getAllNetworkData() {
        List<NetworkDataInfo> allNetworkDataList = new ArrayList<>();
        log.info("Get all the network name and Id ");
        Iterable<Network> networkIterable = networkRepository.findAll();
        for(Network network : networkIterable) {
            log.debug("Network name {} with Id {}" , network.getName(), network.getId() );
            NetworkDataInfo allNetworkData = new NetworkDataInfo();
            BeanUtils.copyProperties(network, allNetworkData);
            Optional<NodeProfileData>  nodeProfileOptional = nodeProfileService.getNodeProfile(
                    network.getNodeProfileRef().getNodeProfileId());
            nodeProfileOptional.ifPresent(nodeProfileData -> {
                allNetworkData.setNodeProfile(nodeProfileData.getName());
                allNetworkData.setNodeProfileId(nodeProfileData.getId());
            });

            allNetworkDataList.add(allNetworkData);
        }
        return allNetworkDataList;
    }

    /*
    This method returns all the network stored in DB.
     */
    public List<NetworkDataView> getAllNetworks() {
        List<NetworkDataView> networkDataList = new ArrayList<>();
        Iterable<Network> networkIterable = networkRepository.findAll();
        for(Network network : networkIterable) {
            log.debug("Network name {} with Id {}" , network.getName(), network.getId() );
            NetworkDataView networkData = convertToNetworkData(network);
            networkDataList.add(networkData);
        }

        log.info("No. of networks " + networkDataList.size());
        return networkDataList;
    }

    /*
    This method is used to update the Network.
     */
    public NetworkApiResponse updateNetwork(NetworkDataView networkData) {
        log.info("Executing update for network with Id " + networkData.getId());

        Optional<Network> networkOptional = networkRepository.findById(networkData.getId());
        if (networkOptional.isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Invalid Network requested for update",
                    "The network provided for update does not exist.");
        }

        Network network = networkOptional.get();
        if(!network.getName().equals(networkData.getName())  &&
                networkRepository.findByName(networkData.getName()).isPresent()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Invalid Network Name",
                    "Network with name " + networkData.getName() + " already exists !");
        }

        //Copy network profile reference
        verifyNetworkProfile(networkData.getNodeProfileData().getId());
        network.addNodeProfileRef(networkData.getNodeProfileData().getId());
        NetworkApiResponse networkApiResponse = new NetworkApiResponse();
        List<Long> prevCollectorIds = new ArrayList<>();

        log.debug(" Network draft " + network.isDraft());
        if(networkData.isDraft()) {
            if(!network.isDraft()) {
                throw new CustomException(HttpStatus.FORBIDDEN, "Network status cannot be changed to Draft.");
            }
            network.setDraft(true);
            network.setDraftConfig(networkData.getDraftConfig());
        } else {
            //Clear the draft config
            network.setDraft(false);
            network.setDraftConfig("");
            prevCollectorIds = network.getCollectorIds();
            network.getCollectorRefs().clear();

            //Copy the collector details
            saveCollectors(networkData, network, networkApiResponse, true);
        }
        network = networkRepository.save(network);
        Long networkId = network.getId();
        networkApiResponse.setId(networkId);

        if(!networkData.isDraft()) {
            for(ApiResponseDetail apiResponseDetail : networkApiResponse.getResponseDetails()){
                log.debug("Mark the collectors to be deleted.");
                if(null != apiResponseDetail.getId()) {
                    log.debug("Retain collector with response :" + apiResponseDetail);
                    prevCollectorIds.remove(apiResponseDetail.getId());
                }
            }

            log.info("Number of deleted collectors : " + prevCollectorIds.size());

            prevCollectorIds.forEach(collectorId -> {
                log.info("Invoke workflow manager to delete all the tasks associated with the collector.");
                workflowManagerWebClient.deleteCollectorTasks(networkId, collectorId);
                deleteCollector(collectorId);
            });

            log.info("Saved Network Id " + network.getId()
                    + " Saved collectors " + Arrays.toString(network.getCollectorIds().toArray()));
        }

        updateAggPropertiesForAllCustomCollectors(networkData);
        return networkApiResponse;
    }

    /*
    This method is used to delete the network for the given Network ID.
     */
    public Optional<Long> deleteNetwork(Long networkId) {
        Optional<Network> optionalNetwork = networkRepository.findById(networkId);
        if (optionalNetwork.isEmpty()) {
            return Optional.empty();
        }

        log.info("Delete all the jobs associated with the network");
        workflowManagerWebClient.deleteSchedulersOfNetwork(networkId);

        Network network = optionalNetwork.get();
        List<Long> collectorIds =  network.getCollectorIds();

        //Delete the network
        networkRepository.delete(network);

        collectorIds.forEach(this::deleteCollector);

        return Optional.of(networkId);
    }

    /**
     * This API gives the status of all the network and its schedulers.
     * It invokes the workflow manger API to get the newtork status.
     */
    public List<NetworkDataInfo> getNetworkStatus() {
        //Get all the network names and its ID
        List<NetworkDataInfo> allNetworkData = getAllNetworkData();

        //Get the status of each network from work flow manager.
        for (NetworkDataInfo networkData : allNetworkData) {
            try {
                log.info("Adding status to the network {} .", networkData.getName());
                if(!networkData.isDraft()) {
                    networkData.setNetworkStatus(schedulerService.getNetworkStatus(networkData.getId()));
                }
            } catch (Exception e) {
                log.error("Error getting the status for the network {} from workflow manager. " +
                        "Proceed with other networks", networkData.getName() + "::" + e.getMessage());
            }
        }
        return allNetworkData;
    }

    /*
    Used to create network in case of configuration import.
     */
    public void importNetwork(NetworkDataView networkData, boolean... override){
        log.info("Saving network " + networkData.getName());
        boolean update = false;
        Optional<Network> networkOptional = networkRepository.findByName(networkData.getName());
        if(networkOptional.isPresent()) {
            if(override == null || override.length == 0) {
                throw new CustomException(HttpStatus.BAD_REQUEST, "Invalid Network Name",
                        "Network with name " + networkData.getName() + " already exists !");
            }else {
                //Use the existing ID associated with name if override is true
                log.info("Using the existing ID {} associated with the name {}", networkOptional.get().getId(),
                        networkData.getName());
                networkData.setId(networkOptional.get().getId());
                updateNetwork(networkData);
                return;
            }
        } else {
            networkData.setId(null);
        }

        Network network = new Network();
        network.setName(networkData.getName());

        //Copy network profile reference
        log.info("Verify if the node profile with name {} exists.", networkData.getNodeProfileData());
        if (null == networkData.getNodeProfileData() || null == networkData.getNodeProfileData().getName()) {
            log.info("The node profile name is not provided.");
            throw new CustomException(HttpStatus.BAD_REQUEST, "Node profile name is mandatory" ,
                    "Cannot create the network since the node profile name is not provided.");
        }

        Optional<NodeProfileData> optionalNodeProfileData =  nodeProfileService
                .getNodeProfileByName(networkData.getNodeProfileData().getName());
        if(optionalNodeProfileData.isEmpty()) {
            log.info("The node profile does not exists.");
            throw new CustomException(HttpStatus.BAD_REQUEST, "Node profile does not exist" ,
                    "Cannot create the network since the node profile provided doesn't exist.");
        }

        network.addNodeProfileRef(optionalNodeProfileData.get().getId());

        if(networkData.isDraft()) {
            network.setDraft(true);
            network.setDraftConfig(networkData.getDraftConfig());
        } else {
            saveCollectors(networkData, network, new NetworkApiResponse(), update);
        }
        network = networkRepository.save(network);
        log.info("Saved Network Id " + network.getId()
                + " Saved collectors " + Arrays.toString(network.getCollectorIds().toArray()));

        networkData.setId(network.getId());
    }

    /*
    This method is used to convert CollectorData to collector entity and persist to DB.
     */
    private void saveCollectorEntity(CollectorDataView collectorDataView, boolean verifyName, List<Long> childCollectorIds, Long sourceCollector){
        if(verifyName) {
            collectorValidationService.validateCollectorParams(collectorDataView, true);
        } else {
            collectorValidationService.validateCollectorParams(collectorDataView);
        }
        //Copy the collector attributes
        Collector collector = new Collector();
        BeanUtils.copyProperties(collectorDataView, collector);
        if(null != sourceCollector) {
            log.debug("Setting the source collector as {} for collector {} with id {}",
                    sourceCollector, collectorDataView.getName(), collectorDataView.getId());
            collector.setSourceCollector(sourceCollector.toString());
        }

        for(AllAgentData allAgentData : collectorDataView.getAgents()){
            log.info("Adding agent" + allAgentData);
            collector.addAgent(allAgentData.getId());
        }

        for(Long childCollectorId : childCollectorIds) {
            collector.addChildCollector(childCollectorId);
        }

        //Save the collector and get the Id
        collector = collectorRepository.save(collector);
        log.info("Saved Collector Id " + collector.getId());
        collectorDataView.setId(collector.getId());
    }

    /*
    This method is used to save all the collectors associated with network to DB.
     */
    private void saveCollectors(NetworkDataView networkDataView, Network network,
                           NetworkApiResponse networkApiResponse,boolean update){

        if(null == networkDataView.getCollectors()) {
            log.info("There are no collectors associated with the network.");
            return;
        }

        List<ApiResponseDetail> apiResponseDetails = new ArrayList<>();
        DemandCollectorView demandCollector = null;
        for (CollectorDataView collectorDataView : networkDataView.getCollectors()) {
            log.info("Adding the collector " + collectorDataView.getName());
            boolean verifyName = true;
            if (!update && null != collectorDataView.getId()) {
                log.error("The Collector ID is not considered while saving the network. " +
                        "Ignoring the Id " + collectorDataView.getId());
                collectorDataView.setId(null);
            }

            if (update) {
                verifyName = computeVerifyName(collectorDataView);
            }

            Optional<Collector> collectorsOptional;

            Set<Long> prevChildIds = new HashSet<>();
            List<Long> childCollectorIds = new ArrayList<>();

            if (collectorDataView.getId() != null) {
                collectorsOptional = collectorRepository.findById(collectorDataView.getId());
                if (collectorsOptional.isPresent()) {
                    prevChildIds = collectorsOptional.get().getChildCollectorIds();
                }
            }
            try {
                try {
                    if (collectorDataView.getType().equals(CollectorTypes.TRAFFIC_DEMAND)) {
                        demandCollector = addDemandToolAsChildCollector(collectorDataView, childCollectorIds, apiResponseDetails);
                    } else if (collectorDataView.getType().equals(CollectorTypes.MULTICAST)) {
                        addMulticastTypesAsChildCollector(collectorDataView, childCollectorIds, apiResponseDetails);
                    }
                }catch (Exception e){
                    log.error("Error adding the child collectors. Retain the previous child collectors. ", e);
                    childCollectorIds.addAll(prevChildIds);
                }

                saveCollectorEntity(collectorDataView, verifyName, childCollectorIds, null);
            } catch (Exception e) {
                log.info("Could not add collector " + collectorDataView.getName(), e);
                ApiResponseDetail apiResponseDetail = new ApiResponseDetail();
                if(update && collectorDataView.getId() !=  null){
                    apiResponseDetail.setId(collectorDataView.getId());
                }
                apiResponseDetail.setName(collectorDataView.getName());
                apiResponseDetail.setErrorDetails(e.getMessage());
                apiResponseDetail.setStatus(false);
                apiResponseDetails.add(apiResponseDetail);
            }

            log.info("Prev Child collector Id size : " + prevChildIds.size());
            log.info("Curr Child collector Id size : " + childCollectorIds.size());

            if (prevChildIds.size() > 0) {
                prevChildIds.forEach(collectorId -> {
                    if (!childCollectorIds.contains(collectorId)) {
                        log.info("Deleting the child collector " + collectorId);
                        collectorCustomRepository.deleteCollector(collectorId);
                    }
                });
            }
        }

        // Save all the collectors with source collector Ids
        for (CollectorDataView collectorDataView : networkDataView.getCollectors()) {
            ApiResponseDetail apiResponseDetail = new ApiResponseDetail();
            apiResponseDetail.setName(collectorDataView.getName());
            apiResponseDetail.setType(collectorDataView.getType());
            boolean validChild = true;
                try {
                    if(null != collectorDataView.getId()) {
                        log.info("Verify if the source Collector has to be added for " + collectorDataView.getName()
                                + " source collector " + collectorDataView.getSourceCollector() );
                        collectorValidationService.checkForSourceCollector(collectorDataView);
                        if(collectorDataView.getSourceCollector() != null && (collectorDataView.getSourceCollector().getId() != null)) {
                            Optional<Collector> collectorsOptional = collectorRepository.findById(collectorDataView.getId());
                            if(collectorsOptional.isPresent()) {
                                Collector collector =collectorsOptional.get();
                                log.debug("Setting the source collector to {} for the collector {} with id {}",
                                        collectorDataView.getSourceCollector(), collectorDataView.getName(), collectorDataView.getId());
                                collector.setSourceCollector(collectorDataView.getSourceCollector().getId().toString());

                                if(collector.getType().equals(CollectorTypes.TRAFFIC_POLL)){
                                    addUpdateTrafficPollerAgent(collector, network.getName());
                                    log.debug("The traffic poller agent added " + Arrays.toString(collector.getAgentIds().toArray()));
                                }
                                else if(collector.getType().equals(CollectorTypes.TRAFFIC_DEMAND) && demandCollector != null) {
                                    try {
                                        updateCopyDemandSourceData(demandCollector, collector);
                                    } catch (CustomException e) {
                                       validChild = false;
                                       apiResponseDetail.setErrorDetails("Copy demand step has invalid source collector.");
                                    }
                                }
                                collectorRepository.save(collector);
                            }
                        }
                        network.addCollectorRef(collectorDataView.getId());
                        apiResponseDetail.setStatus(validChild);
                        apiResponseDetail.setId(collectorDataView.getId());
                        apiResponseDetails.add(apiResponseDetail);
                    }
                } catch (Exception e) {
                    log.info("Could not add collector " + collectorDataView.getName(),e);
                    deleteCollector(collectorDataView.getId());
                    apiResponseDetail.setErrorDetails(e.getMessage());
                    apiResponseDetail.setStatus(false);
                    apiResponseDetails.add(apiResponseDetail);
                }
            }

        networkApiResponse.setResponseDetails(apiResponseDetails);
    }

    private String getDemandToolParams(DemandStepView demandStepView, CollectorTypes toolType) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        switch (toolType) {
            case DEMAND_MESH_CREATOR:
                return mapper.writeValueAsString(demandStepView.getTool().getDmdMeshCreator());
            case DEMAND_FOR_LSPS:
                return mapper.writeValueAsString(demandStepView.getTool().getDmdsForLsps());
            case DEMAND_FOR_P2MP_LSPS:
                return mapper.writeValueAsString(demandStepView.getTool().getDmdsForP2mplsps());
            case DEMAND_DEDUCTION:
                return mapper.writeValueAsString(demandStepView.getTool().getDemandDeduction());
            case COPY_DEMANDS:
                return mapper.writeValueAsString(demandStepView.getTool().getCopyDemands());
            case EXTERNAL_SCRIPT:
                return mapper.writeValueAsString(demandStepView.getTool().getExternalExecutable());
        }
        return null;
    }

    /*
    This method verifies if the network profile with the ID exists.
     */
    private void verifyNetworkProfile(Long networkProfileId) {
        //Copy network profile reference
        log.info("Verify if the node profile with id {} exists.", networkProfileId);
        if (null == networkProfileId) {
            log.info("The node profile Id is not provided.");
            throw new CustomException(HttpStatus.BAD_REQUEST, "Node profile is mandatory",
                    "Cannot create the network since the node profile Id is not provided.");
        }

        Optional<NodeProfileData> optionalNodeProfileData = nodeProfileService
                .getNodeProfile(networkProfileId);
        if (optionalNodeProfileData.isEmpty()) {
            log.info("The node profile does not exists.");
            throw new CustomException(HttpStatus.BAD_REQUEST, "Node profile does not exist",
                    "Cannot create the network since the node profile provided doesn't exist.");
        }
    }

    /*
    This method is used to check if the collector name has to be verified in case
    network update flow.
     */
    private boolean computeVerifyName(CollectorDataView collectorData) {
        Optional<Collector> optionalCollector = Optional.empty();
        if(null != collectorData.getId()) {
            optionalCollector = collectorRepository.findById(collectorData.getId());
            if(optionalCollector.isEmpty()) {
                log.error("The collector with ID {} is not present. " +
                        "Ignoring the Id and saving as a new Collector", collectorData.getId());
                collectorData.setId(null);
            }
        }
        return optionalCollector.isEmpty() || !optionalCollector.get().getName().equals(collectorData.getName());
    }

    private DemandCollectorView addDemandToolAsChildCollector(CollectorDataView collectorDataView, List<Long> childCollectorIds,
                                                List<ApiResponseDetail> apiResponseDetails) throws Exception {
        boolean isCopyDemandPresent = false;
        ObjectMapper objectMapper = new ObjectMapper();
        DemandCollectorView demandCollectorView = objectMapper.
                readValue(collectorDataView.getParams(), DemandCollectorView.class);

        SourceCollector sourceCollector = collectorDataView.getSourceCollector();
        if (null == sourceCollector) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Source collector must be specified for" +
                    " copy demands tool part of Traffic Demand collector.");
        }

        demandCollectorView.getDemandSteps().sort(Comparator.comparing(DemandStepView::getStepNumber));
        for (DemandStepView demandStepView : demandCollectorView.getDemandSteps()) {
            if (!demandStepView.getEnabled()) {
                log.info("Skipping the disabled step." + demandStepView);
                continue;
            }
            try {
                log.info("Adding demand step " + demandStepView + " with source collector " + sourceCollector);

                CollectorTypes toolType = demandStepView.getTool().getToolType();
                if (toolType.equals(CollectorTypes.TRAFFIC_DEMAND)) {
                    log.warn("No valid tool selected for the step " + demandStepView.getStepNumber());
                    continue;
                }

                if (toolType.equals(CollectorTypes.COPY_DEMANDS)) {
                    isCopyDemandPresent = true;
                }

                CollectorDataView childCollector = new CollectorDataView();
                childCollector.setType(toolType);
                childCollector.setSourceCollector(sourceCollector);
                childCollector.setConsolidationType(collectorDataView.getConsolidationType());
                childCollector.setParams(getDemandToolParams(demandStepView, toolType));
                String childCollectorName = collectorDataView.getName() + "_" + toolType;
                childCollector.setName(childCollectorName);

                List<Collector> childCollectorByName = collectorRepository.findByName(childCollectorName);
                if (childCollectorByName.size() > 0) {
                    log.debug("The child collector with the same name already exists with id {}. Assigning the same Id. ", childCollectorByName.get(0).getId());
                    childCollector.setId(childCollectorByName.get(0).getId());
                }

                saveCollectorEntity(childCollector, false, new ArrayList<>(), sourceCollector.getId());
                childCollectorIds.add(childCollector.getId());
                demandStepView.setId(childCollector.getId());

                sourceCollector = new SourceCollector();
                sourceCollector.setId(childCollector.getId());

            } catch (JsonProcessingException e) {
                log.error("Error creating the demand tool collector. " + demandStepView.getName(), e);
                ApiResponseDetail apiResponseDetail = new ApiResponseDetail();
                apiResponseDetail.setName(collectorDataView.getName() + "_" + demandStepView.getTool().getToolType());
                apiResponseDetail.setErrorDetails("Error creating the demand tool collectors " + demandStepView.getName()
                        + ". " + e.getMessage());
                apiResponseDetail.setStatus(false);
                apiResponseDetails.add(apiResponseDetail);
            }
        }

        collectorDataView.setParams(objectMapper.writeValueAsString(demandCollectorView));
        return isCopyDemandPresent ? demandCollectorView : null;
    }

    private void updateCopyDemandSourceData(DemandCollectorView demandCollectorView, Collector demandCollector) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        for (DemandStepView demandStepView : demandCollectorView.getDemandSteps()) {
            CollectorTypes toolType = demandStepView.getTool().getToolType();
            if(toolType.equals(CollectorTypes.COPY_DEMANDS)) {
                SourceCollector sourceCollector = demandStepView.getTool().getCopyDemands().getSourceCollector();
                collectorValidationService.checkForCopyDemandSource(sourceCollector);
                Long childCollectorId = demandStepView.getId();
                Optional<Collector> childCollectorOptional = collectorRepository.findById(childCollectorId);
                if(childCollectorOptional.isPresent()) {
                    Collector copyDemandTool = childCollectorOptional.get();
                    copyDemandTool.setParams(getDemandToolParams(demandStepView, CollectorTypes.COPY_DEMANDS));
                    collectorRepository.save(copyDemandTool);
                    demandCollector.setParams(objectMapper.writeValueAsString(demandCollectorView));
                }
                break;
            }
        }
    }

    private void addMulticastTypesAsChildCollector(CollectorDataView collectorData, List<Long> childCollectorIds,
                                              List<ApiResponseDetail> apiResponseDetails) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        MulticastCollectorView multicastCollector = objectMapper.
                readValue(collectorData.getParams(), MulticastCollectorView.class);

        Long sourceCollectorId = collectorData.getSourceCollector() != null ? collectorData.getSourceCollector().getId() : null;
        String params ;
        try {
            if (null != multicastCollector.getSnmpFindMulticastCollector()) {
                SnmpFindMulticastCollectorView snmpFindMulticastCollector = multicastCollector.getSnmpFindMulticastCollector();
                params = objectMapper.writeValueAsString(snmpFindMulticastCollector);

                Long childCollectorId = saveMulticastChildEntity(collectorData.getName(), snmpFindMulticastCollector.getId(),
                        CollectorTypes.SNMP_FIND_MULTICAST, params, sourceCollectorId);
                childCollectorIds.add(childCollectorId);
                snmpFindMulticastCollector.setId(childCollectorId);

                sourceCollectorId = childCollectorId;
            }

            if (null != multicastCollector.getSnmpPollMulticastCollector()) {
                SnmpPollMulticastCollectorView snmpPollMulticastCollector = multicastCollector.getSnmpPollMulticastCollector();
                params = objectMapper.writeValueAsString(snmpPollMulticastCollector);

                Long childCollectorId = saveMulticastChildEntity(collectorData.getName(), snmpPollMulticastCollector.getId(),
                        CollectorTypes.SNMP_POLL_MULTICAST, params, sourceCollectorId);
                childCollectorIds.add(childCollectorId);
                snmpPollMulticastCollector.setId(childCollectorId);

                sourceCollectorId = childCollectorId;
            }

            if (null != multicastCollector.getLoginFindMulticastCollector()) {
                LoginFindMulticastCollectorView loginFindMulticastCollector = multicastCollector.getLoginFindMulticastCollector();
                params = objectMapper.writeValueAsString(loginFindMulticastCollector);

                Long childCollectorId = saveMulticastChildEntity(collectorData.getName(), loginFindMulticastCollector.getId(),
                        CollectorTypes.LOGIN_FIND_MULTICAST, params, sourceCollectorId);
                childCollectorIds.add(childCollectorId);
                loginFindMulticastCollector.setId(childCollectorId);

                sourceCollectorId = childCollectorId;
            }

            if (null != multicastCollector.getLoginPollMulticastCollector()) {
                LoginPollMulticastCollectorView loginPollMulticastCollector = multicastCollector.getLoginPollMulticastCollector();
                params = objectMapper.writeValueAsString(loginPollMulticastCollector);

                Long childCollectorId = saveMulticastChildEntity(collectorData.getName(), loginPollMulticastCollector.getId(),
                        CollectorTypes.LOGIN_POLL_MULTICAST, params, sourceCollectorId);
                childCollectorIds.add(childCollectorId);
                loginPollMulticastCollector.setId(childCollectorId);
            }
        } catch (JsonProcessingException e) {
            log.error("Error creating the multicast type. " + collectorData.getName() , e);
            ApiResponseDetail apiResponseDetail = new ApiResponseDetail();
            apiResponseDetail.setName(collectorData.getName());
            apiResponseDetail.setErrorDetails("Error creating the multicast collector " + collectorData.getName()
                    + ". " + e.getMessage());
            apiResponseDetail.setStatus(false);
            apiResponseDetails.add(apiResponseDetail);
        }

        collectorData.setParams(objectMapper.writeValueAsString(multicastCollector));

    }

    private Long saveMulticastChildEntity(String name, Long id, CollectorTypes multicastType,
                                          String params, Long sourceCollectorId){
        CollectorDataView childCollector = new CollectorDataView();
        if(id != null) {
            childCollector.setId(id);
        }

        childCollector.setType(multicastType);
        childCollector.setParams(params);
        childCollector.setName(name + "_" + multicastType);

        saveCollectorEntity(childCollector, false, new ArrayList<>(), sourceCollectorId);
        return childCollector.getId();
    }

    private void addUpdateTrafficPollerAgent(Collector collectorEntity, String name ) {
        Optional<Long> agentId = collectorEntity.getAgentIds().stream().findFirst();
        log.debug("Traffic poller Agent id " + agentId.isPresent());

        AgentData agent = new AgentData();
        agentId.ifPresent(id -> {
            log.debug("Found Traffic poller Agent " + id);
            agent.setId(id);
        });
        agent.setName(name);
        agent.setType(AgentTypes.TRAFFIC_POLLER);

        ContinuosPollerAgent continuosPollerAgent = new ContinuosPollerAgent();
        continuosPollerAgent.setNetworkId(collectorEntity.getId());

        String params;
        try {
            params = new ObjectMapper().writeValueAsString(continuosPollerAgent);
            agent.setParams(params);

            if (agentId.isPresent()) {
                agentService.updateAgent(agent);
            } else {
                agentService.addAgent(agent, true);
            }
        } catch (Exception e) {
            log.error("Error adding or updating traffic poller agent ", e);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while restarting traffic poller agent", "Error while restarting traffic poller agent");
        }
        collectorEntity.addAgent(agent.getId());
    }

    private void deleteCollector(Long collectorId) {
        log.info("Deleting the collector " + collectorId);
        if(null != collectorId) {
           Optional<Collector> collectorEntityOptional = collectorRepository.findById(collectorId);
           collectorEntityOptional.ifPresent(collectorEntity -> {
               log.debug("Deleting the collector of type " + collectorEntity.getType());
               collectorCustomRepository.deleteCollector(collectorId);
               collectorEntity.getChildCollectorIds().forEach(childId -> collectorRepository.deleteById(childId));
               if(collectorEntity.getType().equals(CollectorTypes.TRAFFIC_POLL)){
                   log.info("Delete the associated Traffic poller agent.");
                   collectorEntity.getAgentIds().forEach(agentId -> agentService.deleteAgent(agentId));
               }

               if(collectorEntity.getType().equals(CollectorTypes.TOPO_BGPLS_XTC)){
                   log.info("Send request to stop bgpls collector");
                   srPceWebClient.stopBgpls(collectorId);
               } else if(collectorEntity.getType().equals(CollectorTypes.LSP_PCEP_XTC)){
                   log.info("Send request to stop pcep lsp collector");
                   srPceWebClient.stopPcepLsp(collectorId);
               }

           });
        }
    }

    /*
    Update the aggregator properties.
     */
    private void updateAggPropertiesForAllCustomCollectors(NetworkDataView networkDataView){
        Set<CollectorDataView>  collectorDataViewSet = networkDataView.getCollectors();
        if(networkDataView.isDraft() || collectorDataViewSet == null){
            return;
        }
        for(CollectorDataView collectorDataView : collectorDataViewSet){
            if(collectorDataView.getType().equals(CollectorTypes.EXTERNAL_SCRIPT)) {
                log.debug("External executor  collector is present ");
                String params = collectorDataView.getParams();
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    CustomCollector customCollector = objectMapper.readValue(params, CustomCollector.class);
                    aggregatorService.updateAggregatorProperties(networkDataView.getId(), networkDataView.getName(),
                            collectorDataView.getId(),customCollector.getAggregatorProperties());
                } catch (Exception e) {
                    log.error("Error updating the aggregator properties for the custom collector ",e);
                }
            } else if(collectorDataView.getType().equals(CollectorTypes.TRAFFIC_DEMAND)) {
                log.debug("Verify if External executor step is present ");
                String params = collectorDataView.getParams();
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    DemandCollectorView demandCollectorView = objectMapper.readValue(params, DemandCollectorView.class);
                    for(DemandStepView demandStepView : demandCollectorView.getDemandSteps()) {
                        if(demandStepView.getTool().getExternalExecutable() !=  null){
                            log.debug("Found the external executor step. ");
                            CustomCollector customCollector = demandStepView.getTool().getExternalExecutable();
                            aggregatorService.updateAggregatorProperties(networkDataView.getId(), networkDataView.getName(),
                                    collectorDataView.getId(),customCollector.getAggregatorProperties());
                        }
                    }
                } catch (Exception e) {
                    log.error("Error updating the aggregator properties for " +
                            "the custom collector step part of Demand collection",e);
                }
            }
        }
    }
}
