package com.cisco.configService.service;

import com.cisco.configService.entity.Agents;
import com.cisco.configService.enums.AgentTypes;
import com.cisco.configService.enums.CollectorTypes;
import com.cisco.configService.exception.CustomException;
import com.cisco.configService.model.composer.*;
import com.cisco.configService.model.composer.cli.CollectorData;
import com.cisco.configService.model.custom.CustomCollector;
import com.cisco.configService.model.demand.*;
import com.cisco.configService.model.demand.ui.DemandCollectorView;
import com.cisco.configService.model.demand.ui.DemandStepView;
import com.cisco.configService.model.demand.ui.DemandToolView;
import com.cisco.configService.model.multicast.ui.*;
import com.cisco.configService.model.preConfig.AgentData;
import com.cisco.configService.model.preConfig.AllAgentData;
import com.cisco.configService.model.preConfig.AllNodeProfileData;
import com.cisco.configService.model.preConfig.NodeProfileData;
import com.cisco.configService.model.topoBgpls.ui.BgpLsCollectorView;
import com.cisco.configService.webClient.WorkflowManagerWebClient;
import com.cisco.workflowmanager.ConsolidationType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
public class NetworkTest {

    @Autowired
    NetworkService networkService;

    @MockBean
    NodeProfileService nodeProfileService;

    @MockBean
    AgentService agentService;

    @MockBean
    AggregatorService aggregatorService;

    @MockBean
    WorkflowManagerWebClient webClient;

    @Autowired
    CollectorService collectorService;

    private static final Logger logger = LogManager.getLogger(NetworkTest.class);
    private static final Long networkProfileId = 1L;
    private static final Long agentId = 1L;


    @Test
    void testAddNetwork() {

        Mockito.doReturn(Optional.of(new NodeProfileData())).when(nodeProfileService).getNodeProfile(networkProfileId);
        SourceCollector sourceCollector = new SourceCollector();
        sourceCollector.setId(1L);

        NetworkDataView networkData = new NetworkDataView();
        networkData.setName("addIgpBgpVpnLspNetwork");

        CollectorDataView collectorData = new CollectorDataView();
        collectorData.setName("TOPO_IGP_add");
        collectorData.setType(CollectorTypes.TOPO_IGP);
        collectorData.setConsolidationType(ConsolidationType.DARE);
        collectorData.setParams("{\"igpConfigs\":[{\"igpIndex\":1,\"seedRouter\":\"10.10.10.10\",\"igpProtocol\":\"ISIS\",\"advanced\":{\"backupRouter\":null,\"getSegment\":false,\"isisLevel\":\"2\",\"ospfArea\":\"0\",\"ospfProcessIds\":[],\"isisProcessIds\":[],\"removeNullProcessId\":false,\"runIGPOffline\":\"OFF\",\"nodeTag\":null,\"loginConfig\":{\"forceLoginPlatform\":null,\"fallbackLoginPlatform\":null,\"sendEnablePassword\":false,\"telnetUserName\":\"cisco\",\"telnetPassword\":\"cisco\"},\"timeout\":60,\"debug\":{\"netRecorder\":\"RECORD\",\"verbosity\":60}}}],\"collectInterfaces\":true,\"advanced\":{\"nodes\":{\"qosNodeFilterList\":null,\"performanceData\":false,\"removeNodeSuffix\":[],\"discoverQosQueue\":true,\"timeout\":60,\"debug\":{\"netRecorder\":\"RECORD\",\"verbosity\":60}},\"interfaces\":{\"findParallelLinks\":false,\"ipGuessing\":\"SAFE\",\"discoverLags\":false,\"lagPortMatch\":\"GUESS\",\"circuitCleanup\":false,\"copyDescription\":false,\"collectPhysicalPort\":false,\"minIPGuessPrefixLength\":0,\"minPrefixLength\":30,\"timeout\":60,\"debug\":{\"netRecorder\":\"RECORD\",\"verbosity\":60}}}}");

        CollectorDataView bgpCollector = new CollectorDataView();
        bgpCollector.setName("TOPO_BGP_add");
        bgpCollector.setType(CollectorTypes.TOPO_BGP);
        bgpCollector.setConsolidationType(ConsolidationType.DARE);
        bgpCollector.setSourceCollector(sourceCollector);
        bgpCollector.setParams("{\"advanced\":{\"protocol\":[],\"minPrefixLength\":24,\"minIPv6PrefixLength\":64,\"loginToRouterForMultihop\":true,\"loginConfig\":{\"encodedTelnetPassword\":null,\"forceLoginPlatform\":null,\"fallbackLoginPlatform\":null,\"sendEnablePassword\":false,\"telnetUserName\":\"cisco\",\"telnetPassword\":\"cisco\"},\"findInternalASNLinks\":false,\"findNonIPExitInterface\":false,\"findInternalExitInterface\":false,\"getMacAddress\":false,\"useDNS\":false,\"forceCheckAll\":false,\"timeout\":60,\"debug\":{\"netRecorder\":\"OFF\",\"verbosity\":30,\"loginRecordMode\":\"OFF\"}}}");
        CollectorDataView vpnCollector = new CollectorDataView();
        vpnCollector.setName("TOPO_VPN_add");
        vpnCollector.setType(CollectorTypes.TOPO_VPN);
        vpnCollector.setSourceCollector(sourceCollector);
        vpnCollector.setConsolidationType(ConsolidationType.DARE);

        CollectorDataView lspSnmpCollector = new CollectorDataView();
        lspSnmpCollector.setName("LSP_SNMP_add");
        lspSnmpCollector.setType(CollectorTypes.LSP_SNMP);
        lspSnmpCollector.setSourceCollector(sourceCollector);
        lspSnmpCollector.setConsolidationType(ConsolidationType.DARE);

        networkData.setCollectors(Set.of(collectorData, bgpCollector, vpnCollector));

        AllNodeProfileData allNodeProfileData = new AllNodeProfileData();
        allNodeProfileData.setId(networkProfileId);
        allNodeProfileData.setName("NetworkProfile");

        networkData.setNodeProfileData(allNodeProfileData);

        NodeProfileData nodeProfileData = new NodeProfileData();
        BeanUtils.copyProperties(allNodeProfileData, nodeProfileData);

        Mockito.doReturn(Optional.of(nodeProfileData)).when(nodeProfileService).getNodeProfile(networkProfileId);

        networkService.saveNetwork(networkData);


        logger.info("Network ID " + networkData.getId());
        networkData.getCollectors().forEach(collector -> {
            logger.info("Collector Id :: " + collector.getId());
            Assertions.assertNotNull(collector.getId());
            Optional<CollectorData> collectorCli = collectorService.getCollector(collector.getId());
            Assertions.assertTrue(collectorCli.isPresent());
            Assertions.assertEquals(60,collectorCli.get().getTimeout());
        });

        Assertions.assertNotNull(networkData.getId());
    }

    @Test
    void testAddBgpLsNetwork() throws JsonProcessingException {
        ObjectMapper Obj = new ObjectMapper();
        SourceCollector sourceCollector = new SourceCollector();
        sourceCollector.setId(1L);

        SourceCollector sourceCollector2 = new SourceCollector();
        sourceCollector2.setType(CollectorTypes.LSP_PCEP_XTC);
        sourceCollector2.setName("Pcep Collector_1");

        SourceCollector sourceCollector3 = new SourceCollector();
        sourceCollector3.setType(CollectorTypes.DARE);

        Mockito.doReturn(Optional.of(new NodeProfileData())).when(nodeProfileService).getNodeProfile(networkProfileId);

        NetworkDataView networkData = new NetworkDataView();
        networkData.setName("addBgpLsPcTrafficNetwork");

        CollectorDataView collectorData = new CollectorDataView();
        collectorData.setName("BGPLS_NIMO_1");
        collectorData.setType(CollectorTypes.TOPO_BGPLS_XTC);
        collectorData.setConsolidationType(ConsolidationType.DARE);

        BgpLsCollectorView bgpLsCollector = new BgpLsCollectorView();
        bgpLsCollector.setPrimarySrPceAgent(agentId);
        String collectorParams = Obj.writeValueAsString(bgpLsCollector);
        logger.info("Collector params = " + collectorParams);
        collectorData.setParams(collectorParams);

        AllAgentData agents = new AllAgentData();
        agents.setName("SR_PCE_AGENT");
        agents.setType(AgentTypes.SR_PCE_AGENT);
        agents.setId(agentId);

        AgentData agentData = new AgentData();
        BeanUtils.copyProperties(agents, agentData);
        collectorData.setAgents(Set.of(agents));

        CollectorDataView cPcepCollector = new CollectorDataView();
        cPcepCollector.setName("Pcep Collector_1");
        cPcepCollector.setSourceCollector(sourceCollector);
        cPcepCollector.setType(CollectorTypes.LSP_PCEP_XTC);
        cPcepCollector.setAgents(Set.of(agents));
        cPcepCollector.setConsolidationType(ConsolidationType.DARE);

        CollectorDataView aPcCollector = new CollectorDataView();
        aPcCollector.setName("Parse Config Collector_1");
        aPcCollector.setSourceCollector(sourceCollector2);
        aPcCollector.setType(CollectorTypes.CONFIG_PARSE);
        aPcCollector.setParams("{\"getConfig\":{\"enable\":true,\"loginConfig\":{\"encodedTelnetPassword\":null,\"forceLoginPlatform\":null,\"fallbackLoginPlatform\":null,\"sendEnablePassword\":false,\"telnetUserName\":\"cisco\",\"telnetPassword\":\"cisco\"},\"timeout\":60,\"debug\":{\"loginRecordMode\":\"OFF\",\"verbosity\":30}},\"parseConfig\":{\"igpProtocol\":\"ISIS\",\"isisLevel\":\"2\",\"ospfArea\":\"0.0.0.0\",\"asn\":null,\"includeObjects\":[],\"parseConfigAdvanced\":{\"circuitMatch\":\"SAME_IGP\",\"lagPortMatch\":\"GUESS\",\"ospfProcessIds\":[],\"isisProcessIds\":[],\"selectLoopBackInt\":0,\"resolveReferences\":true,\"useMultiThreading\":false,\"filterShowCommands\":false,\"buildTopology\":true,\"sharedMedia\":true,\"advancedToolOptions\":[],\"timeout\":60,\"debug\":{\"verbosity\":30}}}}");

        CollectorDataView trafficCollector = new CollectorDataView();
        trafficCollector.setName("Traffic Poller Collector_1");
        trafficCollector.setSourceCollector(sourceCollector3);
        trafficCollector.setType(CollectorTypes.TRAFFIC_POLL);
        trafficCollector.setConsolidationType(ConsolidationType.SAGE);

        networkData.setCollectors(Set.of(collectorData, aPcCollector, cPcepCollector, trafficCollector));

        AllNodeProfileData allNodeProfileData = new AllNodeProfileData();
        allNodeProfileData.setId(networkProfileId);
        allNodeProfileData.setName("NetworkProfile");

        networkData.setNodeProfileData(allNodeProfileData);

        NodeProfileData nodeProfileData = new NodeProfileData();
        BeanUtils.copyProperties(allNodeProfileData, nodeProfileData);

        Mockito.doReturn(Optional.of(nodeProfileData)).when(nodeProfileService).getNodeProfile(networkProfileId);
        Mockito.doReturn(Optional.of(agentData)).when(agentService).getAgent(agentId);
        ObjectMapper objectMapper = new ObjectMapper();
        logger.debug("Network json " + objectMapper.writeValueAsString(networkData));
        networkService.saveNetwork(networkData);

        logger.info("Network ID " + networkData.getId());
        networkData.getCollectors().forEach(collector -> {
            logger.info("Collector Id" + collector.getId() + " Source collector " + collector.getSourceCollector());
            Assertions.assertNotNull(collector.getId());
            Assertions.assertNotNull(collector.getParams());
            Assertions.assertNotNull(collector.getConsolidationType());
            Assertions.assertNotNull(collector.getTimeout());
            collector.getAgents().forEach(agent -> logger.info(" Agent Id : " + agent));
        });

        Assertions.assertNotNull(networkData.getId());
    }

