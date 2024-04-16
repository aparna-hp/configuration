package com.cisco.configService.service;

import com.cisco.configService.enums.AgentTypes;
import com.cisco.configService.enums.CollectorTypes;
import com.cisco.configService.enums.IgpProtocol;
import com.cisco.configService.enums.RecordMode;
import com.cisco.configService.migration.CollectorMigrationService;
import com.cisco.configService.migration.wae7xConfig.Network;
import com.cisco.configService.migration.wae7xConfig.Nimo;
import com.cisco.configService.migration.wae7xConfig.agents.CfgParse;
import com.cisco.configService.migration.wae7xConfig.nimos.*;
import com.cisco.configService.model.AllConfigurations;
import com.cisco.configService.model.common.Interfaces;
import com.cisco.configService.model.common.ui.InterfacesView;
import com.cisco.configService.model.common.ui.NodesView;
import com.cisco.configService.model.composer.CollectorDataView;
import com.cisco.configService.model.composer.NetworkDataView;
import com.cisco.configService.model.demand.DmdsForLsps;
import com.cisco.configService.model.demand.ui.*;
import com.cisco.configService.model.inventory.ui.InventoryCollectorView;
import com.cisco.configService.model.multicast.ui.MulticastCollectorView;
import com.cisco.configService.model.netflow.CommonConfigs;
import com.cisco.configService.model.netflow.DemandConfigs;
import com.cisco.configService.model.netflow.IASConfigs;
import com.cisco.configService.model.netflow.NetflowCollector;
import com.cisco.configService.model.parseConfig.ParseConfigAdvanced;
import com.cisco.configService.model.parseConfig.ui.ParseConfigCollectorView;
import com.cisco.configService.model.pcepLsp.Advanced;
import com.cisco.configService.model.pcepLsp.PcepLspCollector;
import com.cisco.configService.model.pcepLsp.ReactiveNetwork;
import com.cisco.configService.model.preConfig.AgentData;
import com.cisco.configService.model.preConfig.NodeFilterData;
import com.cisco.configService.model.preConfig.NodeProfileData;
import com.cisco.configService.model.topoBgpls.SrpceAdvanced;
import com.cisco.configService.model.topoBgpls.ui.BgpLsCollectorAdvancedView;
import com.cisco.configService.model.topoBgpls.ui.BgpLsCollectorView;
import com.cisco.configService.model.trafficPoller.ui.TrafficCollectorView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@SpringBootTest
public class CollectorMigrationServiceTest {

    @Autowired
    CollectorMigrationService collectorMigrationService;

    private static final Logger logger = LogManager.getLogger(CollectorMigrationServiceTest.class);

    @Test
    public void testIgpMigrate() {
        final String NODE_FILTER_NAME = "filter";
        final String NODE_PROFILE_NAME = "net-access";

        Network network = new Network();
        TopoIgpNimo topoIgpNimo = new TopoIgpNimo();
        topoIgpNimo.setNetworkAccess(NODE_PROFILE_NAME);
        topoIgpNimo.setCollectInterfaces(true);
        topoIgpNimo.setNodeFilter(NODE_FILTER_NAME);

        IgpConfig igpConfig = new IgpConfig();
        igpConfig.setIgpIndex(1);
        igpConfig.setIgpProtocol("isis");
        igpConfig.setSeedRouter("1.1.2.2");

        IgpConfig igpConfig2 = new IgpConfig();
        igpConfig2.setIgpIndex(2);
        igpConfig2.setIgpProtocol("isis");
        igpConfig2.setSeedRouter("2.2.2.2");

        topoIgpNimo.setIgpConfig(List.of(igpConfig, igpConfig2));

        Nimo nimo = new Nimo();
        nimo.setTopoIgpNimo(topoIgpNimo);
        network.setNimo(nimo);

        CollectorDataView collectorDataView = new CollectorDataView();
        collectorDataView.setName("igp");
        NetworkDataView networkDataView = new NetworkDataView();
        List<NodeProfileData> nodeProfileDataList = new ArrayList<>();
        NodeProfileData nodeProfileData = new NodeProfileData();
        nodeProfileData.setName(NODE_PROFILE_NAME);
        nodeProfileData.setId(1L);
        nodeProfileDataList.add(nodeProfileData);

        NodeFilterData nodeFilterData = new NodeFilterData();
        nodeFilterData.setType(NodeFilterData.Type.IP_REGEX);
        nodeFilterData.setCondition(NodeFilterData.Condition.EXCLUDE);
        nodeFilterData.setValue("1.*.*.*");

        AllConfigurations configurations = new AllConfigurations();
        configurations.setNodeProfileDataList(nodeProfileDataList);

        collectorMigrationService.migrateTopoIgp(network,collectorDataView,networkDataView,configurations,
                Map.of(NODE_FILTER_NAME, List.of(nodeFilterData)));
        Assertions.assertNotNull(networkDataView.getNodeProfileData());
        Assertions.assertEquals(NODE_PROFILE_NAME, networkDataView.getNodeProfileData().getName());
        Assertions.assertEquals(1, networkDataView.getCollectors().size());
        Assertions.assertEquals(1, configurations.getNodeProfileDataList().size());
        Assertions.assertEquals(1, configurations.getNodeProfileDataList().get(0).getNodeFilters().size());
    }

