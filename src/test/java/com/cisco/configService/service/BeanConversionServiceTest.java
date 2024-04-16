package com.cisco.configService.service;

import com.cisco.configService.entity.Collector;
import com.cisco.configService.enums.CollectorTypes;
import com.cisco.configService.enums.IgpProtocol;
import com.cisco.configService.enums.RecordMode;
import com.cisco.configService.exception.CustomException;
import com.cisco.configService.model.common.LoginConfig;
import com.cisco.configService.model.composer.cli.CollectorData;
import com.cisco.configService.model.custom.CustomCollector;
import com.cisco.configService.model.demand.DemandDeduction;
import com.cisco.configService.model.demand.DmdMeshCreator;
import com.cisco.configService.model.demand.DmdsForLsps;
import com.cisco.configService.model.demand.DmdsForP2mplsps;
import com.cisco.configService.model.demand.ui.DemandDeductionView;
import com.cisco.configService.model.demand.ui.DmdMeshCreatorView;
import com.cisco.configService.model.demand.ui.DmdsForLspsView;
import com.cisco.configService.model.demand.ui.DmdsForP2mplspsView;
import com.cisco.configService.model.inventory.InventoryCollector;
import com.cisco.configService.model.inventory.ui.InventoryCollectorView;
import com.cisco.configService.model.layout.LayoutCollector;
import com.cisco.configService.model.layout.LayoutCollectorView;
import com.cisco.configService.model.lspSnmp.LspSnmpCollector;
import com.cisco.configService.model.lspSnmp.ui.LspSnmpCollectorView;
import com.cisco.configService.model.multicast.LoginFindMulticastCollector;
import com.cisco.configService.model.multicast.LoginPollMulticastCollector;
import com.cisco.configService.model.multicast.SnmpFindMulticastCollector;
import com.cisco.configService.model.multicast.SnmpPollMulticastCollector;
import com.cisco.configService.model.multicast.ui.*;
import com.cisco.configService.model.parseConfig.ParseConfig;
import com.cisco.configService.model.parseConfig.ParseConfigAdvanced;
import com.cisco.configService.model.parseConfig.ParseConfigCollector;
import com.cisco.configService.model.parseConfig.ui.GetConfigView;
import com.cisco.configService.model.parseConfig.ui.ParseConfigAdvancedView;
import com.cisco.configService.model.parseConfig.ui.ParseConfigCollectorView;
import com.cisco.configService.model.parseConfig.ui.ParseConfigView;
import com.cisco.configService.model.topoBgp.BgpCollector;
import com.cisco.configService.model.topoBgp.ui.BgpCollectorView;
import com.cisco.configService.model.topoBgpls.BgpLsCollector;
import com.cisco.configService.model.topoBgpls.ui.BgpLsCollectorView;
import com.cisco.configService.model.topoVpn.VpnCollector;
import com.cisco.configService.model.topoVpn.ui.VpnCollectorView;
import com.cisco.configService.model.trafficPoller.TrafficCollector;
import com.cisco.configService.model.trafficPoller.ui.TrafficCollectorView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class BeanConversionServiceTest {

    @Autowired
    BeanConverstionService beanConverstionService;

    private static final Logger logger = LogManager.getLogger(BeanConversionServiceTest.class);

    @Test
    public void testIgpParamConvert() {
        Collector collectorEntity = new Collector();
        collectorEntity.setName("igp");
        collectorEntity.setType(CollectorTypes.TOPO_IGP);
        collectorEntity.setParams("{\"igpConfigs\":[{\"igpIndex\":1,\"seedRouter\":\"x.x.x.x\",\"igpProtocol\":\"ISIS\",\"advanced\":{\"backupRouter\":null,\"getSegment\":true,\"isisLevel\":\"2\",\"ospfArea\":\"0\",\"ospfProcessIds\":[],\"isisProcessIds\":[],\"removeNullProcessId\":true,\"runIGPOffline\":\"OFF\",\"nodeTag\":null,\"loginConfig\":{\"forceLoginPlatform\":null,\"fallbackLoginPlatform\":null,\"sendEnablePassword\":false,\"telnetUserName\":null,\"telnetPassword\":null},\"timeout\":60,\"debug\":{\"netRecorder\":\"RECORD\",\"verbosity\":60}}}],\"collectInterfaces\":true,\"advanced\":{\"nodes\":{\"qosNodeFilterList\":null,\"performanceData\":false,\"removeNodeSuffix\":[],\"discoverQosQueue\":true,\"timeout\":60,\"debug\":{\"netRecorder\":\"RECORD\",\"verbosity\":60}},\"interfaces\":{\"findParallelLinks\":false,\"ipGuessing\":\"SAFE\",\"discoverLags\":false,\"lagPortMatch\":\"GUESS\",\"circuitCleanup\":false,\"copyDescription\":false,\"collectPhysicalPort\":false,\"minIPGuessPrefixLength\":0,\"minPrefixLength\":30,\"timeout\":60,\"debug\":{\"netRecorder\":\"RECORD\",\"verbosity\":60}}}}");

        CollectorData collectorData = beanConverstionService.populateToolParameters(collectorEntity);
        logger.info("Tool Collector params " + collectorData);
        Assertions.assertNotNull(collectorData.getName());
        Assertions.assertEquals(collectorData.getType(), CollectorTypes.TOPO_IGP);
        Assertions.assertTrue(collectorData.getParams().length() > 0);
    }

    @Test
    public void testBgpParamConvert() throws JsonProcessingException {
        Collector collectorEntity = new Collector();
        collectorEntity.setName("bgp");
        collectorEntity.setType(CollectorTypes.TOPO_BGP);

        ObjectMapper objectMapper = new ObjectMapper();
        BgpCollectorView bgpCollectorView = new BgpCollectorView();
        bgpCollectorView.getAdvanced().getDebug().setVerbosity(90);
        String params = objectMapper.writeValueAsString(bgpCollectorView);
        logger.info("Entity parameters : " + params);
        collectorEntity.setParams(params);

        CollectorData collectorData = beanConverstionService.populateToolParameters(collectorEntity);
        logger.info("Tool Collector params " + collectorData);
        Assertions.assertNotNull(collectorData.getName());
        Assertions.assertEquals(collectorData.getType(), CollectorTypes.TOPO_BGP);

        BgpCollector bgpCollector = objectMapper.readValue(collectorData.getParams(), BgpCollector.class);
        Assertions.assertEquals(bgpCollector.getDebug().getVerbosity(), 90);
    }

    @Test
    public void testVpnParamConvert() throws JsonProcessingException {
        Collector collectorEntity = new Collector();
        collectorEntity.setName("vpn");
        collectorEntity.setType(CollectorTypes.TOPO_VPN);

        ObjectMapper objectMapper = new ObjectMapper();
        VpnCollectorView vpnCollectorView = new VpnCollectorView();
        vpnCollectorView.setVpnType(List.of(VpnCollector.VpnType.L3VPN));
        vpnCollectorView.getAdvanced().getDebug().setNetRecorder(RecordMode.RECORD);
        String params = objectMapper.writeValueAsString(vpnCollectorView);
        logger.info("Entity parameters : " + params);
        collectorEntity.setParams(params);

        CollectorData collectorData = beanConverstionService.populateToolParameters(collectorEntity);
        logger.info("Tool Collector params " + collectorData);
        Assertions.assertNotNull(collectorData.getName());
        Assertions.assertEquals(collectorData.getType(), CollectorTypes.TOPO_VPN);

        VpnCollector vpnCollector = objectMapper.readValue(collectorData.getParams(), VpnCollector.class);
        Assertions.assertEquals(vpnCollector.getDebug().getNetRecorder(), RecordMode.RECORD);
        Assertions.assertEquals(vpnCollector.getVpnType().size(), 1);
    }

    @Test
    public void testBgplsParamConvert() throws JsonProcessingException {
        Collector collectorEntity = new Collector();
        collectorEntity.setName("bgpls");
        collectorEntity.setType(CollectorTypes.TOPO_BGPLS_XTC);

        ObjectMapper objectMapper = new ObjectMapper();
        BgpLsCollectorView bgpLsCollectorView = new BgpLsCollectorView();
        bgpLsCollectorView.setAsn(2);
        bgpLsCollectorView.getAdvanced().getNodes().setTimeout(20);
        bgpLsCollectorView.getAdvanced().getNodes().setPerformanceData(true);

        bgpLsCollectorView.getAdvanced().getInterfaces().setTimeout(25);
        bgpLsCollectorView.getAdvanced().getInterfaces().setCircuitCleanup(true);
        bgpLsCollectorView.getAdvanced().getInterfaces().setFindParallelLinksc(true);
        bgpLsCollectorView.getAdvanced().getInterfaces().setDiscoverLags(true);
        bgpLsCollectorView.getAdvanced().getInterfaces().setCopyDescription(true);
        bgpLsCollectorView.getAdvanced().getInterfaces().setCollectPhysicalPort(true);

        String params = objectMapper.writeValueAsString(bgpLsCollectorView);
        logger.info("Entity parameters : " + params);
        collectorEntity.setParams(params);

        CollectorData collectorData = beanConverstionService.populateToolParameters(collectorEntity);
        logger.info("Tool Collector params " + collectorData);
        Assertions.assertNotNull(collectorData.getName());
        Assertions.assertEquals(collectorData.getType(), CollectorTypes.TOPO_BGPLS_XTC);

        BgpLsCollector bgpLsCollector = objectMapper.readValue(collectorData.getParams(), BgpLsCollector.class);
        Assertions.assertEquals(bgpLsCollector.getAdvanced().getNodes().getTimeout(), 20);
        Assertions.assertTrue(bgpLsCollector.getAdvanced().getNodes().getPerformanceData());

        Assertions.assertTrue(bgpLsCollector.getAdvanced().getInterfaces().getCircuitCleanup());
        Assertions.assertTrue(bgpLsCollector.getAdvanced().getInterfaces().getCollectPhysicalPort());
        Assertions.assertTrue(bgpLsCollector.getAdvanced().getInterfaces().getCopyDescription());
        Assertions.assertTrue(bgpLsCollector.getAdvanced().getInterfaces().getDiscoverLags());
        Assertions.assertTrue(bgpLsCollector.getAdvanced().getInterfaces().getFindParallelLinks());
        Assertions.assertEquals(bgpLsCollector.getAdvanced().getInterfaces().getTimeout(), 25);

        Assertions.assertEquals(bgpLsCollector.getAsn(), 2);
    }

    @Test
    public void testLspSnmpParamConvert() throws JsonProcessingException {
        Collector collectorEntity = new Collector();
        collectorEntity.setName("snmp");
        collectorEntity.setType(CollectorTypes.LSP_SNMP);

        ObjectMapper objectMapper = new ObjectMapper();
        LspSnmpCollectorView lspSnmpCollectorView = new LspSnmpCollectorView();
        lspSnmpCollectorView.getAdvanced().setTimeout(20);
        lspSnmpCollectorView.getAdvanced().setFindActualPaths(false);
        lspSnmpCollectorView.getAdvanced().getDebug().setNetRecorder(RecordMode.RECORD);
        String params = objectMapper.writeValueAsString(lspSnmpCollectorView);
        logger.info("Entity parameters : " + params);
        collectorEntity.setParams(params);

        CollectorData collectorData = beanConverstionService.populateToolParameters(collectorEntity);
        logger.info("Tool Collector params " + collectorData);
        Assertions.assertNotNull(collectorData.getName());
        Assertions.assertEquals(collectorData.getType(), CollectorTypes.LSP_SNMP);

        LspSnmpCollector lspSnmpCollector = objectMapper.readValue(collectorData.getParams(), LspSnmpCollector.class);
        Assertions.assertEquals(lspSnmpCollector.getDebug().getNetRecorder(), RecordMode.RECORD);
        Assertions.assertEquals(lspSnmpCollector.getDebug().getTimeout(), 20);
        Assertions.assertFalse(lspSnmpCollector.getAdvanced().getFindActualPaths());

    }

    @Test
    public void testPCParamConvert() throws JsonProcessingException {
        Collector collectorEntity = new Collector();
        collectorEntity.setName("pc");
        collectorEntity.setType(CollectorTypes.CONFIG_PARSE);

        ObjectMapper objectMapper = new ObjectMapper();
        ParseConfigCollectorView parseConfigCollectorView = new ParseConfigCollectorView();
        GetConfigView getConfigView = new GetConfigView();
        getConfigView.setEnable(true);
        LoginConfig loginConfigView = new LoginConfig();
        loginConfigView.setTelnetUserName("cisco");
        getConfigView.setLoginConfig(loginConfigView);
        getConfigView.setTimeout(20);

        ParseConfigView parseConfigView = new ParseConfigView();
        parseConfigView.setAsn(2);
        parseConfigView.setIgpProtocol(IgpProtocol.ISIS);
        parseConfigView.setIncludeObjects(List.of(ParseConfig.IncludeObject.RSVP));

        ParseConfigAdvancedView parseConfigAdvancedView = new ParseConfigAdvancedView();
        parseConfigAdvancedView.setCircuitMatch(ParseConfigAdvanced.CircuitMatch.SAME_IGP);

        parseConfigCollectorView.setGetConfig(getConfigView);
        parseConfigCollectorView.setParseConfig(parseConfigView);

        String params = objectMapper.writeValueAsString(parseConfigCollectorView);
        logger.info("Entity parameters : " + params);
        collectorEntity.setParams(params);

        CollectorData collectorData = beanConverstionService.populateToolParameters(collectorEntity);
        logger.info("Tool Collector params " + collectorData);
        Assertions.assertNotNull(collectorData.getName());
        Assertions.assertEquals(collectorData.getType(), CollectorTypes.CONFIG_PARSE);

        ParseConfigCollector configCollector = objectMapper.readValue(collectorData.getParams(), ParseConfigCollector.class);
        Assertions.assertTrue(configCollector.getGetConfig().getEnable());
        Assertions.assertEquals("cisco", configCollector.getGetConfig().getLoginConfig().getTelnetUserName());
        Assertions.assertEquals(20, configCollector.getGetConfig().getDebug().getTimeout());

        Assertions.assertEquals(2, configCollector.getParseConfig().getAsn());
        Assertions.assertEquals(configCollector.getParseConfig().getIgpProtocol(), IgpProtocol.ISIS);
        Assertions.assertEquals(1, configCollector.getParseConfig().getIncludeObjects().size());
        Assertions.assertEquals(ParseConfigAdvanced.CircuitMatch.SAME_IGP, configCollector.getParseConfig().getParseConfigAdvanced().getCircuitMatch());
    }

    @Test
    public void testTPParamConvert() throws JsonProcessingException {
        Collector collectorEntity = new Collector();
        collectorEntity.setName("tp");
        collectorEntity.setType(CollectorTypes.TRAFFIC_POLL);

        ObjectMapper objectMapper = new ObjectMapper();
        TrafficCollectorView trafficCollectorView = new TrafficCollectorView();
        trafficCollectorView.getSnmpTrafficPoller().setTimeout(20);
        trafficCollectorView.getSnmpTrafficPoller().setDiscardOverCapacity(false);
        trafficCollectorView.getLspTraffic().setPeriod(10);
        trafficCollectorView.getMacTraffic().setPeriod(10);
        trafficCollectorView.getInterfaceTraffic().setPeriod(10);

        String params = objectMapper.writeValueAsString(trafficCollectorView);
        logger.info("Entity parameters : " + params);
        collectorEntity.setParams(params);

        CollectorData collectorData = beanConverstionService.populateToolParameters(collectorEntity);
        logger.info("Tool Collector params " + collectorData);
        Assertions.assertNotNull(collectorData.getName());
        Assertions.assertEquals(collectorData.getType(), CollectorTypes.TRAFFIC_POLL);

        TrafficCollector trafficCollector = objectMapper.readValue(collectorData.getParams(), TrafficCollector.class);
        Assertions.assertEquals(trafficCollector.getSnmpTrafficPoller().getDebug().getTimeout(), 20);
        Assertions.assertFalse(trafficCollector.getSnmpTrafficPoller().getDiscardOverCapacity());
        Assertions.assertEquals(trafficCollector.getLspTraffic().getPeriod(),10);
        Assertions.assertEquals(trafficCollector.getMacTraffic().getPeriod(),10);
        Assertions.assertEquals(trafficCollector.getInterfaceTraffic().getPeriod(),10);

    }

    @Test
    public void testInvParamConvert() throws JsonProcessingException {
        Collector collectorEntity = new Collector();
        collectorEntity.setName("inv");
        collectorEntity.setType(CollectorTypes.INVENTORY);

        ObjectMapper objectMapper = new ObjectMapper();
        InventoryCollectorView inventoryCollectorView = new InventoryCollectorView();
        inventoryCollectorView.getAdvanced().setActionTimeout(20);
        inventoryCollectorView.getAdvanced().getBuildInventoryOptions().setExcludeFile("exclude");
        inventoryCollectorView.getAdvanced().getBuildInventoryOptions().getDebug().setVerbosity(20);
        inventoryCollectorView.getAdvanced().getGetInventoryOptions().setLoginAllowed(false);

        String params = objectMapper.writeValueAsString(inventoryCollectorView);
        logger.info("Entity parameters : " + params);
        collectorEntity.setParams(params);

        CollectorData collectorData = beanConverstionService.populateToolParameters(collectorEntity);
        logger.info("Tool Collector params " + collectorData);
        Assertions.assertNotNull(collectorData.getName());
        Assertions.assertEquals(collectorData.getType(), CollectorTypes.INVENTORY);

        InventoryCollector inventoryCollector = objectMapper.readValue(collectorData.getParams(), InventoryCollector.class);
        Assertions.assertEquals(inventoryCollector.getAdvanced().getActionTimeout(), 20);
        Assertions.assertEquals("exclude",inventoryCollector.getAdvanced().getBuildInventoryOptions().getExcludeFile());
        Assertions.assertEquals(inventoryCollector.getAdvanced().getBuildInventoryOptions().getVerbosity(),20);
        Assertions.assertFalse(inventoryCollector.getAdvanced().getGetInventoryOptions().getLoginAllowed());

    }

    @Test
    public void testLayoutParamConvert() throws JsonProcessingException {
        Collector collectorEntity = new Collector();
        collectorEntity.setName("layout");
        collectorEntity.setType(CollectorTypes.LAYOUT);

        ObjectMapper objectMapper = new ObjectMapper();
        LayoutCollectorView layoutCollectorView = new LayoutCollectorView();
        layoutCollectorView.setTemplateFile("euro4_1.pln");
        layoutCollectorView.getAdvanced().setConnectTimeout(20);

        String params = objectMapper.writeValueAsString(layoutCollectorView);
        logger.info("Entity parameters : " + params);
        collectorEntity.setParams(params);

        CollectorData collectorData = beanConverstionService.populateToolParameters(collectorEntity);
        logger.info("Tool Collector params " + collectorData);
        Assertions.assertNotNull(collectorData.getName());
        Assertions.assertEquals(collectorData.getType(), CollectorTypes.LAYOUT);

        LayoutCollector layoutCollector = objectMapper.readValue(collectorData.getParams(), LayoutCollector.class);
        Assertions.assertEquals(layoutCollector.getConnectTimeout(), 20);
        Assertions.assertEquals("euro4_1.pln",layoutCollector.getTemplateFile());

    }

    @Test
    public void testDmdParamConvert() throws JsonProcessingException {
        Collector collectorEntity = new Collector();
        collectorEntity.setName("dmd");
        collectorEntity.setType(CollectorTypes.DEMAND_MESH_CREATOR);

        ObjectMapper objectMapper = new ObjectMapper();
        DmdMeshCreatorView dmdMeshCreatorView = new DmdMeshCreatorView();
        dmdMeshCreatorView.setServiceClass("service");
        dmdMeshCreatorView.getDemandMeshAdvancedView().setDestination("dest");
        dmdMeshCreatorView.getDemandMeshAdvancedView().getDebug().setVerbosity(20);

        String params = objectMapper.writeValueAsString(dmdMeshCreatorView);
        logger.info("Entity parameters : " + params);
        collectorEntity.setParams(params);

        CollectorData collectorData = beanConverstionService.populateToolParameters(collectorEntity);
        logger.info("Tool Collector params " + collectorData);
        Assertions.assertNotNull(collectorData.getName());
        Assertions.assertEquals(collectorData.getType(), CollectorTypes.DEMAND_MESH_CREATOR);

        DmdMeshCreator dmdMeshCreator = objectMapper.readValue(collectorData.getParams(), DmdMeshCreator.class);
        Assertions.assertEquals(dmdMeshCreator.getServiceClass(), "service");
        Assertions.assertEquals(dmdMeshCreator.getAdvanced().getDestination(), "dest");
        Assertions.assertEquals(dmdMeshCreator.getAdvanced().getVerbosity(), 20);

    }

    @Test
    public void testDmdForLspsParamConvert() throws JsonProcessingException {
        Collector collectorEntity = new Collector();
        collectorEntity.setName("dmd");
        collectorEntity.setType(CollectorTypes.DEMAND_FOR_LSPS);

        ObjectMapper objectMapper = new ObjectMapper();
        DmdsForLspsView dmdsForLspsView = new DmdsForLspsView();
        dmdsForLspsView.setServiceClass("service");
        dmdsForLspsView.getDemandForLspAdvancedView().getDebug().setVerbosity(20);
        dmdsForLspsView.setDemandTraffic(DmdsForLsps.LspDemandTraffic.TRAFFIC);

        String params = objectMapper.writeValueAsString(dmdsForLspsView);
        logger.info("Entity parameters : " + params);
        collectorEntity.setParams(params);

        CollectorData collectorData = beanConverstionService.populateToolParameters(collectorEntity);
        logger.info("Tool Collector params " + collectorData);
        Assertions.assertNotNull(collectorData.getName());
        Assertions.assertEquals(collectorData.getType(), CollectorTypes.DEMAND_FOR_LSPS);

        DmdsForLsps dmdsForLsps = objectMapper.readValue(collectorData.getParams(), DmdsForLsps.class);
        Assertions.assertEquals(dmdsForLsps.getServiceClass(), "service");
        Assertions.assertEquals(dmdsForLsps.getAdvanced().getVerbosity(), 20);
        Assertions.assertEquals(dmdsForLsps.getDemandTraffic(), DmdsForLsps.LspDemandTraffic.TRAFFIC);

    }

    @Test
    public void testDmdForP2mpLspsParamConvert() throws JsonProcessingException {
        Collector collectorEntity = new Collector();
        collectorEntity.setName("dmd");
        collectorEntity.setType(CollectorTypes.DEMAND_FOR_P2MP_LSPS);

        ObjectMapper objectMapper = new ObjectMapper();
        DmdsForP2mplspsView dmdsForP2mplspsView = new DmdsForP2mplspsView();
        dmdsForP2mplspsView.setServiceClass("service");
        dmdsForP2mplspsView.getDemandForP2mpAdvancedView().getDebug().setVerbosity(20);
        dmdsForP2mplspsView.setP2mpDemandTraffic(DmdsForP2mplsps.DemandTraffic.ZERO);

        String params = objectMapper.writeValueAsString(dmdsForP2mplspsView);
        logger.info("Entity parameters : " + params);
        collectorEntity.setParams(params);

        CollectorData collectorData = beanConverstionService.populateToolParameters(collectorEntity);
        logger.info("Tool Collector params " + collectorData);
        Assertions.assertNotNull(collectorData.getName());
        Assertions.assertEquals(collectorData.getType(), CollectorTypes.DEMAND_FOR_P2MP_LSPS);

        DmdsForP2mplsps dmdsForP2mplsps = objectMapper.readValue(collectorData.getParams(), DmdsForP2mplsps.class);
        Assertions.assertEquals(dmdsForP2mplsps.getServiceClass(), "service");
        Assertions.assertEquals(dmdsForP2mplsps.getAdvanced().getVerbosity(), 20);
        Assertions.assertEquals(dmdsForP2mplsps.getDemandTraffic(), DmdsForP2mplsps.DemandTraffic.ZERO);

    }

    @Test
    public void testDmdDeductionParamConvert() throws JsonProcessingException {
        Collector collectorEntity = new Collector();
        collectorEntity.setName("demand");
        collectorEntity.setType(CollectorTypes.DEMAND_DEDUCTION);

        ObjectMapper objectMapper = new ObjectMapper();
        DemandDeductionView demandDeductionView = new DemandDeductionView();
        demandDeductionView.setZeroBwTolerance(0.25F);
        demandDeductionView.getAdvancedView().getDebug().setVerbosity(20);
        demandDeductionView.getMeasurements().setLspsPriority(10L);

        String params = objectMapper.writeValueAsString(demandDeductionView);
        logger.info("Entity parameters : " + params);
        collectorEntity.setParams(params);

        CollectorData collectorData = beanConverstionService.populateToolParameters(collectorEntity);
        logger.info("Tool Collector params " + collectorData);
        Assertions.assertNotNull(collectorData.getName());
        Assertions.assertEquals(collectorData.getType(), CollectorTypes.DEMAND_DEDUCTION);

        DemandDeduction demandDeduction = objectMapper.readValue(collectorData.getParams(), DemandDeduction.class);
        Assertions.assertEquals(demandDeduction.getZeroBwTolerance(), 0.25F);
        Assertions.assertEquals(demandDeduction.getAdvanced().getVerbosity(), 20);
        Assertions.assertEquals(demandDeduction.getMeasurements().getLspsPriority(), 10L);

    }

    @Test
    public void testLoginFindParamConvert() throws JsonProcessingException {
        Collector collectorEntity = new Collector();
        collectorEntity.setName("mc");
        collectorEntity.setType(CollectorTypes.LOGIN_FIND_MULTICAST);

        ObjectMapper objectMapper = new ObjectMapper();
        LoginFindMulticastCollectorView loginFindMulticastCollectorView = new LoginFindMulticastCollectorView();
        loginFindMulticastCollectorView.setTimeout(20);
        loginFindMulticastCollectorView.getDebug().setVerbosity(20);

        String params = objectMapper.writeValueAsString(loginFindMulticastCollectorView);
        logger.info("Entity parameters : " + params);
        collectorEntity.setParams(params);

        CollectorData collectorData = beanConverstionService.populateToolParameters(collectorEntity);
        logger.info("Tool Collector params " + collectorData);
        Assertions.assertNotNull(collectorData.getName());
        Assertions.assertEquals(collectorData.getType(), CollectorTypes.LOGIN_FIND_MULTICAST);

        LoginFindMulticastCollector demandDeduction = objectMapper.readValue(collectorData.getParams(), LoginFindMulticastCollector.class);
        Assertions.assertEquals(demandDeduction.getTimeout(), 20);
        Assertions.assertEquals(demandDeduction.getVerbosity(), 20);
    }

    @Test
    public void testLoginPollParamConvert() throws JsonProcessingException {
        Collector collectorEntity = new Collector();
        collectorEntity.setName("mc");
        collectorEntity.setType(CollectorTypes.LOGIN_POLL_MULTICAST);

        ObjectMapper objectMapper = new ObjectMapper();
        LoginPollMulticastCollectorView mcView = new LoginPollMulticastCollectorView();
        mcView.setTimeout(20);
        mcView.getDebug().setVerbosity(20);

        String params = objectMapper.writeValueAsString(mcView);
        logger.info("Entity parameters : " + params);
        collectorEntity.setParams(params);

        CollectorData collectorData = beanConverstionService.populateToolParameters(collectorEntity);
        logger.info("Tool Collector params " + collectorData);
        Assertions.assertNotNull(collectorData.getName());
        Assertions.assertEquals(collectorData.getType(), CollectorTypes.LOGIN_POLL_MULTICAST);

        LoginPollMulticastCollector mc = objectMapper.readValue(collectorData.getParams(), LoginPollMulticastCollector.class);
        Assertions.assertEquals(mc.getTimeout(), 20);
        Assertions.assertEquals(mc.getVerbosity(), 20);
    }

    @Test
    public void testSnmpFindParamConvert() throws JsonProcessingException {
        Collector collectorEntity = new Collector();
        collectorEntity.setName("mc");
        collectorEntity.setType(CollectorTypes.SNMP_FIND_MULTICAST);

        ObjectMapper objectMapper = new ObjectMapper();
        SnmpFindMulticastCollectorView mcView = new SnmpFindMulticastCollectorView();
        mcView.setTimeout(20);
        mcView.getDebug().setVerbosity(20);

        String params = objectMapper.writeValueAsString(mcView);
        logger.info("Entity parameters : " + params);
        collectorEntity.setParams(params);

        CollectorData collectorData = beanConverstionService.populateToolParameters(collectorEntity);
        logger.info("Tool Collector params " + collectorData);
        Assertions.assertNotNull(collectorData.getName());
        Assertions.assertEquals(collectorData.getType(), CollectorTypes.SNMP_FIND_MULTICAST);

        SnmpFindMulticastCollector mc = objectMapper.readValue(collectorData.getParams(), SnmpFindMulticastCollector.class);
        Assertions.assertEquals(mc.getTimeout(), 20);
        Assertions.assertEquals(mc.getVerbosity(), 20);
    }

    @Test
    public void testSnmpPollParamConvert() throws JsonProcessingException {
        Collector collectorEntity = new Collector();
        collectorEntity.setName("mc");
        collectorEntity.setType(CollectorTypes.SNMP_POLL_MULTICAST);

        ObjectMapper objectMapper = new ObjectMapper();
        SnmpPollMulticastCollectorView mcView = new SnmpPollMulticastCollectorView();
        mcView.setTimeout(20);
        mcView.getDebug().setVerbosity(20);

        String params = objectMapper.writeValueAsString(mcView);
        logger.info("Entity parameters : " + params);
        collectorEntity.setParams(params);

        CollectorData collectorData = beanConverstionService.populateToolParameters(collectorEntity);
        logger.info("Tool Collector params " + collectorData);
        Assertions.assertNotNull(collectorData.getName());
        Assertions.assertEquals(collectorData.getType(), CollectorTypes.SNMP_POLL_MULTICAST);

        SnmpPollMulticastCollector mc = objectMapper.readValue(collectorData.getParams(), SnmpPollMulticastCollector.class);
        Assertions.assertEquals(mc.getTimeout(), 20);
        Assertions.assertEquals(mc.getVerbosity(), 20);
    }

    @Test
    public void testMulticastParamConvert() {
        Collector collectorEntity = new Collector();
        collectorEntity.setName("mc");
        collectorEntity.setType(CollectorTypes.MULTICAST);

        ObjectMapper objectMapper = new ObjectMapper();
        MulticastCollectorView mcView = new MulticastCollectorView();

        Assertions.assertDoesNotThrow(()-> {
                    String params = objectMapper.writeValueAsString(mcView);
                    logger.info("Entity parameters : " + params);
                    collectorEntity.setParams(params);
                });

        Assertions.assertThrows(CustomException.class, () ->beanConverstionService.populateToolParameters(collectorEntity));

    }

    @Test
    public void testCustomParamConvert() {
        Collector collectorEntity = new Collector();
        collectorEntity.setName("custom");
        collectorEntity.setType(CollectorTypes.EXTERNAL_SCRIPT);

        ObjectMapper objectMapper = new ObjectMapper();
        CustomCollector customCollector = new CustomCollector();
        customCollector.setTimeout(20);
        customCollector.setExecutableScript("my_script");

        Assertions.assertDoesNotThrow(() -> {
            String params = objectMapper.writeValueAsString(customCollector);

            logger.info("Entity parameters : " + params);
            collectorEntity.setParams(params);

            CollectorData collectorData = beanConverstionService.populateToolParameters(collectorEntity);
            logger.info("Tool Collector params " + collectorData);
            Assertions.assertNotNull(collectorData.getName());
            Assertions.assertEquals(collectorData.getType(), CollectorTypes.EXTERNAL_SCRIPT);

            CustomCollector collector = objectMapper.readValue(collectorData.getParams(), CustomCollector.class);
            Assertions.assertEquals(collector.getTimeout(), 20);
            Assertions.assertEquals(collector.getExecutableScript(), "my_script");
        });
    }
}