    @Test
    void testUpdateNetwork() {

        Mockito.doReturn(Optional.of(new NodeProfileData())).when(nodeProfileService).getNodeProfile(networkProfileId);

        NetworkDataView networkData = new NetworkDataView();
        networkData.setName("updateNetwork");
        Set<CollectorDataView> collectorDataSet = new HashSet<>();

        CollectorDataView collectorData = new CollectorDataView();
        collectorData.setName("TOPO_IGP");
        collectorData.setType(CollectorTypes.TOPO_IGP);
        collectorData.setParams("{\"igpConfigs\":[{\"igpIndex\":1,\"seedRouter\":\"x.x.x.x\",\"igpProtocol\":\"ISIS\",\"advanced\":{\"backupRouter\":null,\"getSegment\":false,\"isisLevel\":\"2\",\"ospfArea\":\"0\",\"ospfProcessIds\":[],\"isisProcessIds\":[],\"removeNullProcessId\":false,\"runIGPOffline\":\"OFF\",\"nodeTag\":null,\"loginConfig\":{\"forceLoginPlatform\":null,\"fallbackLoginPlatform\":null,\"sendEnablePassword\":false,\"telnetUserName\":null,\"telnetPassword\":null},\"timeout\":60,\"debug\":{\"netRecorder\":\"RECORD\",\"verbosity\":60}}}],\"collectInterfaces\":true,\"advanced\":{\"nodes\":{\"qosNodeFilterList\":null,\"performanceData\":false,\"removeNodeSuffix\":[],\"discoverQosQueue\":true,\"timeout\":60,\"debug\":{\"netRecorder\":\"RECORD\",\"verbosity\":60}},\"interfaces\":{\"findParallelLinks\":false,\"ipGuessing\":\"SAFE\",\"discoverLags\":false,\"lagPortMatch\":\"GUESS\",\"circuitCleanup\":false,\"copyDescription\":false,\"collectPhysicalPort\":false,\"minIPGuessPrefixLength\":0,\"minPrefixLength\":30,\"timeout\":60,\"debug\":{\"netRecorder\":\"RECORD\",\"verbosity\":60}}}}");
        collectorDataSet.add(collectorData);

        AllAgentData agents = new AllAgentData();
        agents.setName("SR_PCE_AGENT");
        agents.setType(AgentTypes.SR_PCE_AGENT);
        agents.setId(agentId);

        Set<AllAgentData> agentDataList = new HashSet<>();
        agentDataList.add(agents);

        collectorData.setAgents(agentDataList);
        networkData.setCollectors(collectorDataSet);

        AllNodeProfileData allNodeProfileData = new AllNodeProfileData();
        allNodeProfileData.setId(networkProfileId);
        allNodeProfileData.setName("NetworkProfile");

        networkData.setNodeProfileData(allNodeProfileData);

        networkService.saveNetwork(networkData);

        logger.info("Save Network ID {} and name {} ", networkData.getId(), networkData.getName());
        networkData.getCollectors().forEach(collector -> {
            logger.info("Collector Id" + collector.getId());
            collector.getAgents().forEach(agent -> logger.info(" Agent Id : " + agent));
        });

        Assertions.assertNotNull(networkData.getId());

        CollectorDataView collectorData2 = new CollectorDataView();
        collectorData2.setName("Topo_Igp");
        collectorData2.setType(CollectorTypes.TOPO_IGP);
        collectorData2.setParams("{\"igpConfigs\":[{\"igpIndex\":1,\"seedRouter\":\"x.x.x.x\",\"igpProtocol\":\"ISIS\",\"advanced\":{\"backupRouter\":null,\"getSegment\":false,\"isisLevel\":\"2\",\"ospfArea\":\"0\",\"ospfProcessIds\":[],\"isisProcessIds\":[],\"removeNullProcessId\":false,\"runIGPOffline\":\"OFF\",\"nodeTag\":null,\"loginConfig\":{\"forceLoginPlatform\":null,\"fallbackLoginPlatform\":null,\"sendEnablePassword\":false,\"telnetUserName\":null,\"telnetPassword\":null},\"timeout\":60,\"debug\":{\"netRecorder\":\"RECORD\",\"verbosity\":60}}}],\"collectInterfaces\":true,\"advanced\":{\"nodes\":{\"qosNodeFilterList\":null,\"performanceData\":false,\"removeNodeSuffix\":[],\"discoverQosQueue\":true,\"timeout\":60,\"debug\":{\"netRecorder\":\"RECORD\",\"verbosity\":60}},\"interfaces\":{\"findParallelLinks\":false,\"ipGuessing\":\"SAFE\",\"discoverLags\":false,\"lagPortMatch\":\"GUESS\",\"circuitCleanup\":false,\"copyDescription\":false,\"collectPhysicalPort\":false,\"minIPGuessPrefixLength\":0,\"minPrefixLength\":30,\"timeout\":60,\"debug\":{\"netRecorder\":\"RECORD\",\"verbosity\":60}}}}");

        collectorDataSet.add(collectorData2);
        networkService.updateNetwork(networkData);

        logger.info("Update with 2 collectors Network ID {} and name {} ", networkData.getId(), networkData.getName());
        networkData.getCollectors().forEach(collectors -> {
            logger.info("Collector Id {} name {} ", collectors.getId(), collectors.getName());
            collectors.getAgents().forEach(agentData -> logger.info(" Agent Id : " + agentData));
        });

        collectorData2.setAgents(agentDataList);
        networkService.updateNetwork(networkData);

        logger.info("Add agent to 2nd collector network ID {} and name {} ", networkData.getId(), networkData.getName());
        networkData.getCollectors().forEach(collectors -> {
            logger.info("Collector Id {} name {} ", collectors.getId(), collectors.getName());
            collectors.getAgents().forEach(agentData -> logger.info(" Agent Id : " + agentData));
        });

        Set<CollectorDataView> collectorDataSet2 = new HashSet<>();
        collectorDataSet2.add(collectorData2);
        networkData.setCollectors(collectorDataSet2);

        logger.info("Before Delete 2nd collector network ID {} and name {} ", networkData.getId(), networkData.getName());
        networkData.getCollectors().forEach(collectors -> {
            logger.info("Collector Id {} name {} ", collectors.getId(), collectors.getName());
            collectors.getAgents().forEach(agentData -> logger.info(" Agent Id : " + agentData));
        });
        networkService.updateNetwork(networkData);

        logger.info("Delete 2nd collector network ID {} and name {} ", networkData.getId(), networkData.getName());
        networkData.getCollectors().forEach(collectors -> {
            logger.info("Collector Id {} name {} ", collectors.getId(), collectors.getName());
            collectors.getAgents().forEach(agentData -> logger.info(" Agent Id : " + agentData));
        });
    }

    @Test
    void getNetworksTest() {
        Agents agentData = new Agents();
        agentData.setName("AgentName");
        agentData.setId(agentId);
        agentData.setType(AgentTypes.SR_PCE_AGENT);
        Mockito.doReturn(List.of(agentData)).when(agentService).getAgentByCollectorId(any());

        NodeProfileData nodeProfileData = new NodeProfileData();
        nodeProfileData.setId(1L);
        nodeProfileData.setName("node-profile");

        Mockito.doReturn(Optional.of(nodeProfileData)).when(nodeProfileService).getNodeProfile(Mockito.anyLong());

        List<NetworkDataInfo> networkDataList = networkService.getAllNetworkData();
        networkDataList.forEach(allNetworkData -> {
            logger.info("Get the network details related to Id {} and name {} ",
                    allNetworkData.getId(), allNetworkData.getName());
            Assertions.assertNotNull(allNetworkData.getNodeProfile());
            Optional<NetworkDataView> networkData = networkService.getNetwork(allNetworkData.getId());
            Assertions.assertTrue(networkData.isPresent());
            logger.info("Network Data " + networkData);
        });
    }

    @Test
    void deleteNetworksTest() {
        Mockito.doReturn(Optional.of(new NodeProfileData())).when(nodeProfileService).getNodeProfile(networkProfileId);
        Mockito.doNothing().when(webClient).deleteSchedulersOfNetwork(Mockito.anyLong());

        NetworkDataView networkData = new NetworkDataView();
        networkData.setName("DeleteNetwork");

        AllNodeProfileData allNodeProfileData = new AllNodeProfileData();
        allNodeProfileData.setId(networkProfileId);
        allNodeProfileData.setName("NetworkProfile");

        networkData.setNodeProfileData(allNodeProfileData);
        networkService.saveNetwork(networkData);

        Optional<Long> optionalId = networkService.deleteNetwork(networkData.getId());
        Assertions.assertTrue(optionalId.isPresent());
        Assertions.assertEquals(networkData.getId(), optionalId.get());
    }