    @Test
    public void testBgpMigrate() {
        Network network = new Network();
        TopoBgpNimo topoBgpNimo = new TopoBgpNimo();
        topoBgpNimo.setSourceNetwork("igp");

        Nimo nimo = new Nimo();
        nimo.setTopoBgpNimo(topoBgpNimo);
        network.setNimo(nimo);

        CollectorDataView collectorDataView = new CollectorDataView();
        collectorDataView.setName("bgp");
        NetworkDataView networkDataView = new NetworkDataView();

        collectorMigrationService.migrateTopoBgp(network,collectorDataView,networkDataView, List.of());
        Assertions.assertEquals(1, networkDataView.getCollectors().size());
    }

    @Test
    public void testLspMigrate() {
        Network network = new Network();
        LspSnmpNimo lspSnmpNimo = new LspSnmpNimo();
        lspSnmpNimo.setSourceNetwork("igp");

        Nimo nimo = new Nimo();
        nimo.setLspSnmpNimo(lspSnmpNimo);
        network.setNimo(nimo);

        CollectorDataView collectorDataView = new CollectorDataView();
        collectorDataView.setName("lsp");
        NetworkDataView networkDataView = new NetworkDataView();

        collectorMigrationService.migrateLspSnmp(network,collectorDataView,networkDataView,List.of());
        Assertions.assertEquals(1, networkDataView.getCollectors().size());
    }

    @Test
    public void testVpnMigrate() {
        Network network = new Network();
        TopoVpnNimo topoVpnNimo = new TopoVpnNimo();
        topoVpnNimo.setSourceNetwork("igp");

        Nimo nimo = new Nimo();
        nimo.setTopoVpnNimo(topoVpnNimo);
        network.setNimo(nimo);

        CollectorDataView collectorDataView = new CollectorDataView();
        collectorDataView.setName("vpn");
        NetworkDataView networkDataView = new NetworkDataView();

        collectorMigrationService.migrateTopoVpn(network,collectorDataView,networkDataView, List.of());
        Assertions.assertEquals(1, networkDataView.getCollectors().size());
    }

    @Test
    public void testInvMigrate() {
        Network network = new Network();
        InventoryNimo inventoryNimo = new InventoryNimo();
        inventoryNimo.setSourceNetwork("dare");
        InventoryNimo.InventoryAdvanced advanced = new InventoryNimo.InventoryAdvanced();
        advanced.setActionTimeout(55);

        InventoryNimo.GetInventory getInventory = new InventoryNimo.GetInventory();
        getInventory.setLoginAllowed(true);
        getInventory.setNetRecorder(RecordMode.RECORD.name());
        getInventory.setVerbosity(40);
        advanced.setGetInventoryOptions(getInventory);

        InventoryNimo.BuildInventory buildInventory = new InventoryNimo.BuildInventory();
        buildInventory.setVerbosity(35);
        buildInventory.setExcludeFile("exclude");
        buildInventory.setTemplateFile("template");
        buildInventory.setGuessTemplateIfNoMatch(true);
        buildInventory.setHardwareSpecFile("hw");
        advanced.setBuildInventoryOptions(buildInventory);
        inventoryNimo.setInventoryAdvanced(advanced);

        Nimo nimo = new Nimo();
        nimo.setInventoryNimo(inventoryNimo);
        network.setNimo(nimo);

        CollectorDataView collectorDataView = new CollectorDataView();
        collectorDataView.setName("inv");
        NetworkDataView networkDataView = new NetworkDataView();

        collectorMigrationService.migrateInventory(network,collectorDataView,networkDataView, List.of("dare"));
        Assertions.assertEquals(1, networkDataView.getCollectors().size());

        networkDataView.getCollectors().forEach(collectorView -> {
            Assertions.assertEquals(collectorView.getSourceCollector().getType(), CollectorTypes.DARE);
            ObjectMapper objectMapper = new ObjectMapper();
            String params = collectorView.getParams();
            try {
                InventoryCollectorView inventoryCollectorView = objectMapper.readValue(params, InventoryCollectorView.class);
                Assertions.assertEquals(inventoryCollectorView.getAdvanced().getActionTimeout(), 55);

                Assertions.assertTrue(inventoryCollectorView.getAdvanced().getGetInventoryOptions().getLoginAllowed());
                Assertions.assertEquals(inventoryCollectorView.getAdvanced().getGetInventoryOptions().getDebug().getVerbosity(), 40);

                Assertions.assertEquals(inventoryCollectorView.getAdvanced().getBuildInventoryOptions().getDebug().getVerbosity(), 35);
                Assertions.assertTrue(inventoryCollectorView.getAdvanced().getBuildInventoryOptions().getGuessTemplateIfNoMatch());
                Assertions.assertEquals(inventoryCollectorView.getAdvanced().getBuildInventoryOptions().getExcludeFile(), "exclude");
                Assertions.assertEquals(inventoryCollectorView.getAdvanced().getBuildInventoryOptions().getTemplateFile(), "template");
                Assertions.assertEquals(inventoryCollectorView.getAdvanced().getBuildInventoryOptions().getHardwareSpecFile(), "hw");

            } catch (JsonProcessingException e) {
                logger.error("Error generating the traffic collector view.",e);
            }
        });
    }