    @Test
    void testExternalScriptDemandsNetwork() throws JsonProcessingException {
        Mockito.doReturn(Optional.of(new NodeProfileData())).when(nodeProfileService).getNodeProfile(networkProfileId);
        Mockito.doNothing().when(aggregatorService).validateAggregatorProperties(Mockito.any());
        Mockito.doNothing().when(aggregatorService).updateAggregatorProperties(Mockito.anyLong(),Mockito.any(),
                Mockito.anyLong(), Mockito.any());

        ObjectMapper obj = new ObjectMapper();
        SourceCollector sourceCollector = new SourceCollector();
        sourceCollector.setId(1L);

        NetworkDataView networkData = new NetworkDataView();
        networkData.setName("DemandNetworkWithExternalScript");
        Set<CollectorDataView> collectorDataSet = new HashSet<>();

        CollectorDataView collectorData = new CollectorDataView();
        collectorData.setName("Demand_Collector");
        collectorData.setType(CollectorTypes.TRAFFIC_DEMAND);

        DemandCollector demandCollector = new DemandCollector();
        List<DemandStep> demandSteps = new ArrayList<>();

        DemandStep demandStep = new DemandStep();
        demandStep.setName("Step1");
        demandStep.setEnabled(true);
        demandStep.setStepNumber(1);
        DemandTool tool = new DemandTool();
        CustomCollector customCollector = new CustomCollector();
        customCollector.setExecutableScript("script.exe");
        customCollector.setAggregatorProperties("ExternalScript.properties");
        tool.setExternalExecutable(customCollector);
        demandStep.setTool(tool);
        demandSteps.add(demandStep);

        demandCollector.setDemandSteps(demandSteps);

        String params = obj.writeValueAsString(demandCollector);
        logger.info("Demand collector params with external executor step : " + params);
        collectorData.setParams(params);
        collectorData.setSourceCollector(sourceCollector);
        collectorDataSet.add(collectorData);

        AllNodeProfileData allNodeProfileData = new AllNodeProfileData();
        allNodeProfileData.setId(networkProfileId);
        allNodeProfileData.setName("NetworkProfile");

        networkData.setNodeProfileData(allNodeProfileData);
        networkData.setCollectors(collectorDataSet);

        networkService.saveNetwork(networkData);
        logger.info("Network Id " + networkData.getId());

        Optional<NetworkDataView> readNetworkOptional = networkService.getNetwork(networkData.getId());
        Assertions.assertTrue(readNetworkOptional.isPresent());
        NetworkDataView updateNetwork = readNetworkOptional.get();
        Assertions.assertNotNull(updateNetwork.getId());
        logger.info("Read network with external executor step : " + updateNetwork);
    }

    @Test
    void testAddUpdateDeleteDemandsNetwork() throws JsonProcessingException {
        Mockito.doReturn(Optional.of(new NodeProfileData())).when(nodeProfileService).getNodeProfile(networkProfileId);

        ObjectMapper obj = new ObjectMapper();
        SourceCollector sourceCollector = new SourceCollector();
        sourceCollector.setId(1L);

        NetworkDataView networkData = new NetworkDataView();
        networkData.setName("DemandNetwork");
        Set<CollectorDataView> collectorDataSet = new HashSet<>();

        CollectorDataView collectorData = new CollectorDataView();
        collectorData.setName("Demand_Collector");
        collectorData.setType(CollectorTypes.TRAFFIC_DEMAND);

        DemandCollector demandCollector = new DemandCollector();
        List<DemandStep> demandSteps = new ArrayList<>();

        DemandStep demandStep = new DemandStep();
        demandStep.setName("Step1");
        demandStep.setEnabled(true);
        demandStep.setStepNumber(1);
        DemandTool tool = new DemandTool();
        tool.setDmdMeshCreator(new DmdMeshCreator());
        demandStep.setTool(tool);
        demandSteps.add(demandStep);

        demandStep = new DemandStep();
        demandStep.setName("Step2");
        demandStep.setEnabled(true);
        demandStep.setStepNumber(2);
        tool = new DemandTool();
        tool.setDmdsForLsps(new DmdsForLsps());
        demandStep.setTool(tool);
        demandSteps.add(demandStep);

        demandStep = new DemandStep();
        demandStep.setName("Step3");
        demandStep.setEnabled(true);
        demandStep.setStepNumber(3);
        tool = new DemandTool();
        tool.setDmdsForP2mplsps(new DmdsForP2mplsps());
        demandStep.setTool(tool);
        demandSteps.add(demandStep);

        demandStep = new DemandStep();
        demandStep.setName("Step4");
        demandStep.setEnabled(true);
        demandStep.setStepNumber(4);
        tool = new DemandTool();
        tool.setDemandDeduction(new DemandDeduction());
        demandStep.setTool(tool);
        demandSteps.add(demandStep);
        demandCollector.setDemandSteps(demandSteps);

        String params = obj.writeValueAsString(demandCollector);
        logger.info("Demand collector params with 4 steps : " + params);
        collectorData.setParams(params);
        collectorData.setSourceCollector(sourceCollector);
        collectorDataSet.add(collectorData);

        AllNodeProfileData allNodeProfileData = new AllNodeProfileData();
        allNodeProfileData.setId(networkProfileId);
        allNodeProfileData.setName("NetworkProfile");

        networkData.setNodeProfileData(allNodeProfileData);
        networkData.setCollectors(collectorDataSet);

        networkService.saveNetwork(networkData);
        logger.info("Network Id " + networkData.getId());

        Optional<NetworkDataView> readNetworkOptional = networkService.getNetwork(networkData.getId());
        Assertions.assertTrue(readNetworkOptional.isPresent());
        NetworkDataView updateNetwork = readNetworkOptional.get();
        Assertions.assertNotNull(updateNetwork.getId());
        logger.info("Read network with 4 steps : " + updateNetwork);
        Set<CollectorDataView> updateCollectorSet = updateNetwork.getCollectors();
        updateCollectorSet.forEach(updateCollector -> {
            logger.info("Executing update for collector " + updateCollector);
            Assertions.assertDoesNotThrow(() ->  {
                DemandCollectorView updateDemandCollector = obj.
                        readValue(updateCollector.getParams(), DemandCollectorView.class);

                Assertions.assertEquals(4, updateDemandCollector.getDemandSteps().size());
                DemandStepView updateDemandStep = new DemandStepView();
                updateDemandStep.setName("Step5");
                updateDemandStep.setEnabled(true);
                updateDemandStep.setStepNumber(5);
                DemandToolView updateTool = new DemandToolView();
                updateTool.setCopyDemands(new CopyDemands());
                updateDemandStep.setTool(updateTool);

                updateDemandCollector.getDemandSteps().add(updateDemandStep);
                String updateParams = obj.writeValueAsString(updateDemandCollector);
                logger.info("Updated Demand collector params with invalid copy demands : " + updateParams);
                updateCollector.setParams(updateParams);

            });

        });

        readNetworkOptional = networkService.getNetwork(networkData.getId());
        Assertions.assertTrue(readNetworkOptional.isPresent());
        updateNetwork = readNetworkOptional.get();
        Assertions.assertNotNull(updateNetwork.getId());
        logger.info("Read network with invalid copy demand step : " + updateNetwork);
        updateCollectorSet = updateNetwork.getCollectors();
        updateCollectorSet.forEach(updateCollector -> {
            logger.info("Executing update for collector " + updateCollector);
            Assertions.assertDoesNotThrow(() ->  {
                DemandCollectorView updateDemandCollector = obj.
                        readValue(updateCollector.getParams(), DemandCollectorView.class);
                Assertions.assertEquals(4, updateDemandCollector.getDemandSteps().size());

                DemandStepView updateDemandStep = new DemandStepView();
                updateDemandStep.setName("Step5");
                updateDemandStep.setEnabled(true);
                updateDemandStep.setStepNumber(5);
                DemandToolView updateTool = new DemandToolView();
                CopyDemands copyDemands = new CopyDemands();
                SourceCollector copyFrom = new SourceCollector();
                copyFrom.setId(1L);
                copyDemands.setSourceCollector(copyFrom);
                updateTool.setCopyDemands(copyDemands);
                updateDemandStep.setTool(updateTool);

                updateDemandCollector.getDemandSteps().add(updateDemandStep);
                String updateParams = obj.writeValueAsString(updateDemandCollector);
                logger.info("Updated Demand collector params with valid copy demands : " + updateParams);
                updateCollector.setParams(updateParams);

            });
        });
        updateNetwork.setNodeProfileData(allNodeProfileData);
        networkService.updateNetwork(updateNetwork);

        readNetworkOptional = networkService.getNetwork(networkData.getId());
        Assertions.assertTrue(readNetworkOptional.isPresent());
        updateNetwork = readNetworkOptional.get();
        Assertions.assertNotNull(updateNetwork.getId());
        logger.info("Read network with valid copy demands : " + updateNetwork);
        updateCollectorSet = updateNetwork.getCollectors();
        updateCollectorSet.forEach(updateCollector -> {
            logger.info("Executing update for collector " + updateCollector);
            Assertions.assertDoesNotThrow(() ->  {
                DemandCollectorView updateDemandCollector = obj.
                        readValue(updateCollector.getParams(), DemandCollectorView.class);

                Assertions.assertEquals(5, updateDemandCollector.getDemandSteps().size());
                List<DemandStepView> updateDemandStepList = updateDemandCollector.getDemandSteps();
                updateDemandStepList.forEach(demandStepView -> demandStepView.setId(null));
                updateDemandCollector.setDemandSteps(updateDemandStepList);
                String updateParams = obj.writeValueAsString(updateDemandCollector);
                logger.info("Updated Demand collector params with repeated step name : " + updateParams);
                updateCollector.setParams(updateParams);

            });
        });
        updateNetwork.setNodeProfileData(allNodeProfileData);
        networkService.updateNetwork(updateNetwork);

        readNetworkOptional = networkService.getNetwork(networkData.getId());
        Assertions.assertTrue(readNetworkOptional.isPresent());
        updateNetwork = readNetworkOptional.get();
        Assertions.assertNotNull(updateNetwork.getId());
        logger.info("Read network with repeated step name : " + updateNetwork);
        Set<CollectorDataView> deleteCollectorSet = updateNetwork.getCollectors();
        deleteCollectorSet.forEach(updateCollector -> {
            logger.info("Executing delete for collector " + updateCollector);
            Assertions.assertDoesNotThrow(() ->  {
                DemandCollectorView updateDemandCollector = obj.
                        readValue(updateCollector.getParams(), DemandCollectorView.class);
                Assertions.assertEquals(5, updateDemandCollector.getDemandSteps().size());

                updateDemandCollector.getDemandSteps().clear();
                String updateParams = obj.writeValueAsString(updateDemandCollector);
                logger.info("Demand collector params with all steps deleted: " + updateParams);
                updateCollector.setParams(updateParams);
            });
        });
        updateNetwork.setCollectors(deleteCollectorSet);
        updateNetwork.setNodeProfileData(allNodeProfileData);
        networkService.updateNetwork(updateNetwork);
    }