    @Test
    public void testTpMigrate() {
        Network network = new Network();
        TrafficPollNimo trafficPollNimo = new TrafficPollNimo();
        trafficPollNimo.setSourceNetwork("dare");
        trafficPollNimo.setEnabled(true);

        TrafficPollNimo.InterfaceTraffic interfaceTraffic = new TrafficPollNimo.InterfaceTraffic();
        interfaceTraffic.setEnabled(true);
        interfaceTraffic.setQosEnabled(true);
        interfaceTraffic.setVpnEnabled(true);
        interfaceTraffic.setPeriod(50);
        trafficPollNimo.setInterfaceTraffic(interfaceTraffic);

        TrafficPollNimo.LspTraffic lspTraffic = new TrafficPollNimo.LspTraffic();
        lspTraffic.setEnabled(true);
        lspTraffic.setPeriod(50);
        trafficPollNimo.setLspTraffic(lspTraffic);

        TrafficPollNimo.MacTraffic macTraffic = new TrafficPollNimo.MacTraffic();
        macTraffic.setEnabled(true);
        macTraffic.setPeriod(50);
        trafficPollNimo.setMacTraffic(macTraffic);

        TrafficPollNimo.SnmpTrafficPoller snmpTrafficPoller = new TrafficPollNimo.SnmpTrafficPoller();
        snmpTrafficPoller.setMaxWindowLengthStats(555);
        snmpTrafficPoller.setMinWindowLengthStats(100);
        snmpTrafficPoller.setNetRecorder(RecordMode.RECORD.name());
        TrafficPollNimo.TrafficAdvanced trafficAdvanced = new TrafficPollNimo.TrafficAdvanced();
        trafficAdvanced.setSnmpTrafficPoller(snmpTrafficPoller);
        trafficPollNimo.setTrafficAdvanced(trafficAdvanced);

        Nimo nimo = new Nimo();
        nimo.setTrafficPollNimo(trafficPollNimo);
        network.setNimo(nimo);

        CollectorDataView collectorDataView = new CollectorDataView();
        collectorDataView.setName("tp");
        NetworkDataView networkDataView = new NetworkDataView();

        collectorMigrationService.migrateTP(network,collectorDataView,networkDataView, List.of("dare"));
        Assertions.assertEquals(1, networkDataView.getCollectors().size());

        networkDataView.getCollectors().forEach(collectorView -> {
            Assertions.assertEquals(collectorView.getSourceCollector().getType(), CollectorTypes.DARE);
            ObjectMapper objectMapper = new ObjectMapper();
            String params = collectorView.getParams();
            try {
                TrafficCollectorView trafficCollectorView = objectMapper.readValue(params, TrafficCollectorView.class);
                Assertions.assertTrue(trafficCollectorView.getEnabled());

                Assertions.assertTrue(trafficCollectorView.getInterfaceTraffic().getEnabled());
                Assertions.assertEquals(trafficCollectorView.getInterfaceTraffic().getPeriod(), 50);

                Assertions.assertTrue(trafficCollectorView.getLspTraffic().getEnabled());
                Assertions.assertEquals(trafficCollectorView.getLspTraffic().getPeriod(), 50);

                Assertions.assertTrue(trafficCollectorView.getMacTraffic().getEnabled());
                Assertions.assertEquals(trafficCollectorView.getMacTraffic().getPeriod(), 50);

                Assertions.assertEquals(trafficCollectorView.getSnmpTrafficPoller().getMaxWindowLengthStats(), 555);
                Assertions.assertEquals(trafficCollectorView.getSnmpTrafficPoller().getMinWindowLengthStats(), 100);


            } catch (JsonProcessingException e) {
                logger.error("Error generating the traffic collector view.",e);
            }
        });
    }