    @Test
    void testAddUpdateDeleteMulticastNetwork() throws JsonProcessingException {
        Mockito.doReturn(Optional.of(new NodeProfileData())).when(nodeProfileService).getNodeProfile(networkProfileId);

        ObjectMapper obj = new ObjectMapper();
        SourceCollector sourceCollector = new SourceCollector();
        sourceCollector.setId(1L);

        NetworkDataView networkData = new NetworkDataView();
        networkData.setName("MulticastNetwork");
        Set<CollectorDataView> collectorDataSet = new HashSet<>();

        CollectorDataView collectorData = new CollectorDataView();
        collectorData.setName("Multicast_Collector");
        collectorData.setType(CollectorTypes.MULTICAST);

        MulticastCollectorView multicastCollector = new MulticastCollectorView();
        multicastCollector.setSnmpFindMulticastCollector(new SnmpFindMulticastCollectorView());
        multicastCollector.setSnmpPollMulticastCollector(new SnmpPollMulticastCollectorView());

        String params = obj.writeValueAsString(multicastCollector);
        logger.info("Multicast collector params : " + params);
        collectorData.setParams(params);
        collectorData.setSourceCollector(sourceCollector);
        collectorDataSet.add(collectorData);


        AllNodeProfileData allNodeProfileData = new AllNodeProfileData();
        allNodeProfileData.setId(networkProfileId);
        allNodeProfileData.setName("NetworkProfile");

        networkData.setNodeProfileData(allNodeProfileData);
        networkData.setCollectors(collectorDataSet);

        networkService.saveNetwork(networkData);
        logger.info("Network Id " + networkData.getId());

        Optional<NetworkDataView> readNetworkOptional = networkService.getNetwork(networkData.getId());
        Assertions.assertTrue(readNetworkOptional.isPresent());
        NetworkDataView updateNetwork = readNetworkOptional.get();
        Assertions.assertNotNull(updateNetwork.getId());
        logger.info("Read network " + updateNetwork);
        Set<CollectorDataView> updateCollectorSet = updateNetwork.getCollectors();
        updateCollectorSet.forEach(updateCollector -> {
            logger.info("Executing update for collector " + updateCollector);
             Assertions.assertDoesNotThrow(() ->  {
                MulticastCollectorView updateMulticast = obj.
                        readValue(updateCollector.getParams(), MulticastCollectorView.class);

                updateMulticast.setLoginFindMulticastCollector(new LoginFindMulticastCollectorView());
                updateMulticast.setLoginPollMulticastCollector(new LoginPollMulticastCollectorView());

                String updateParams = obj.writeValueAsString(updateMulticast);
                logger.info("Update Multicast collector params : " + updateParams);
                updateCollector.setParams(updateParams);

            });
        });
        updateNetwork.setNodeProfileData(allNodeProfileData);
        networkService.updateNetwork(updateNetwork);

        readNetworkOptional = networkService.getNetwork(networkData.getId());
        Assertions.assertTrue(readNetworkOptional.isPresent());
        updateNetwork = readNetworkOptional.get();
        Assertions.assertNotNull(updateNetwork.getId());
        logger.info("Read network with all steps " + updateNetwork);
        Set<CollectorDataView> deleteCollectorSet = updateNetwork.getCollectors();
        deleteCollectorSet.forEach(updateCollector -> {
            logger.info("Executing delete of multicast type for collector " + updateCollector);
            Assertions.assertDoesNotThrow(() ->  {
                MulticastCollectorView updateMulticast = obj.
                        readValue(updateCollector.getParams(), MulticastCollectorView.class);

                updateMulticast.setLoginFindMulticastCollector(null);
                updateMulticast.setLoginPollMulticastCollector(null);

                String updateParams = obj.writeValueAsString(updateMulticast);
                logger.info("Delete Multicast collector params : " + updateParams);
                updateCollector.setParams(updateParams);
            });
        });
        updateNetwork.setCollectors(deleteCollectorSet);
        updateNetwork.setNodeProfileData(allNodeProfileData);
        networkService.updateNetwork(updateNetwork);
    }

    @Test
    @DisplayName("Test Invalid Network Profile throws exception ")
    public void testInvalidNodeProfile() {

        long invalidNodeProfileId = 10L;

        Mockito.doReturn(Optional.of(new NodeProfileData())).when(nodeProfileService).getNodeProfile(networkProfileId);
        Mockito.doReturn(Optional.empty()).when(nodeProfileService).getNodeProfile(invalidNodeProfileId);

        NetworkDataView networkData = new NetworkDataView();
        networkData.setName("NetworkWithNetProfile");

        AllNodeProfileData allNodeProfileData = new AllNodeProfileData();
        allNodeProfileData.setId(invalidNodeProfileId);
        allNodeProfileData.setName("NetworkProfile");

        networkData.setNodeProfileData(allNodeProfileData);

        Assertions.assertThrows(CustomException.class, () -> networkService.saveNetwork(networkData));

        allNodeProfileData.setId(networkProfileId);

        Assertions.assertDoesNotThrow(() -> networkService.saveNetwork(networkData));
    }

    @Test
    public void testNetworkFromJson() throws JsonProcessingException {
        String networkStr = " { " +
                " \"nodeProfileData\": { " +
                " \"updateDate\": null, " +
                " \"id\": 1, " +
                " \"name\": \"NetworkProfile\", " +
                " \"useNodeListAsIncludeFilter\": false " +
                " }, " +
                " \"id\": null, " +
                " \"name\": \"Net_1\", " +
                " \"draft\": false, " +
                " \"draftConfig\": null, " +
                " \"collectors\": [{ " +
                " \"id\": null, " +
                " \"name\": \"TOPO_IGP_add2\", " +
                " \"type\": \"TOPO_IGP\", " +
                " \"consolidationType\": \"DARE\", " +
                " \"params\": \"{\\\"igpConfigs\\\":[{\\\"igpIndex\\\":1,\\\"seedRouter\\\":\\\"x.x.x.x\\\",\\\"igpProtocol\\\":\\\"ISIS\\\",\\\"advanced\\\":{\\\"backupRouter\\\":null,\\\"getSegment\\\":false,\\\"isisLevel\\\":\\\"2\\\",\\\"ospfArea\\\":\\\"0\\\",\\\"ospfProcessIds\\\":[],\\\"isisProcessIds\\\":[],\\\"removeNullProcessId\\\":false,\\\"runIGPOffline\\\":\\\"OFF\\\",\\\"nodeTag\\\":null,\\\"loginConfig\\\":{\\\"forceLoginPlatform\\\":null,\\\"fallbackLoginPlatform\\\":null,\\\"sendEnablePassword\\\":false,\\\"telnetUserName\\\":null,\\\"telnetPassword\\\":null},\\\"timeout\\\":60,\\\"debug\\\":{\\\"netRecorder\\\":\\\"RECORD\\\",\\\"verbosity\\\":60}}}],\\\"collectInterfaces\\\":true,\\\"advanced\\\":{\\\"nodes\\\":{\\\"qosNodeFilterList\\\":null,\\\"performanceData\\\":false,\\\"removeNodeSuffix\\\":[],\\\"discoverQosQueue\\\":true,\\\"timeout\\\":60,\\\"debug\\\":{\\\"netRecorder\\\":\\\"RECORD\\\",\\\"verbosity\\\":60}},\\\"interfaces\\\":{\\\"findParallelLinks\\\":false,\\\"ipGuessing\\\":\\\"SAFE\\\",\\\"discoverLags\\\":false,\\\"lagPortMatch\\\":\\\"GUESS\\\",\\\"circuitCleanup\\\":false,\\\"copyDescription\\\":false,\\\"collectPhysicalPort\\\":false,\\\"minIPGuessPrefixLength\\\":0,\\\"minPrefixLength\\\":30,\\\"timeout\\\":60,\\\"debug\\\":{\\\"netRecorder\\\":\\\"RECORD\\\",\\\"verbosity\\\":60}}}}\", " +
                " \"sourceCollector\": null, " +
                " \"agents\": [] " +
                " }, { " +
                " \"id\": null, " +
                " \"name\": \"TOPO_BGP_add2\", " +
                " \"type\": \"TOPO_BGP\", " +
                " \"consolidationType\": \"DARE\", " +
                " \"params\": \"{\\\"advanced\\\":{\\\"protocol\\\":[\\\"IPV4\\\"],\\\"minPrefixLength\\\":24,\\\"minIPv6PrefixLength\\\":64,\\\"loginToRouterForMultihop\\\":true,\\\"loginConfig\\\":{\\\"forceLoginPlatform\\\":null,\\\"fallbackLoginPlatform\\\":null,\\\"sendEnablePassword\\\":true,\\\"telnetUserName\\\":\\\"\\\",\\\"telnetPassword\\\":null},\\\"findInternalASNLinks\\\":true,\\\"findNonIPExitInterface\\\":true,\\\"findInternalExitInterface\\\":true,\\\"getMacAddress\\\":true,\\\"useDNS\\\":true,\\\"forceCheckAll\\\":true,\\\"timeout\\\":60,\\\"debug\\\":{\\\"verbosity\\\":30,\\\"netRecorder\\\":\\\"OFF\\\",\\\"loginRecordMode\\\":\\\"OFF\\\"}}}\", " +
                " \"sourceCollector\": { " +
                " \"id\": 1, " +
                " \"name\": null, " +
                " \"type\": null " +
                " }, " +
                " \"agents\": [] " +
                " }, { " +
                " \"id\": null, " +
                " \"name\": \"TOPO_VPN_add2\", " +
                " \"type\": \"TOPO_VPN\", " +
                " \"consolidationType\": \"DARE\", " +
                " \"params\": null, " +
                " \"sourceCollector\": { " +
                " \"id\": 1, " +
                " \"name\": null, " +
                " \"type\": null " +
                " }, " +
                " \"agents\": [] " +
                " }] " +
                " }";

        ObjectMapper obj = new ObjectMapper();
        NetworkDataView networkData = obj.readValue(networkStr, NetworkDataView.class);

        Mockito.doReturn(Optional.of(new NodeProfileData())).when(nodeProfileService).getNodeProfile(any());

        NetworkApiResponse networkApiResponse = networkService.saveNetwork(networkData);

        Assertions.assertNotNull(networkApiResponse.getId());
        networkApiResponse.getResponseDetails().forEach(collectorResponse ->
                logger.info("collector {} status {} error details {} ", collectorResponse.getName()
                , collectorResponse.isStatus(), collectorResponse.getErrorDetails()));

        AgentData agentData = new AgentData();
        agentData.setType(AgentTypes.SR_PCE_AGENT);
        agentData.setId(1L);
        agentData.setName("srpce");

        networkStr = " { " +
                " \"nodeProfileData\": { " +
                " \"updateDate\": null, " +
                " \"id\": 1, " +
                " \"name\": \"NetworkProfile\", " +
                " \"useNodeListAsIncludeFilter\": false " +
                " }, " +
                " \"id\": null, " +
                " \"name\": \"Net_2\", " +
                " \"draft\": false, " +
                " \"draftConfig\": null, " +
                " \"collectors\": [{ " +
                " \"id\": null, " +
                " \"name\": \"Traffic Poller Collector_2\", " +
                " \"type\": \"TRAFFIC_POLL\", " +
                " \"consolidationType\": \"SAGE\", " +
                " \"params\": null, " +
                " \"sourceCollector\": { " +
                " \"id\": null, " +
                " \"name\": \"Pcep Collector_2\", " +
                " \"type\": \"LSP_PCEP_XTC\" " +
                " }, " +
                " \"agents\": [] " +
                " }, { " +
                " \"id\": null, " +
                " \"name\": \"Parse Config Collector_2\", " +
                " \"type\": \"CONFIG_PARSE\", " +
                " \"consolidationType\": \"DARE\", " +
                " \"params\": null, " +
                " \"sourceCollector\": { " +
                " \"id\": null, " +
                " \"name\": \"Pcep Collector_2\", " +
                " \"type\": \"LSP_PCEP_XTC\" " +
                " }, " +
                " \"agents\": [] " +
                " }, { " +
                " \"id\": null, " +
                " \"name\": \"BGPLS_NIMO_2\", " +
                " \"type\": \"TOPO_BGPLS_XTC\", " +
                " \"consolidationType\": \"DARE\", " +
                " \"params\": \"{\\\"primarySrPceAgent\\\":1,\\\"secondarySrPceAgent\\\":null,\\\"asn\\\":0,\\\"igpProtocol\\\":\\\"ISIS\\\",\\\"extendedTopologyDiscovery\\\":true,\\\"reactiveEnabled\\\":true,\\\"advanced\\\":{\\\"SR_PCE\\\":{\\\"singleEndedEbgpDiscovery\\\":false},\\\"nodes\\\":{\\\"qosNodeFilterList\\\":null,\\\"performanceData\\\":false,\\\"removeNodeSuffix\\\":[],\\\"discoverQosQueue\\\":true,\\\"timeout\\\":60,\\\"debug\\\":{\\\"netRecorder\\\":\\\"OFF\\\",\\\"verbosity\\\":30}},\\\"interfaces\\\":{\\\"findParallelLinks\\\":false,\\\"ipGuessing\\\":\\\"SAFE\\\",\\\"discoverLags\\\":false,\\\"lagPortMatch\\\":\\\"GUESS\\\",\\\"circuitCleanup\\\":false,\\\"copyDescription\\\":false,\\\"collectPhysicalPort\\\":false,\\\"minIPGuessPrefixLength\\\":0,\\\"minPrefixLength\\\":30,\\\"timeout\\\":60,\\\"debug\\\":{\\\"netRecorder\\\":\\\"OFF\\\",\\\"verbosity\\\":30}}}}\", " +
                " \"sourceCollector\": null, " +
                " \"agents\": [{ " +
                " \"id\": 1, " +
                " \"name\": \"SR_PCE_AGENT\", " +
                " \"type\": \"SR_PCE_AGENT\" " +
                " }] " +
                " }, { " +
                " \"id\": null, " +
                " \"name\": \"Pcep Collector_2\", " +
                " \"type\": \"LSP_PCEP_XTC\", " +
                " \"consolidationType\": \"DARE\", " +
                " \"params\": null, " +
                " \"sourceCollector\": { " +
                " \"id\": 1, " +
                " \"name\": null, " +
                " \"type\": null " +
                " }, " +
                " \"agents\": [{ " +
                " \"id\": 1, " +
                " \"name\": \"SR_PCE_AGENT\", " +
                " \"type\": \"SR_PCE_AGENT\" " +
                " }] " +
                " }] " +
                " }";

        networkData = obj.readValue(networkStr, NetworkDataView.class);
        Mockito.doReturn(Optional.of(agentData)).when(agentService).getAgent(Mockito.anyLong());

        networkApiResponse = networkService.saveNetwork(networkData);

        Assertions.assertNotNull(networkApiResponse.getId());
        networkApiResponse.getResponseDetails().forEach(collectorResponse ->
                logger.info("collector {} status {} error details {} ", collectorResponse.getName()
                , collectorResponse.isStatus(), collectorResponse.getErrorDetails()));

        networkStr = "{ " +
                "\"collectors\": [{ " +
                "\"name\": \"IGP-coll-aug-22-13-10\", " +
                "\"type\": \"TOPO_IGP\", " +
                "\"params\": \"{\\\"igpConfigs\\\":[{\\\"igpIndex\\\":1,\\\"seedRouter\\\":\\\"1.2.3.4\\\",\\\"igpProtocol\\\":\\\"ISIS\\\",\\\"advanced\\\":{\\\"backupRouter\\\":null,\\\"getSegment\\\":false,\\\"isisLevel\\\":\\\"2\\\",\\\"ospfArea\\\":\\\"0\\\",\\\"ospfProcessIds\\\":[],\\\"isisProcessIds\\\":[],\\\"removeNullProcessId\\\":false,\\\"runIGPOffline\\\":\\\"OFF\\\",\\\"nodeTag\\\":null,\\\"loginConfig\\\":{\\\"forceLoginPlatform\\\":null,\\\"fallbackLoginPlatform\\\":null,\\\"sendEnablePassword\\\":false,\\\"telnetUserName\\\":null,\\\"telnetPassword\\\":null},\\\"timeout\\\":60,\\\"debug\\\":{\\\"verbosity\\\":30,\\\"netRecorder\\\":\\\"OFF\\\"}}}],\\\"collectInterfaces\\\":true,\\\"advanced\\\":{\\\"nodes\\\":{\\\"performanceData\\\":false,\\\"removeNodeSuffix\\\":[],\\\"discoverQosQueue\\\":true,\\\"timeout\\\":60,\\\"qosNodeFilterList\\\":[],\\\"debug\\\":{\\\"verbosity\\\":30,\\\"netRecorder\\\":\\\"OFF\\\"}},\\\"interfaces\\\":{\\\"findParallelLinks\\\":false,\\\"ipGuessing\\\":\\\"SAFE\\\",\\\"discoverLags\\\":false,\\\"lagPortMatch\\\":\\\"GUESS\\\",\\\"circuitCleanup\\\":false,\\\"copyDescription\\\":false,\\\"collectPhysicalPort\\\":false,\\\"minIPGuessPrefixLength\\\":0,\\\"minPrefixLength\\\":30,\\\"timeout\\\":60,\\\"debug\\\":{\\\"verbosity\\\":30,\\\"netRecorder\\\":\\\"OFF\\\"}}}}\", " +
                "\"sourceCollector\": null, " +
                "\"agents\": [], " +
                "\"consolidationType\": \"DARE\", " +
                "\"id\": 16 " +
                "}, { " +
                "\"name\": \"Demand-Deduction-coll-aug-22-13-10\", " +
                "\"type\": \"TRAFFIC_DEMAND\", " +
                "\"params\": \"{\\\"demandSteps\\\":[{\\\"name\\\":\\\"n2\\\",\\\"stepNumber\\\":1,\\\"tool\\\":{\\\"copy-demands\\\":{\\\"network\\\":{\\\"id\\\":3}}},\\\"enabled\\\":false},{\\\"name\\\":\\\"n1\\\",\\\"stepNumber\\\":\\\"1\\\",\\\"tool\\\":{\\\"demand-deduction\\\":{\\\"fixDemandsWithTraffic\\\":false,\\\"demandUpperBound\\\":0,\\\"removeZeroBwDemands\\\":true,\\\"zeroBwTolerance\\\":0,\\\"zeroFlowTolerance\\\":0,\\\"measurements\\\":{\\\"nodes\\\":true,\\\"nodesPriority\\\":2,\\\"interfaces\\\":true,\\\"interfacesPriority\\\":1,\\\"lsps\\\":true,\\\"lspsPriority\\\":2,\\\"flows\\\":true,\\\"flowsPriority\\\":2},\\\"advanced\\\":{\\\"demandsTable\\\":\\\"\\\",\\\"fixDemandsTable\\\":\\\"\\\",\\\"fixMulticastDemands\\\":false,\\\"reportFile\\\":\\\"\\\",\\\"trafficLevel\\\":\\\"\\\",\\\"scaleMeasurements\\\":[],\\\"measErrors\\\":\\\"SPREAD\\\",\\\"maxPercentLinkUtil\\\":\\\"\\\",\\\"onlyTunnelAs\\\":\\\"\\\",\\\"computationTime\\\":10,\\\"warnDynamicLsps\\\":false,\\\"warnUnroutedLsps\\\":false,\\\"debug\\\":{\\\"verbosity\\\":30}}}},\\\"enabled\\\":false}]}\", " +
                "\"sourceCollector\": { " +
                "\"type\": \"TOPO_IGP\", " +
                "\"name\": \"IGP-coll-aug-22-13-10\" " +
                "}, " +
                "\"agents\": [], " +
                "\"consolidationType\": \"SAGE\", " +
                "\"id\": 17 " +
                "}], " +
                "\"name\": \"Net_3\", " +
                "\"nodeProfileData\": { " +
                "\"updateDate\": null, " +
                "\"id\": 1, " +
                "\"name\": \"n1\", " +
                "\"useNodeListAsIncludeFilter\": false " +
                "}, " +
                "\"draft\": false, " +
                "\"id\": \"9\" " +
                "}";
        networkData = obj.readValue(networkStr, NetworkDataView.class);

        networkApiResponse = networkService.saveNetwork(networkData);

        Assertions.assertNotNull(networkApiResponse.getId());
        networkApiResponse.getResponseDetails().forEach(collectorResponse ->
                logger.info("collector {} status {} error details {} ", collectorResponse.getName()
                , collectorResponse.isStatus(), collectorResponse.getErrorDetails()));

        networkStr = "{ " +
                "\"collectors\": [{ " +
                "\"name\": \"Net-10\", " +
                "\"type\": \"TOPO_IGP\", " +
                "\"params\": \"{\\\"igpConfigs\\\":[{\\\"igpIndex\\\":1,\\\"seedRouter\\\":\\\"1.2.3.4\\\",\\\"igpProtocol\\\":\\\"ISIS\\\",\\\"advanced\\\":{\\\"backupRouter\\\":null,\\\"getSegment\\\":false,\\\"isisLevel\\\":\\\"2\\\",\\\"ospfArea\\\":\\\"0\\\",\\\"ospfProcessIds\\\":[],\\\"isisProcessIds\\\":[],\\\"removeNullProcessId\\\":false,\\\"runIGPOffline\\\":\\\"OFF\\\",\\\"nodeTag\\\":null,\\\"loginConfig\\\":{\\\"forceLoginPlatform\\\":null,\\\"fallbackLoginPlatform\\\":null,\\\"sendEnablePassword\\\":false,\\\"telnetUserName\\\":null,\\\"telnetPassword\\\":null},\\\"timeout\\\":60,\\\"debug\\\":{\\\"verbosity\\\":30,\\\"netRecorder\\\":\\\"OFF\\\"}}}],\\\"collectInterfaces\\\":true,\\\"advanced\\\":{\\\"nodes\\\":{\\\"performanceData\\\":false,\\\"removeNodeSuffix\\\":[],\\\"discoverQosQueue\\\":true,\\\"timeout\\\":60,\\\"qosNodeFilterList\\\":[],\\\"debug\\\":{\\\"verbosity\\\":30,\\\"netRecorder\\\":\\\"OFF\\\"}},\\\"interfaces\\\":{\\\"findParallelLinks\\\":false,\\\"ipGuessing\\\":\\\"SAFE\\\",\\\"discoverLags\\\":false,\\\"lagPortMatch\\\":\\\"GUESS\\\",\\\"circuitCleanup\\\":false,\\\"copyDescription\\\":false,\\\"collectPhysicalPort\\\":false,\\\"minIPGuessPrefixLength\\\":0,\\\"minPrefixLength\\\":30,\\\"timeout\\\":60,\\\"debug\\\":{\\\"verbosity\\\":30,\\\"netRecorder\\\":\\\"OFF\\\"}}}}\", " +
                "\"sourceCollector\": null, " +
                "\"agents\": [], " +
                "\"consolidationType\": \"DARE\", " +
                "\"id\": 16 " +
                "}, { " +
                "\"name\": \"Multicast-coll-aug-23-13-47\", " +
                "\"type\": \"MULTICAST\", " +
                "\"params\": \"{\\\"loginFindMulticastCollector\\\":{\\\"timeout\\\":30,\\\"forceConfigUpdate\\\":true,\\\"saveConfigs\\\":false,\\\"overwriteFiles\\\":false,\\\"debug\\\":{\\\"verbosity\\\":30,\\\"loginRecordMode\\\":\\\"OFF\\\"}},\\\"loginPollMulticastCollector\\\":{\\\"timeout\\\":30,\\\"noOfSamples\\\":1,\\\"pollingInterval\\\":300,\\\"trafficLevelName\\\":\\\"Default\\\",\\\"trafficFiltering\\\":\\\"max\\\",\\\"directory\\\":null,\\\"saveConfigs\\\":false,\\\"debug\\\":{\\\"verbosity\\\":30,\\\"loginRecordMode\\\":\\\"OFF\\\"}},\\\"snmpFindMulticastCollector\\\":{\\\"timeout\\\":30,\\\"debug\\\":{\\\"verbosity\\\":30,\\\"netRecorder\\\":\\\"OFF\\\"}},\\\"snmpPollMulticastCollector\\\":{\\\"timeout\\\":30,\\\"noOfSamples\\\":1,\\\"pollingInterval\\\":300,\\\"trafficLevelName\\\":\\\"Default\\\",\\\"trafficFiltering\\\":\\\"max\\\",\\\"debug\\\":{\\\"verbosity\\\":30,\\\"netRecorder\\\":\\\"OFF\\\"}}}\", " +
                "\"sourceCollector\": { " +
                "\"type\": \"TOPO_IGP\", " +
                "\"name\": \"Net-10\" " +
                "}, " +
                "\"agents\": [], " +
                "\"consolidationType\": \"DARE\" " +
                "}], " +
                "\"name\": \"coll-aug-28-13-47\", " +
                "\"nodeProfileData\": { " +
                "\"updateDate\": null, " +
                "\"id\": 1, " +
                "\"name\": \"n1\", " +
                "\"useNodeListAsIncludeFilter\": false " +
                "}, " +
                "\"draft\": false, " +
                "\"id\": \"10\" " +
                "}";
        networkData = obj.readValue(networkStr, NetworkDataView.class);

        networkApiResponse = networkService.saveNetwork(networkData);

        Assertions.assertNotNull(networkApiResponse.getId());
        networkApiResponse.getResponseDetails().forEach(collectorResponse ->
                logger.info("collector {} status {} error details {} ", collectorResponse.getName()
                , collectorResponse.isStatus(), collectorResponse.getErrorDetails()));

    }