    @Test
    public void testDemandMeshMigrate() {
        Network network = new Network();
        TrafficDemandsNimo trafficDemandsNimo = new TrafficDemandsNimo();
        trafficDemandsNimo.setSourceNetwork("dare");

        TrafficDemandsNimo.DemandMeshConfig demandMeshConfig = new TrafficDemandsNimo.DemandMeshConfig();

        DemandStepView demandStepView = new DemandStepView();
        demandStepView.setName("Mesh");
        demandStepView.setStepNumber(1);

        DmdMeshCreatorView dmdMeshCreator = new DmdMeshCreatorView();
        dmdMeshCreator.setBothDirections(false);
        dmdMeshCreator.setServiceClass("class");
        dmdMeshCreator.setDeleteSameName(false);
        dmdMeshCreator.setEnabled(true);

        DemandMeshAdvancedView demandMeshAdvancedView = new DemandMeshAdvancedView();
        demandMeshAdvancedView.setDemandmeshTable("table");
        demandMeshAdvancedView.setDestAs("1");
        demandMeshAdvancedView.setDestEndpoints("dest_end_point");
        demandMeshAdvancedView.setDestEqualSource(true);
        demandMeshAdvancedView.setDestNodes("dest_nodes");
        demandMeshAdvancedView.setDestSites("dest_sites");
        demandMeshAdvancedView.setDestList(List.of("123"));
        demandMeshAdvancedView.setVerbosity(60);
        dmdMeshCreator.setDemandMeshAdvancedView(demandMeshAdvancedView);

        DemandToolView demandToolView = new DemandToolView();
        demandToolView.setDmdMeshCreator(dmdMeshCreator);
        demandStepView.setTool(demandToolView);

        demandMeshConfig.getDemandMeshSteps().add(demandStepView);

        trafficDemandsNimo.setDemandMeshConfig(demandMeshConfig);

        Nimo nimo = new Nimo();
        nimo.setTrafficDemandsNimo(trafficDemandsNimo);
        network.setNimo(nimo);

        CollectorDataView collectorDataView = new CollectorDataView();
        collectorDataView.setName("dare");
        NetworkDataView networkDataView = new NetworkDataView();

        collectorMigrationService.migrateDemand(network,collectorDataView,networkDataView, List.of("dare"));
        Assertions.assertEquals(1, networkDataView.getCollectors().size());

        networkDataView.getCollectors().forEach(collectorView -> {
            Assertions.assertEquals(collectorView.getSourceCollector().getType(), CollectorTypes.DARE);
            ObjectMapper objectMapper = new ObjectMapper();
            String params = collectorView.getParams();
            try {
                DemandCollectorView demandCollectorView = objectMapper.readValue(params, DemandCollectorView.class);
                Assertions.assertEquals(demandCollectorView.getDemandSteps().size(), 1);
                DemandStepView demandStep = demandCollectorView.getDemandSteps().get(0);
                    Assertions.assertTrue(demandStep.getEnabled());
                    Assertions.assertEquals(demandStep.getStepNumber(),1);

                    DemandToolView tool = demandStep.getTool();
                    DmdMeshCreatorView meshCreator = tool.getDmdMeshCreator();

                    Assertions.assertEquals(meshCreator.getServiceClass(), "class");
                    Assertions.assertFalse(meshCreator.getBothDirections());
                    Assertions.assertFalse(meshCreator.getDeleteSameName());

                    DemandMeshAdvancedView meshAdvanced = meshCreator.getDemandMeshAdvancedView();

                    Assertions.assertEquals(meshAdvanced.getDemandmeshTable(), "table");
                    Assertions.assertEquals(meshAdvanced.getDebug().getVerbosity(), 60);
                    Assertions.assertEquals(meshAdvanced.getDestAs(),"1");
                    Assertions.assertEquals(meshAdvanced.getDestEndpoints(), "dest_end_point");
                    Assertions.assertEquals(meshAdvanced.getDestNodes(), "dest_nodes");
                    Assertions.assertEquals(meshAdvanced.getDestSites(), "dest_sites");



            } catch (JsonProcessingException e) {
                logger.error("Error generating the traffic collector view.",e);
            }
        });
    }

    @Test
    public void testDmdForLspMigrate() {
        Network network = new Network();
        TrafficDemandsNimo trafficDemandsNimo = new TrafficDemandsNimo();
        trafficDemandsNimo.setSourceNetwork("dare");

        TrafficDemandsNimo.DemandMeshConfig demandMeshConfig = new TrafficDemandsNimo.DemandMeshConfig();

        DemandStepView demandStepView = new DemandStepView();
        demandStepView.setName("DmdForLsp");
        demandStepView.setStepNumber(2);

        DmdsForLspsView dmdsForLspsView = new DmdsForLspsView();
        dmdsForLspsView.setDemandTraffic(DmdsForLsps.LspDemandTraffic.TRAFFIC);
        dmdsForLspsView.setEnabled(true);
        dmdsForLspsView.setServiceClass("class");

        DemandForLspAdvancedView dmdForLspAdvView = new DemandForLspAdvancedView();
        dmdForLspAdvView.setVerbosity(60);
        dmdForLspAdvView.setLspsTable("table");
        dmdForLspAdvView.setNoGlobalOptions(true);

        dmdsForLspsView.setDemandForLspAdvancedView(dmdForLspAdvView);

        DemandToolView demandToolView = new DemandToolView();
        demandToolView.setDmdsForLsps(dmdsForLspsView);
        demandStepView.setTool(demandToolView);

        demandMeshConfig.getDemandMeshSteps().add(demandStepView);

        trafficDemandsNimo.setDemandMeshConfig(demandMeshConfig);

        Nimo nimo = new Nimo();
        nimo.setTrafficDemandsNimo(trafficDemandsNimo);
        network.setNimo(nimo);

        CollectorDataView collectorDataView = new CollectorDataView();
        collectorDataView.setName("dare");
        NetworkDataView networkDataView = new NetworkDataView();

        collectorMigrationService.migrateDemand(network,collectorDataView,networkDataView, List.of("dare"));
        Assertions.assertEquals(1, networkDataView.getCollectors().size());

        networkDataView.getCollectors().forEach(collectorView -> {
            Assertions.assertEquals(collectorView.getSourceCollector().getType(), CollectorTypes.DARE);
            ObjectMapper objectMapper = new ObjectMapper();
            String params = collectorView.getParams();
            try {
                DemandCollectorView demandCollectorView = objectMapper.readValue(params, DemandCollectorView.class);
                Assertions.assertEquals(demandCollectorView.getDemandSteps().size(), 1);
                DemandStepView demandStep = demandCollectorView.getDemandSteps().get(0);
                Assertions.assertTrue(demandStep.getEnabled());
                Assertions.assertEquals(demandStep.getStepNumber(),2);

                DemandToolView tool = demandStep.getTool();
                DmdsForLspsView dmdsForLsps = tool.getDmdsForLsps();

                Assertions.assertEquals(dmdsForLsps.getServiceClass(), "class");
                Assertions.assertEquals(dmdsForLsps.getDemandTraffic(), DmdsForLsps.LspDemandTraffic.TRAFFIC);

                DemandForLspAdvancedView meshAdvanced = dmdsForLsps.getDemandForLspAdvancedView();

                Assertions.assertEquals(meshAdvanced.getLspsTable(), "table");
                Assertions.assertEquals(meshAdvanced.getDebug().getVerbosity(), 60);
                Assertions.assertTrue(meshAdvanced.getNoGlobalOptions());

            } catch (JsonProcessingException e) {
                logger.error("Error generating the traffic collector view.",e);
            }
        });
    }

    @Test
    public void testBgplsMigrate() {
        Network network = new Network();
        BgpLsCollectorView bgpLsCollectorView = new BgpLsCollectorView();
        bgpLsCollectorView.setNetworkAccess("net-access");
        bgpLsCollectorView.setXtcHost("srpce");
        bgpLsCollectorView.setAsn(1);

        BgpLsCollectorView.ReactiveNetworkBgpLs reactiveNetwork = new BgpLsCollectorView.ReactiveNetworkBgpLs();
        reactiveNetwork.setEnable(false);

        bgpLsCollectorView.setReactiveNetwork(reactiveNetwork);
        bgpLsCollectorView.setIgpProtocolStr(IgpProtocol.ISIS.name().toLowerCase(Locale.ROOT));

        BgpLsCollectorAdvancedView bgpLsCollectorAdvanced = new BgpLsCollectorAdvancedView();
        SrpceAdvanced srpceAdvanced = new SrpceAdvanced();
        srpceAdvanced.setSingleEndedEbgpDiscovery(true);

        bgpLsCollectorAdvanced.setXtc(srpceAdvanced);

        NodesView nodesView = new NodesView();
        nodesView.setVerbosity(65);
        nodesView.setNetRecorder("record");
        nodesView.setPerformanceData(true);

        bgpLsCollectorAdvanced.setNodes(nodesView);

        InterfacesView interfacesView = new InterfacesView();
        interfacesView.setIpGuessStr("full");
        interfacesView.setLagPortMatchStr("exact");
        interfacesView.setVerbosity(35);
        interfacesView.setNetRecorder("record");
        interfacesView.setTimeout(45);
        interfacesView.setCircuitCleanup(true);

        bgpLsCollectorAdvanced.setInterfaces(interfacesView);
        bgpLsCollectorView.setAdvanced(bgpLsCollectorAdvanced);

        Nimo nimo = new Nimo();
        nimo.setTopoBgpLsXtcNimo(bgpLsCollectorView);
        network.setNimo(nimo);

        CollectorDataView collectorDataView = new CollectorDataView();
        collectorDataView.setName("bgpls");
        NetworkDataView networkDataView = new NetworkDataView();

        NodeProfileData nodeProfileData = new NodeProfileData();
        nodeProfileData.setName("net-access");

        AgentData agentData = new AgentData();
        agentData.setName("srpce");
        agentData.setType(AgentTypes.SR_PCE_AGENT);

        AllConfigurations allConfigurations = new AllConfigurations();
        allConfigurations.setNodeProfileDataList(List.of(nodeProfileData));
        allConfigurations.setAgentDataList(List.of(agentData));

        collectorMigrationService.migrateBgpLs(network, collectorDataView, networkDataView, allConfigurations, Map.of());
        Assertions.assertEquals("net-access", networkDataView.getNodeProfileData().getName());
        Assertions.assertEquals(1, networkDataView.getCollectors().size());

        networkDataView.getCollectors().forEach(collector -> {
            String params = collector.getParams();
            ObjectMapper objectMapper = new ObjectMapper();


            Assertions.assertDoesNotThrow(() -> {
                BgpLsCollectorView bgplsCollector = objectMapper.readValue(params, BgpLsCollectorView.class);
                Assertions.assertEquals(IgpProtocol.ISIS, bgplsCollector.getIgpProtocol());
                Assertions.assertFalse(bgplsCollector.getReactiveEnabled());
                Assertions.assertEquals(1, bgplsCollector.getAsn());
                Assertions.assertTrue(bgplsCollector.getAdvanced().getXtc().getSingleEndedEbgpDiscovery());
                Assertions.assertTrue(bgplsCollector.getAdvanced().getNodes().getPerformanceData());
                Assertions.assertEquals(RecordMode.RECORD, bgplsCollector.getAdvanced().getNodes().getDebug().getNetRecorder());
                Assertions.assertEquals(65, bgplsCollector.getAdvanced().getNodes().getDebug().getVerbosity());

                Assertions.assertEquals(RecordMode.RECORD, bgplsCollector.getAdvanced().getInterfaces().getDebug().getNetRecorder());
                Assertions.assertEquals(Interfaces.IpGuessing.FULL, bgplsCollector.getAdvanced().getInterfaces().getIpGuessing());
                Assertions.assertEquals(Interfaces.LagPortMatch.EXACT, bgplsCollector.getAdvanced().getInterfaces().getLagPortMatch());
                Assertions.assertEquals(35, bgplsCollector.getAdvanced().getInterfaces().getDebug().getVerbosity());
                Assertions.assertTrue(bgplsCollector.getAdvanced().getInterfaces().getCircuitCleanup());
                Assertions.assertEquals(45, bgplsCollector.getAdvanced().getInterfaces().getTimeout());
            });
        });
    }