    @Test
    @DisplayName("Test Invalid Collector.")
    public void testInvalidCollector() {
        Mockito.doReturn(Optional.of(new NodeProfileData())).when(nodeProfileService).getNodeProfile(networkProfileId);

        String networkStr = "{" +
                "  \"collectors\": [" +
                "    {" +
                "      \"name\": \"IGP-copyDemands\", " +
                "      \"type\": \"TOPO_IGP\", " +
                "      \"params\": \"{\\\"igpConfigs\\\":[{\\\"igpIndex\\\":1,\\\"seedRouter\\\":\\\"1.1.1.1\\\",\\\"igpProtocol\\\":\\\"ISIS\\\",\\\"advanced\\\":{\\\"backupRouter\\\":null,\\\"getSegment\\\":false,\\\"isisLevel\\\":\\\"2\\\",\\\"ospfArea\\\":\\\"0\\\",\\\"ospfProcessIds\\\":[],\\\"isisProcessIds\\\":[],\\\"removeNullProcessId\\\":false,\\\"runIGPOffline\\\":\\\"OFF\\\",\\\"nodeTag\\\":null,\\\"loginConfig\\\":{\\\"forceLoginPlatform\\\":null,\\\"fallbackLoginPlatform\\\":null,\\\"sendEnablePassword\\\":false,\\\"telnetUserName\\\":null,\\\"telnetPassword\\\":null},\\\"timeout\\\":60,\\\"debug\\\":{\\\"verbosity\\\":30,\\\"netRecorder\\\":\\\"OFF\\\"}}}],\\\"collectInterfaces\\\":true,\\\"advanced\\\":{\\\"nodes\\\":{\\\"performanceData\\\":false,\\\"removeNodeSuffix\\\":[],\\\"discoverQosQueue\\\":true,\\\"timeout\\\":60,\\\"qosNodeFilterList\\\":[],\\\"debug\\\":{\\\"verbosity\\\":30,\\\"netRecorder\\\":\\\"OFF\\\"}},\\\"interfaces\\\":{\\\"findParallelLinks\\\":false,\\\"ipGuessing\\\":\\\"SAFE\\\",\\\"discoverLags\\\":false,\\\"lagPortMatch\\\":\\\"GUESS\\\",\\\"circuitCleanup\\\":false,\\\"copyDescription\\\":false,\\\"collectPhysicalPort\\\":false,\\\"minIPGuessPrefixLength\\\":0,\\\"minPrefixLength\\\":30,\\\"timeout\\\":60,\\\"debug\\\":{\\\"verbosity\\\":30,\\\"netRecorder\\\":\\\"OFF\\\"}}}}\", " +
                "      \"sourceCollector\": null, " +
                "      \"agents\": [], " +
                "      \"consolidationType\": \"DARE\", " +
                "      \"id\": 33 " +
                "    }, " +
                "    { " +
                "      \"name\": \"Demand-Deduction-copyDemands\", " +
                "      \"type\": \"TRAFFIC_DEMAND\", " +
                "      \"params\": \"{\\\"demandSteps\\\":[{\\\"id\\\":31,\\\"name\\\":\\\"1\\\",\\\"stepNumber\\\":1,\\\"tool\\\":{\\\"dmd-mesh-creator\\\":{\\\"bothDirections\\\":true,\\\"deleteSameName\\\":true,\\\"serviceClass\\\":null,\\\"topology\\\":null,\\\"advanced\\\":{\\\"sourceList\\\":[],\\\"sourceNodes\\\":null,\\\"sourceSites\\\":null,\\\"sourceAs\\\":null,\\\"sourceEndpoints\\\":null,\\\"destSites\\\":null,\\\"destAs\\\":null,\\\"destEndpoints\\\":null,\\\"destList\\\":[],\\\"destination\\\":\\\"\\\",\\\"demandmeshTable\\\":null,\\\"outDemandmeshTable\\\":null,\\\"outDemandsTable\\\":null,\\\"externalAsInterfaceEndpoints\\\":true,\\\"respectAsRelationships\\\":true,\\\"externalMesh\\\":\\\"RESPECT\\\",\\\"setName\\\":null,\\\"setTagList\\\":null,\\\"includeDemandToSelf\\\":true,\\\"debug\\\":{\\\"verbosity\\\":30}}}},\\\"enabled\\\":true},{\\\"id\\\":null,\\\"name\\\":\\\"2\\\",\\\"stepNumber\\\":\\\"2\\\",\\\"tool\\\":{\\\"copy-demands\\\":{\\\"network\\\":{\\\"name\\\":\\\"IGP-copyDemands\\\",\\\"type\\\":\\\"TOPO_IGP\\\"}}},\\\"enabled\\\":true}]}\", " +
                "      \"sourceCollector\": { " +
                "        \"type\": \"TOPO_IGP\", " +
                "        \"name\": \"IGP-copyDemands\" " +
                "      }, " +
                "      \"agents\": [], " +
                "      \"consolidationType\": \"SAGE\", " +
                "      \"id\": 32 " +
                "    } " +
                "  ], " +
                "  \"name\": \"copyDemands\", " +
                "  \"nodeProfileData\": { " +
                "    \"id\": 1, " +
                "    \"name\": \"n1\", " +
                "    \"useNodeListAsIncludeFilter\": false " +
                "  }, " +
                "  \"draft\": false, " +
                "  \"id\": \"11\" " +
                "}";

        ObjectMapper obj = new ObjectMapper();

        Assertions.assertDoesNotThrow(() -> {
            NetworkDataView networkData = obj.readValue(networkStr, NetworkDataView.class);

            NetworkApiResponse networkApiResponse = networkService.saveNetwork(networkData);

            Assertions.assertNotNull(networkApiResponse.getId());
            AtomicInteger invalidCollector = new AtomicInteger();
            networkApiResponse.getResponseDetails().forEach(collectorResponse -> {
                    logger.info("collector {} status {} error details {} ", collectorResponse.getName()
                    , collectorResponse.isStatus(), collectorResponse.getErrorDetails());
                    if(collectorResponse.isStatus()) invalidCollector.getAndIncrement();
            });
            Assertions.assertEquals(1, invalidCollector.get());
        });
    }