    @Test
    public void testPcepLspMigrate() {
        Network network = new Network();
        PcepLspCollector pcepLspCollector = new PcepLspCollector();
        pcepLspCollector.setSourceNetwork("bgpls");
        PcepLspCollector.SrpceAgents agent = new PcepLspCollector.SrpceAgents();
        agent.setSrpceAgent("srpce");
        pcepLspCollector.setSrpceAgentsList(List.of(agent));

        ReactiveNetwork reactiveNetwork = new ReactiveNetwork();
        reactiveNetwork.setEnable(true);

        pcepLspCollector.setReactiveNetwork(reactiveNetwork);

        Advanced pcepAdvanced = new Advanced();
        pcepAdvanced.setSrUseSignaledName(false);
        pcepAdvanced.setConnectTimeout(45);

        pcepLspCollector.setAdvanced(pcepAdvanced);

        Nimo nimo = new Nimo();
        nimo.setPcepLspCollector(pcepLspCollector);
        network.setNimo(nimo);

        CollectorDataView collectorDataView = new CollectorDataView();
        collectorDataView.setName("pcep");
        NetworkDataView networkDataView = new NetworkDataView();

        AgentData agentData = new AgentData();
        agentData.setName("srpce");
        agentData.setType(AgentTypes.SR_PCE_AGENT);

        AllConfigurations allConfigurations = new AllConfigurations();
        allConfigurations.setAgentDataList(List.of(agentData));

        collectorMigrationService.migratePcepLsp(network, collectorDataView, networkDataView, List.of("dare"),allConfigurations);
        Assertions.assertEquals(1, networkDataView.getCollectors().size());

        networkDataView.getCollectors().forEach(collector -> {
            Assertions.assertEquals(1, collector.getAgents().size());
            String params = collector.getParams();
            ObjectMapper objectMapper = new ObjectMapper();


            Assertions.assertDoesNotThrow(() -> {
                PcepLspCollector lspCollector = objectMapper.readValue(params, PcepLspCollector.class);
                Assertions.assertTrue(lspCollector.getReactiveNetwork().getEnable());
                Assertions.assertFalse(lspCollector.getAdvanced().getSrUseSignaledName());
                Assertions.assertEquals(45, lspCollector.getAdvanced().getConnectTimeout());
            });
        });
    }

    @Test
    public void testPcMigrate() {
        Network network = new Network();
        CfgParseNimo cfgParseNimo = new CfgParseNimo();
        cfgParseNimo.setSourceNetwork("bgpls");
        cfgParseNimo.setCfgParseAgent("get-config-agent");

        CfgParseNimo.ParseConfig parseConfig = new CfgParseNimo.ParseConfig();
        parseConfig.setAsn(0);
        parseConfig.setIgpProtocol("isis");
        parseConfig.setIsisLevel("1");
        parseConfig.setIncludeObjects(List.of("lag", "srlg"));

        CfgParseNimo.ParseConfig.ParseConfigAdv parseConfigAdv = new CfgParseNimo.ParseConfig.ParseConfigAdv();
        parseConfigAdv.setResolveReferences(false);
        parseConfigAdv.setBuildTopology(false);
        parseConfigAdv.setVerbosity(45);
        parseConfigAdv.setTimeout(50);
        parseConfigAdv.setCircuitMatch(ParseConfigAdvanced.CircuitMatch.ALL.name());
        parseConfigAdv.setLagPortMatch(Interfaces.LagPortMatch.EXACT.name());
        parseConfigAdv.setIsisInstanceIds("1,2,3");
        parseConfigAdv.setOspfProcessIds("1,2,3");

        parseConfig.setParseConfigAdvanced(parseConfigAdv);
        cfgParseNimo.setParseConfig(parseConfig);
        logger.debug("Migrating Parse config " + cfgParseNimo);

        Nimo nimo = new Nimo();
        nimo.setCfgParseNimo(cfgParseNimo);
        network.setNimo(nimo);

        CollectorDataView collectorDataView = new CollectorDataView();
        collectorDataView.setName("pc");
        NetworkDataView networkDataView = new NetworkDataView();

        AllConfigurations allConfigurations = new AllConfigurations();
        ParseConfigCollectorView parseConfigCollectorView = new ParseConfigCollectorView();


        collectorMigrationService.migratePc(network, collectorDataView, networkDataView, List.of("dare"), Map.of("get-config-agent",parseConfigCollectorView));
        Assertions.assertEquals(1, networkDataView.getCollectors().size());

        networkDataView.getCollectors().forEach(collector -> {
            String params = collector.getParams();
            ObjectMapper objectMapper = new ObjectMapper();

            Assertions.assertDoesNotThrow(() -> {
                ParseConfigCollectorView configCollectorView = objectMapper.readValue(params, ParseConfigCollectorView.class);
                Assertions.assertEquals(IgpProtocol.ISIS, configCollectorView.getParseConfig().getIgpProtocol());
                Assertions.assertEquals("1", configCollectorView.getParseConfig().getIsisLevel());
                Assertions.assertEquals(2, configCollectorView.getParseConfig().getIncludeObjects().size());

                Assertions.assertFalse(configCollectorView.getParseConfig().getParseConfigAdvanced().getBuildTopology());
                Assertions.assertFalse(configCollectorView.getParseConfig().getParseConfigAdvanced().getResolveReferences());
                Assertions.assertEquals(45, configCollectorView.getParseConfig().getParseConfigAdvanced().getDebug().getVerbosity());
                Assertions.assertEquals(50, configCollectorView.getParseConfig().getParseConfigAdvanced().getTimeout());
                Assertions.assertEquals(ParseConfigAdvanced.CircuitMatch.ALL, configCollectorView.getParseConfig().getParseConfigAdvanced().getCircuitMatch());
                Assertions.assertEquals(Interfaces.LagPortMatch.EXACT, configCollectorView.getParseConfig().getParseConfigAdvanced().getLagPortMatch());
                Assertions.assertEquals(3, configCollectorView.getParseConfig().getParseConfigAdvanced().getIsisProcessIds().size());
                Assertions.assertEquals(3, configCollectorView.getParseConfig().getParseConfigAdvanced().getOspfProcessIds().size());
            });
        });
    }