    @Test
    @DisplayName("Test Draft Network")
    public void testDraftNetwork() throws JsonProcessingException {

        String networkStr = "{ " +
                " \"nodeProfileData\": { " +
                "  \"updateDate\": null, " +
                "  \"id\": 1, " +
                "  \"name\": \"network-profile-lab\", " +
                "  \"useNodeListAsIncludeFilter\": false " +
                " }, " +
                " \"name\": \"draft-network\", " +
                " \"draft\": true, " +
                " \"draftConfig\": \"[{\\\"name\\\":\\\"IGP-sirpi-test\\\",\\\"type\\\":\\\"TOPO_IGP\\\",\\\"consolidationType\\\":\\\"DARE\\\",\\\"params\\\":\\\"{\\\\\\\"igpConfigs\\\\\\\":[{\\\\\\\"igpIndex\\\\\\\":1,\\\\\\\"seedRouter\\\\\\\":\\\\\\\"10.225.120.63\\\\\\\",\\\\\\\"igpProtocol\\\\\\\":\\\\\\\"ISIS\\\\\\\",\\\\\\\"advanced\\\\\\\":{\\\\\\\"backupRouter\\\\\\\":null,\\\\\\\"getSegment\\\\\\\":false,\\\\\\\"isisLevel\\\\\\\":\\\\\\\"2\\\\\\\",\\\\\\\"ospfArea\\\\\\\":\\\\\\\"0\\\\\\\",\\\\\\\"ospfProcessIds\\\\\\\":[],\\\\\\\"isisProcessIds\\\\\\\":[],\\\\\\\"removeNullProcessId\\\\\\\":false,\\\\\\\"runIGPOffline\\\\\\\":\\\\\\\"OFF\\\\\\\",\\\\\\\"nodeTag\\\\\\\":null,\\\\\\\"loginConfig\\\\\\\":{\\\\\\\"forceLoginPlatform\\\\\\\":null,\\\\\\\"fallbackLoginPlatform\\\\\\\":null,\\\\\\\"sendEnablePassword\\\\\\\":false,\\\\\\\"telnetUserName\\\\\\\":null,\\\\\\\"telnetPassword\\\\\\\":null},\\\\\\\"timeout\\\\\\\":60,\\\\\\\"verbosity\\\\\\\":30,\\\\\\\"loginRecordMode\\\\\\\":\\\\\\\"OFF\\\\\\\"}}],\\\\\\\"collectInterfaces\\\\\\\":true,\\\\\\\"advanced\\\\\\\":{\\\\\\\"nodes\\\\\\\":{\\\\\\\"qosNodeFilterList\\\\\\\":[],\\\\\\\"performanceData\\\\\\\":false,\\\\\\\"removeNodeSuffix\\\\\\\":[],\\\\\\\"discoverQosQueue\\\\\\\":true,\\\\\\\"timeout\\\\\\\":60,\\\\\\\"verbosity\\\\\\\":30,\\\\\\\"netRecorder\\\\\\\":\\\\\\\"OFF\\\\\\\"},\\\\\\\"interfaces\\\\\\\":{\\\\\\\"findParallelLinks\\\\\\\":false,\\\\\\\"ipGuessing\\\\\\\":\\\\\\\"SAFE\\\\\\\",\\\\\\\"discoverLags\\\\\\\":false,\\\\\\\"lagPortMatch\\\\\\\":\\\\\\\"GUESS\\\\\\\",\\\\\\\"circuitCleanup\\\\\\\":false,\\\\\\\"copyDescription\\\\\\\":false,\\\\\\\"collectPhysicalPort\\\\\\\":false,\\\\\\\"minIPGuessPrefixLength\\\\\\\":0,\\\\\\\"minPrefixLength\\\\\\\":30,\\\\\\\"timeout\\\\\\\":60,\\\\\\\"verbosity\\\\\\\":30,\\\\\\\"netRecorder\\\\\\\":\\\\\\\"OFF\\\\\\\"}}}\\\",\\\"sourceCollector\\\":null}]\", " +
                " \"collectors\": null " +
                "}";

        ObjectMapper obj = new ObjectMapper();
        NetworkDataView networkData = obj.readValue(networkStr, NetworkDataView.class);

        Mockito.doReturn(Optional.of(new NodeProfileData())).when(nodeProfileService).getNodeProfile(any());

        NetworkApiResponse networkApiResponse = networkService.saveNetwork(networkData);

        Assertions.assertNotNull(networkApiResponse.getId());

        networkStr = "{ " +
                " \"nodeProfileData\": { " +
                "  \"updateDate\": null, " +
                "  \"id\": 1, " +
                "  \"name\": \"network-profile-lab\", " +
                "  \"useNodeListAsIncludeFilter\": false " +
                " }, " +
                "  \"id\": " + networkApiResponse.getId() + ", " +
                " \"name\": \"draft-network\", " +
                " \"draft\": true, " +
                " \"draftConfig\": \"[{\\\"name\\\":\\\"IGP-sirpi-test\\\",\\\"type\\\":\\\"TOPO_IGP\\\",\\\"consolidationType\\\":\\\"DARE\\\",\\\"params\\\":\\\"{\\\\\\\"igpConfigs\\\\\\\":[{\\\\\\\"igpIndex\\\\\\\":1,\\\\\\\"seedRouter\\\\\\\":\\\\\\\"10.225.120.63\\\\\\\",\\\\\\\"igpProtocol\\\\\\\":\\\\\\\"ISIS\\\\\\\",\\\\\\\"advanced\\\\\\\":{\\\\\\\"backupRouter\\\\\\\":null,\\\\\\\"getSegment\\\\\\\":false,\\\\\\\"isisLevel\\\\\\\":\\\\\\\"2\\\\\\\",\\\\\\\"ospfArea\\\\\\\":\\\\\\\"0\\\\\\\",\\\\\\\"ospfProcessIds\\\\\\\":[],\\\\\\\"isisProcessIds\\\\\\\":[],\\\\\\\"removeNullProcessId\\\\\\\":false,\\\\\\\"runIGPOffline\\\\\\\":\\\\\\\"OFF\\\\\\\",\\\\\\\"nodeTag\\\\\\\":null,\\\\\\\"loginConfig\\\\\\\":{\\\\\\\"forceLoginPlatform\\\\\\\":null,\\\\\\\"fallbackLoginPlatform\\\\\\\":null,\\\\\\\"sendEnablePassword\\\\\\\":false,\\\\\\\"telnetUserName\\\\\\\":null,\\\\\\\"telnetPassword\\\\\\\":null},\\\\\\\"timeout\\\\\\\":60,\\\\\\\"verbosity\\\\\\\":30,\\\\\\\"loginRecordMode\\\\\\\":\\\\\\\"OFF\\\\\\\"}}],\\\\\\\"collectInterfaces\\\\\\\":true,\\\\\\\"advanced\\\\\\\":{\\\\\\\"nodes\\\\\\\":{\\\\\\\"qosNodeFilterList\\\\\\\":[],\\\\\\\"performanceData\\\\\\\":false,\\\\\\\"removeNodeSuffix\\\\\\\":[],\\\\\\\"discoverQosQueue\\\\\\\":true,\\\\\\\"timeout\\\\\\\":60,\\\\\\\"verbosity\\\\\\\":30,\\\\\\\"netRecorder\\\\\\\":\\\\\\\"OFF\\\\\\\"},\\\\\\\"interfaces\\\\\\\":{\\\\\\\"findParallelLinks\\\\\\\":false,\\\\\\\"ipGuessing\\\\\\\":\\\\\\\"SAFE\\\\\\\",\\\\\\\"discoverLags\\\\\\\":false,\\\\\\\"lagPortMatch\\\\\\\":\\\\\\\"GUESS\\\\\\\",\\\\\\\"circuitCleanup\\\\\\\":false,\\\\\\\"copyDescription\\\\\\\":false,\\\\\\\"collectPhysicalPort\\\\\\\":false,\\\\\\\"minIPGuessPrefixLength\\\\\\\":0,\\\\\\\"minPrefixLength\\\\\\\":30,\\\\\\\"timeout\\\\\\\":60,\\\\\\\"verbosity\\\\\\\":30,\\\\\\\"netRecorder\\\\\\\":\\\\\\\"OFF\\\\\\\"}}}\\\",\\\"sourceCollector\\\":null}]\", " +
                " \"collectors\": null " +
                "}";

        networkData = obj.readValue(networkStr, NetworkDataView.class);

        networkApiResponse = networkService.updateNetwork(networkData);

        Assertions.assertNotNull(networkApiResponse.getId());

        networkStr = "{ " +
                " \"nodeProfileData\": { " +
                "  \"updateDate\": null, " +
                "  \"id\": 1, " +
                "  \"name\": \"network-profile-lab\", " +
                "  \"useNodeListAsIncludeFilter\": false " +
                " }, " +
                "  \"id\": " + networkApiResponse.getId() + ", " +
                " \"name\": \"draft-network\", " +
                " \"draft\": false, " +
                " \"draftConfig\": \"[{\\\"name\\\":\\\"IGP-sirpi-test\\\",\\\"type\\\":\\\"TOPO_IGP\\\",\\\"consolidationType\\\":\\\"DARE\\\",\\\"params\\\":\\\"{\\\\\\\"igpConfigs\\\\\\\":[{\\\\\\\"igpIndex\\\\\\\":1,\\\\\\\"seedRouter\\\\\\\":\\\\\\\"10.225.120.63\\\\\\\",\\\\\\\"igpProtocol\\\\\\\":\\\\\\\"ISIS\\\\\\\",\\\\\\\"advanced\\\\\\\":{\\\\\\\"backupRouter\\\\\\\":null,\\\\\\\"getSegment\\\\\\\":false,\\\\\\\"isisLevel\\\\\\\":\\\\\\\"2\\\\\\\",\\\\\\\"ospfArea\\\\\\\":\\\\\\\"0\\\\\\\",\\\\\\\"ospfProcessIds\\\\\\\":[],\\\\\\\"isisProcessIds\\\\\\\":[],\\\\\\\"removeNullProcessId\\\\\\\":false,\\\\\\\"runIGPOffline\\\\\\\":\\\\\\\"OFF\\\\\\\",\\\\\\\"nodeTag\\\\\\\":null,\\\\\\\"loginConfig\\\\\\\":{\\\\\\\"forceLoginPlatform\\\\\\\":null,\\\\\\\"fallbackLoginPlatform\\\\\\\":null,\\\\\\\"sendEnablePassword\\\\\\\":false,\\\\\\\"telnetUserName\\\\\\\":null,\\\\\\\"telnetPassword\\\\\\\":null},\\\\\\\"timeout\\\\\\\":60,\\\\\\\"verbosity\\\\\\\":30,\\\\\\\"loginRecordMode\\\\\\\":\\\\\\\"OFF\\\\\\\"}}],\\\\\\\"collectInterfaces\\\\\\\":true,\\\\\\\"advanced\\\\\\\":{\\\\\\\"nodes\\\\\\\":{\\\\\\\"qosNodeFilterList\\\\\\\":[],\\\\\\\"performanceData\\\\\\\":false,\\\\\\\"removeNodeSuffix\\\\\\\":[],\\\\\\\"discoverQosQueue\\\\\\\":true,\\\\\\\"timeout\\\\\\\":60,\\\\\\\"verbosity\\\\\\\":30,\\\\\\\"netRecorder\\\\\\\":\\\\\\\"OFF\\\\\\\"},\\\\\\\"interfaces\\\\\\\":{\\\\\\\"findParallelLinks\\\\\\\":false,\\\\\\\"ipGuessing\\\\\\\":\\\\\\\"SAFE\\\\\\\",\\\\\\\"discoverLags\\\\\\\":false,\\\\\\\"lagPortMatch\\\\\\\":\\\\\\\"GUESS\\\\\\\",\\\\\\\"circuitCleanup\\\\\\\":false,\\\\\\\"copyDescription\\\\\\\":false,\\\\\\\"collectPhysicalPort\\\\\\\":false,\\\\\\\"minIPGuessPrefixLength\\\\\\\":0,\\\\\\\"minPrefixLength\\\\\\\":30,\\\\\\\"timeout\\\\\\\":60,\\\\\\\"verbosity\\\\\\\":30,\\\\\\\"netRecorder\\\\\\\":\\\\\\\"OFF\\\\\\\"}}}\\\",\\\"sourceCollector\\\":null}]\", " +
                " \"collectors\": null " +
                "}";

        networkData = obj.readValue(networkStr, NetworkDataView.class);

        networkApiResponse = networkService.updateNetwork(networkData);

        Assertions.assertNotNull(networkApiResponse.getId());

        String errorNetworkStr = "{ " +
                " \"nodeProfileData\": { " +
                "  \"updateDate\": null, " +
                "  \"id\": 1, " +
                "  \"name\": \"network-profile-lab\", " +
                "  \"useNodeListAsIncludeFilter\": false " +
                " }, " +
                "  \"id\": " + networkApiResponse.getId() + ", " +
                " \"name\": \"draft-network\", " +
                " \"draft\": true, " +
                " \"draftConfig\": \"[{\\\"name\\\":\\\"IGP-sirpi-test\\\",\\\"type\\\":\\\"TOPO_IGP\\\",\\\"consolidationType\\\":\\\"DARE\\\",\\\"params\\\":\\\"{\\\\\\\"igpConfigs\\\\\\\":[{\\\\\\\"igpIndex\\\\\\\":1,\\\\\\\"seedRouter\\\\\\\":\\\\\\\"10.225.120.63\\\\\\\",\\\\\\\"igpProtocol\\\\\\\":\\\\\\\"ISIS\\\\\\\",\\\\\\\"advanced\\\\\\\":{\\\\\\\"backupRouter\\\\\\\":null,\\\\\\\"getSegment\\\\\\\":false,\\\\\\\"isisLevel\\\\\\\":\\\\\\\"2\\\\\\\",\\\\\\\"ospfArea\\\\\\\":\\\\\\\"0\\\\\\\",\\\\\\\"ospfProcessIds\\\\\\\":[],\\\\\\\"isisProcessIds\\\\\\\":[],\\\\\\\"removeNullProcessId\\\\\\\":false,\\\\\\\"runIGPOffline\\\\\\\":\\\\\\\"OFF\\\\\\\",\\\\\\\"nodeTag\\\\\\\":null,\\\\\\\"loginConfig\\\\\\\":{\\\\\\\"forceLoginPlatform\\\\\\\":null,\\\\\\\"fallbackLoginPlatform\\\\\\\":null,\\\\\\\"sendEnablePassword\\\\\\\":false,\\\\\\\"telnetUserName\\\\\\\":null,\\\\\\\"telnetPassword\\\\\\\":null},\\\\\\\"timeout\\\\\\\":60,\\\\\\\"verbosity\\\\\\\":30,\\\\\\\"loginRecordMode\\\\\\\":\\\\\\\"OFF\\\\\\\"}}],\\\\\\\"collectInterfaces\\\\\\\":true,\\\\\\\"advanced\\\\\\\":{\\\\\\\"nodes\\\\\\\":{\\\\\\\"qosNodeFilterList\\\\\\\":[],\\\\\\\"performanceData\\\\\\\":false,\\\\\\\"removeNodeSuffix\\\\\\\":[],\\\\\\\"discoverQosQueue\\\\\\\":true,\\\\\\\"timeout\\\\\\\":60,\\\\\\\"verbosity\\\\\\\":30,\\\\\\\"netRecorder\\\\\\\":\\\\\\\"OFF\\\\\\\"},\\\\\\\"interfaces\\\\\\\":{\\\\\\\"findParallelLinks\\\\\\\":false,\\\\\\\"ipGuessing\\\\\\\":\\\\\\\"SAFE\\\\\\\",\\\\\\\"discoverLags\\\\\\\":false,\\\\\\\"lagPortMatch\\\\\\\":\\\\\\\"GUESS\\\\\\\",\\\\\\\"circuitCleanup\\\\\\\":false,\\\\\\\"copyDescription\\\\\\\":false,\\\\\\\"collectPhysicalPort\\\\\\\":false,\\\\\\\"minIPGuessPrefixLength\\\\\\\":0,\\\\\\\"minPrefixLength\\\\\\\":30,\\\\\\\"timeout\\\\\\\":60,\\\\\\\"verbosity\\\\\\\":30,\\\\\\\"netRecorder\\\\\\\":\\\\\\\"OFF\\\\\\\"}}}\\\",\\\"sourceCollector\\\":null}]\", " +
                " \"collectors\": null " +
                "}";

        Assertions.assertThrows(CustomException.class, () -> networkService.updateNetwork(obj.readValue(errorNetworkStr, NetworkDataView.class)));
    }