    @Test
    public void testMcMigrate() {
        Network loginFindNw = new Network();
        loginFindNw.setName("LoginFind");
        LoginFindMulticastNimo loginFindMulticastNimo = new LoginFindMulticastNimo();
        loginFindMulticastNimo.setSourceNetwork("bgpls");
        loginFindMulticastNimo.getAdvanced().setTimeout(40);
        loginFindNw.getNimo().setLoginFindMulticastNimo(loginFindMulticastNimo);

        Network loginPollNw = new Network();
        loginFindNw.setName("LoginPoll");
        LoginPollMulticastNimo loginPollMulticastNimo = new LoginPollMulticastNimo();
        loginPollMulticastNimo.setSourceNetwork("LoginFind");
        loginPollMulticastNimo.getAdvanced().setTimeout(40);
        loginPollNw.getNimo().setLoginPollMulticastNimo(loginPollMulticastNimo);

        Network snmpFindNw = new Network();
        snmpFindNw.setName("LoginPoll");
        SnmpFindMulticastNimo snmpFindMulticastNimo = new SnmpFindMulticastNimo();
        snmpFindMulticastNimo.setSourceNetwork("LoginPoll");
        snmpFindMulticastNimo.getAdvanced().setTimeout(40);
        snmpFindNw.getNimo().setSnmpFindMulticastNimo(snmpFindMulticastNimo);

        Network snmpPollNw = new Network();
        snmpPollNw.setName("LoginPoll");
        SnmpPollMulticastNimo snmpPollMulticastNimo = new SnmpPollMulticastNimo();
        snmpPollMulticastNimo.setSourceNetwork("LoginPoll");
        snmpPollMulticastNimo.getAdvanced().setTimeout(40);
        snmpPollNw.getNimo().setSnmpPollMulticastNimo(snmpPollMulticastNimo);

        NetworkDataView networkDataView = new NetworkDataView();

        collectorMigrationService.migrateMulticast(snmpFindNw, snmpPollNw, loginFindNw, loginPollNw,  networkDataView,
                List.of("dare"), Map.of(), Map.of("bgpls",CollectorTypes.TOPO_BGPLS_XTC));
        Assertions.assertEquals(1, networkDataView.getCollectors().size());

        networkDataView.getCollectors().forEach(collector -> {
            Assertions.assertNotNull(collector.getSourceCollector());
            Assertions.assertEquals("bgpls", collector.getSourceCollector().getName());

            String params = collector.getParams();
            ObjectMapper objectMapper = new ObjectMapper();

            Assertions.assertDoesNotThrow(() -> {
                MulticastCollectorView multicastCollectorView = objectMapper.readValue(params, MulticastCollectorView.class);
                Assertions.assertNotNull(multicastCollectorView.getLoginFindMulticastCollector());
                Assertions.assertNotNull(multicastCollectorView.getLoginPollMulticastCollector());
                Assertions.assertNotNull(multicastCollectorView.getSnmpFindMulticastCollector());
                Assertions.assertNotNull(multicastCollectorView.getSnmpPollMulticastCollector());

                Assertions.assertEquals(40, multicastCollectorView.getLoginFindMulticastCollector().getTimeout());
                Assertions.assertEquals(40, multicastCollectorView.getLoginPollMulticastCollector().getTimeout());
                Assertions.assertEquals(40, multicastCollectorView.getSnmpFindMulticastCollector().getTimeout());
                Assertions.assertEquals(40, multicastCollectorView.getSnmpPollMulticastCollector().getTimeout());

            });
        });
    }

    @Test
    public void testMcWithSnmpFindSourceMigrate() {
        Network loginFindNw = new Network();
        loginFindNw.setName("LoginFind");
        LoginFindMulticastNimo loginFindMulticastNimo = new LoginFindMulticastNimo();
        loginFindMulticastNimo.setSourceNetwork("SnmpFind");
        loginFindMulticastNimo.getAdvanced().setLoginRecordMode("record");
        loginFindNw.getNimo().setLoginFindMulticastNimo(loginFindMulticastNimo);

        Network loginPollNw = new Network();
        loginFindNw.setName("LoginPoll");
        LoginPollMulticastNimo loginPollMulticastNimo = new LoginPollMulticastNimo();
        loginPollMulticastNimo.setSourceNetwork("SnmpPoll");
        loginPollMulticastNimo.getAdvanced().setLoginRecordMode("record");
        loginPollNw.getNimo().setLoginPollMulticastNimo(loginPollMulticastNimo);

        Network snmpFindNw = new Network();
        snmpFindNw.setName("LoginPoll");
        SnmpFindMulticastNimo snmpFindMulticastNimo = new SnmpFindMulticastNimo();
        snmpFindMulticastNimo.setSourceNetwork("dare");
        snmpFindMulticastNimo.getAdvanced().setRecordMode("record");
        snmpFindNw.getNimo().setSnmpFindMulticastNimo(snmpFindMulticastNimo);

        Network snmpPollNw = new Network();
        snmpPollNw.setName("LoginPoll");
        SnmpPollMulticastNimo snmpPollMulticastNimo = new SnmpPollMulticastNimo();
        snmpPollMulticastNimo.setSourceNetwork("SnmpFind");
        snmpPollMulticastNimo.getAdvanced().setRecordMode("record");
        snmpPollNw.getNimo().setSnmpPollMulticastNimo(snmpPollMulticastNimo);

        NetworkDataView networkDataView = new NetworkDataView();

        collectorMigrationService.migrateMulticast(snmpFindNw, snmpPollNw,loginFindNw, loginPollNw,  networkDataView,
                List.of("dare"), Map.of(), Map.of("bgpls",CollectorTypes.TOPO_BGPLS_XTC));
        Assertions.assertEquals(1, networkDataView.getCollectors().size());

        networkDataView.getCollectors().forEach(collector -> {
            Assertions.assertNotNull(collector.getSourceCollector());
            Assertions.assertEquals(CollectorTypes.DARE, collector.getSourceCollector().getType());

            String params = collector.getParams();
            ObjectMapper objectMapper = new ObjectMapper();

            Assertions.assertDoesNotThrow(() -> {
                MulticastCollectorView multicastCollectorView = objectMapper.readValue(params, MulticastCollectorView.class);
                Assertions.assertNotNull(multicastCollectorView.getLoginFindMulticastCollector());
                Assertions.assertNotNull(multicastCollectorView.getLoginPollMulticastCollector());
                Assertions.assertNotNull(multicastCollectorView.getSnmpFindMulticastCollector());
                Assertions.assertNotNull(multicastCollectorView.getSnmpPollMulticastCollector());

                Assertions.assertEquals(RecordMode.RECORD, multicastCollectorView.getLoginFindMulticastCollector().getDebug().getNetRecorder());
                Assertions.assertEquals(RecordMode.RECORD, multicastCollectorView.getLoginPollMulticastCollector().getDebug().getNetRecorder());
                Assertions.assertEquals(RecordMode.RECORD, multicastCollectorView.getSnmpFindMulticastCollector().getDebug().getNetRecorder());
                Assertions.assertEquals(RecordMode.RECORD, multicastCollectorView.getSnmpPollMulticastCollector().getDebug().getNetRecorder());

            });
        });
    }

    @Test
    public void testNetflowMigrate() {
        Network network = new Network();
        NetflowNimo netflowNimo = new NetflowNimo();
        netflowNimo.setSourceNetwork("tp");

        CommonConfigs commonConfigs = new CommonConfigs();
        commonConfigs.setAsn("1");
        commonConfigs.setSplitAsFlowsOnEgress(true);

        IASConfigs iasConfigs = new IASConfigs();
        iasConfigs.setBacktrackMicroFlows(true);

        DemandConfigs demandConfigs = new DemandConfigs();
        demandConfigs.setDemandName("dmd");

        NetflowNimo.NetflowConfig netflowConfig = new NetflowNimo.NetflowConfig();
        netflowConfig.setCommon(commonConfigs);
        netflowConfig.setIasFlows(iasConfigs);
        netflowConfig.setDemands(demandConfigs);

        netflowNimo.setConfig(netflowConfig);
        Nimo nimo = new Nimo();
        nimo.setNetflowNimo(netflowNimo);

        network.setNimo(nimo);

        CollectorDataView collectorDataView = new CollectorDataView();
        collectorDataView.setName("netflow");
        NetworkDataView networkDataView = new NetworkDataView();

        AllConfigurations allConfigurations = new AllConfigurations();

        collectorMigrationService.migrateNetflow(network, collectorDataView, networkDataView, List.of(), allConfigurations);
        Assertions.assertEquals(1, networkDataView.getCollectors().size());

        networkDataView.getCollectors().forEach(collector -> {
            Assertions.assertEquals(1, collector.getAgents().size());

            String params = collector.getParams();
            ObjectMapper objectMapper = new ObjectMapper();

            Assertions.assertDoesNotThrow(() -> {
                NetflowCollector netflowCollector = objectMapper.readValue(params, NetflowCollector.class);
                Assertions.assertEquals("1", netflowCollector.getCommonConfigs().getAsn());
                Assertions.assertTrue(netflowCollector.getIASConfigs().getBacktrackMicroFlows());
                Assertions.assertEquals("dmd", netflowCollector.getDemandConfigs().getDemandName());
            });
        });
    }
}