    @Test
    @DisplayName("Test Dare as source Collector.")
    public void testDareSrcCollector() {
        Mockito.doReturn(Optional.of(new NodeProfileData())).when(nodeProfileService).getNodeProfile(networkProfileId);

        String networkStr = "{" +
                "  \"collectors\": [" +
                "    {" +
                "      \"name\": \"Topo-Igp-DemandWithDareSource\", " +
                "      \"type\": \"TOPO_IGP\", " +
                "      \"params\": \"{\\\"igpConfigs\\\":[{\\\"igpIndex\\\":1,\\\"seedRouter\\\":\\\"1.1.1.1\\\",\\\"igpProtocol\\\":\\\"ISIS\\\",\\\"advanced\\\":{\\\"backupRouter\\\":null,\\\"getSegment\\\":false,\\\"isisLevel\\\":\\\"2\\\",\\\"ospfArea\\\":\\\"0\\\",\\\"ospfProcessIds\\\":[],\\\"isisProcessIds\\\":[],\\\"removeNullProcessId\\\":false,\\\"runIGPOffline\\\":\\\"OFF\\\",\\\"nodeTag\\\":null,\\\"loginConfig\\\":{\\\"forceLoginPlatform\\\":null,\\\"fallbackLoginPlatform\\\":null,\\\"sendEnablePassword\\\":false,\\\"telnetUserName\\\":null,\\\"telnetPassword\\\":null},\\\"timeout\\\":60,\\\"debug\\\":{\\\"verbosity\\\":30,\\\"netRecorder\\\":\\\"OFF\\\"}}}],\\\"collectInterfaces\\\":true,\\\"advanced\\\":{\\\"nodes\\\":{\\\"performanceData\\\":false,\\\"removeNodeSuffix\\\":[],\\\"discoverQosQueue\\\":true,\\\"timeout\\\":60,\\\"qosNodeFilterList\\\":[],\\\"debug\\\":{\\\"verbosity\\\":30,\\\"netRecorder\\\":\\\"OFF\\\"}},\\\"interfaces\\\":{\\\"findParallelLinks\\\":false,\\\"ipGuessing\\\":\\\"SAFE\\\",\\\"discoverLags\\\":false,\\\"lagPortMatch\\\":\\\"GUESS\\\",\\\"circuitCleanup\\\":false,\\\"copyDescription\\\":false,\\\"collectPhysicalPort\\\":false,\\\"minIPGuessPrefixLength\\\":0,\\\"minPrefixLength\\\":30,\\\"timeout\\\":60,\\\"debug\\\":{\\\"verbosity\\\":30,\\\"netRecorder\\\":\\\"OFF\\\"}}}}\", " +
                "      \"sourceCollector\": null, " +
                "      \"agents\": [], " +
                "      \"consolidationType\": \"DARE\", " +
                "      \"id\": 33 " +
                "    }, " +
                "    { " +
                "      \"name\": \"Demand-Deduction-DemandWithDareSource\", " +
                "      \"type\": \"TRAFFIC_DEMAND\", " +
                "      \"params\": \"{\\\"demandSteps\\\":[{\\\"id\\\":31,\\\"name\\\":\\\"1\\\",\\\"stepNumber\\\":1,\\\"tool\\\":{\\\"dmd-mesh-creator\\\":{\\\"bothDirections\\\":true,\\\"deleteSameName\\\":true,\\\"serviceClass\\\":null,\\\"topology\\\":null,\\\"advanced\\\":{\\\"sourceList\\\":[],\\\"sourceNodes\\\":null,\\\"sourceSites\\\":null,\\\"sourceAs\\\":null,\\\"sourceEndpoints\\\":null,\\\"destSites\\\":null,\\\"destAs\\\":null,\\\"destEndpoints\\\":null,\\\"destList\\\":[],\\\"destination\\\":\\\"\\\",\\\"demandmeshTable\\\":null,\\\"outDemandmeshTable\\\":null,\\\"outDemandsTable\\\":null,\\\"externalAsInterfaceEndpoints\\\":true,\\\"respectAsRelationships\\\":true,\\\"externalMesh\\\":\\\"RESPECT\\\",\\\"setName\\\":null,\\\"setTagList\\\":null,\\\"includeDemandToSelf\\\":true,\\\"debug\\\":{\\\"verbosity\\\":30}}}},\\\"enabled\\\":true}]}\", " +
                "      \"sourceCollector\": { " +
                "        \"type\": \"DARE\" " +
                "      }, " +
                "      \"agents\": [], " +
                "      \"consolidationType\": \"SAGE\", " +
                "      \"id\": 32 " +
                "    } " +
                "  ], " +
                "  \"name\": \"DemandWithDareSource\", " +
                "  \"nodeProfileData\": { " +
                "    \"id\": 1, " +
                "    \"name\": \"n1\", " +
                "    \"useNodeListAsIncludeFilter\": false " +
                "  }, " +
                "  \"draft\": false, " +
                "  \"id\": \"11\" " +
                "}";

        ObjectMapper obj = new ObjectMapper();

        Assertions.assertDoesNotThrow(() -> {
            NetworkDataView networkData = obj.readValue(networkStr, NetworkDataView.class);

            NetworkApiResponse networkApiResponse = networkService.saveNetwork(networkData);

            Assertions.assertNotNull(networkApiResponse.getId());
            AtomicReference<AtomicInteger> validCollector = new AtomicReference<>(new AtomicInteger());
            networkApiResponse.getResponseDetails().forEach(collectorResponse -> {
                logger.info("collector {} status {} error details {} ", collectorResponse.getName()
                        , collectorResponse.isStatus(), collectorResponse.getErrorDetails());
                if(collectorResponse.isStatus()) validCollector.get().getAndIncrement();
            });
            Assertions.assertEquals(2, validCollector.get().get());

            Optional<NetworkDataView> readNetworkOptional = networkService.getNetwork(networkData.getId());
            Assertions.assertTrue(readNetworkOptional.isPresent());
            NetworkDataView readNetwork = readNetworkOptional.get();
            Assertions.assertNotNull(readNetwork.getId());
            logger.info("Read network " + readNetwork);
            Set<CollectorDataView> updateCollectorSet = readNetwork.getCollectors();
            AtomicReference<AtomicInteger> dareSrcCollector = new AtomicReference<>(new AtomicInteger());
            updateCollectorSet.forEach(readCollector -> {
                if(null != readCollector.getSourceCollector()) {
                    logger.info("Collector source " + readCollector.getSourceCollector());
                    dareSrcCollector.get().getAndIncrement();
                }
            });
            Assertions.assertEquals(1, dareSrcCollector.get().get());

        });


    }

}

