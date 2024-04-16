package com.cisco.configService.service;

import com.cisco.configService.enums.AgentTypes;
import com.cisco.configService.enums.CollectorTypes;
import com.cisco.configService.enums.IgpProtocol;
import com.cisco.configService.enums.IsisLevel;
import com.cisco.configService.exception.CustomException;
import com.cisco.configService.model.common.ui.IgpConfigsView;
import com.cisco.configService.model.composer.CollectorDataView;
import com.cisco.configService.model.composer.SourceCollector;
import com.cisco.configService.model.custom.CustomCollector;
import com.cisco.configService.model.demand.DemandCollector;
import com.cisco.configService.model.lspSnmp.ui.LspSnmpCollectorView;
import com.cisco.configService.model.netflow.CommonConfigs;
import com.cisco.configService.model.netflow.NetflowCollector;
import com.cisco.configService.model.preConfig.AgentData;
import com.cisco.configService.model.preConfig.AllAgentData;
import com.cisco.configService.model.topoBgpls.ui.BgpLsCollectorView;
import com.cisco.configService.model.topoIgp.IgpConfigAdvanced;
import com.cisco.configService.model.topoIgp.ui.IgpCollectorView;
import com.cisco.configService.model.topoVpn.ui.VpnCollectorView;
import com.cisco.configService.model.trafficPoller.ui.TrafficCollectorView;
import com.cisco.configService.webClient.AggregatorWebClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@SpringBootTest
public class CollectorValidationTest {

    @Autowired
    CollectorValidationService collectorValidationService;

    @MockBean
    AgentService agentService;

    @MockBean
    AggregatorWebClient webClient;

    private static final Logger logger = LogManager.getLogger(CollectorValidationTest.class);


    @Test
    void testIgpCollectorValidation() throws JsonProcessingException {
        ObjectMapper Obj = new ObjectMapper();

        CollectorDataView collectorData = new CollectorDataView();
        collectorData.setName("IgpCollector");
        collectorData.setType(CollectorTypes.TOPO_IGP);

        Assertions.assertThrows(CustomException.class, ()-> collectorValidationService.validateCollectorParams(collectorData));


        IgpCollectorView igpCollectorView = new IgpCollectorView();

        // Converting the Java object into a JSON string
        String collectorParams = Obj.writeValueAsString(igpCollectorView);
        logger.info("Collector params = " + collectorParams);
        collectorData.setParams(collectorParams);

        Assertions.assertThrows(ConstraintViolationException.class, ()-> collectorValidationService.validateCollectorParams(collectorData));

        IgpConfigsView igpConfigsView = new IgpConfigsView();
        igpConfigsView.setIgpIndex(1);
        igpConfigsView.setIgpProtocol(IgpProtocol.ISIS.toString());
        igpConfigsView.setSeedRouter("290.123.45.67");
        igpCollectorView.setIgpConfigs(List.of(igpConfigsView));

        // Converting the Java object into a JSON string
        collectorParams = Obj.writeValueAsString(igpCollectorView);
        logger.info("Collector params = " + collectorParams);
        collectorData.setParams(collectorParams);
        //Verify that invalid ip address fails validation.
        Assertions.assertThrows(ConstraintViolationException.class, ()-> collectorValidationService.validateCollectorParams(collectorData));

        igpConfigsView.setSeedRouter("10.10.10.10");
        collectorParams = Obj.writeValueAsString(igpCollectorView);
        collectorData.setParams(collectorParams);
        Assertions.assertDoesNotThrow(() -> collectorValidationService.validateCollectorParams(collectorData));
    }

    @Test
    void testBgplsValidation() throws JsonProcessingException {
        ObjectMapper Obj = new ObjectMapper();

        AgentData agentData = new AgentData();
        agentData.setId(1L);
        agentData.setType(AgentTypes.SR_PCE_AGENT);

        AgentData netflowAgent = new AgentData();
        netflowAgent.setId(3L);
        netflowAgent.setType(AgentTypes.NETFLOW_AGENT);

        Mockito.doReturn(Optional.empty()).when(agentService).getAgent(200L);
        Mockito.doReturn(Optional.of(agentData)).when(agentService).getAgent(1L);
        Mockito.doReturn(Optional.of(agentData)).when(agentService).getAgent(2L);
        Mockito.doReturn(Optional.of(netflowAgent)).when(agentService).getAgent(3L);


        CollectorDataView collectorData = new CollectorDataView();
        collectorData.setName("BgpNLsimo");
        collectorData.setType(CollectorTypes.TOPO_BGPLS_XTC);

        Assertions.assertThrows(CustomException.class, ()-> collectorValidationService.validateCollectorParams(collectorData));

        BgpLsCollectorView bgpLsCollector = new BgpLsCollectorView();
        bgpLsCollector.setPrimarySrPceAgent(1L);
        // Converting the Java object into a JSON string
        String collectorParams = Obj.writeValueAsString(bgpLsCollector);
        logger.info("Collector params = " + collectorParams);
        collectorData.setParams(collectorParams);

        AllAgentData primaryAgent = new AllAgentData();
        primaryAgent.setName("SR_PCE_AGENT");
        primaryAgent.setType(AgentTypes.SR_PCE_AGENT);
        primaryAgent.setId(200L);


        collectorData.setAgents(Set.of(primaryAgent));
        Assertions.assertThrows(CustomException.class, ()-> collectorValidationService.validateCollectorParams(collectorData));

        primaryAgent.setId(1L);
        collectorData.setAgents(Set.of(primaryAgent));
        Assertions.assertDoesNotThrow(()-> collectorValidationService.validateCollectorParams(collectorData));

        AllAgentData secondaryAgent = new AllAgentData();
        secondaryAgent.setId(2L);
        secondaryAgent.setType(AgentTypes.SR_PCE_AGENT);

        collectorData.setAgents(Set.of(primaryAgent,secondaryAgent));
        bgpLsCollector.setSecondarySrPceAgent(2L);
        collectorParams = Obj.writeValueAsString(bgpLsCollector);
        logger.info("Collector params = " + collectorParams);
        collectorData.setParams(collectorParams);
        Assertions.assertDoesNotThrow(()-> collectorValidationService.validateCollectorParams(collectorData));

        AllAgentData invalidAgent = new AllAgentData();
        invalidAgent.setId(3L);
        invalidAgent.setType(AgentTypes.NETFLOW_AGENT);
        collectorData.setAgents(Set.of(invalidAgent));
        Assertions.assertThrows(CustomException.class, ()-> collectorValidationService.validateCollectorParams(collectorData));
    }

    @Test
    @DisplayName("Test IGP validation")
    public void testIgpValidation() throws JsonProcessingException {
        CollectorDataView collectorData = new CollectorDataView();
        collectorData.setName("TOPO_IGP");
        collectorData.setType(CollectorTypes.TOPO_IGP);

        ObjectMapper Obj = new ObjectMapper();
        IgpCollectorView igpCollectorView = new IgpCollectorView();
        IgpConfigsView igpConfigsView = new IgpConfigsView();
        igpConfigsView.setIgpIndex(1);
        igpConfigsView.setSeedRouter("10.225.120.62");
        igpConfigsView.setIgpProtocol(IgpProtocol.ISIS.toString());

        IgpConfigAdvanced igpCollectorAdvanced = new IgpConfigAdvanced();
        igpCollectorAdvanced.setIsisLevel(IsisLevel.BOTH.getValue());
 
        igpCollectorView.setIgpConfigs(List.of(igpConfigsView));
        String collectorParams = Obj.writeValueAsString(igpCollectorView);
        logger.info("Collector params = " + collectorParams);
        collectorData.setParams(collectorParams);

        Assertions.assertDoesNotThrow(()-> collectorValidationService.validateCollectorParams(collectorData));
    }

    @Test
    @DisplayName("Test LSP SNMP validation")
    public void testLspSnmpValidation() throws JsonProcessingException {
        CollectorDataView collectorData = new CollectorDataView();
        collectorData.setName("My_LSP_Snmp");
        collectorData.setType(CollectorTypes.LSP_SNMP);

        ObjectMapper obj = new ObjectMapper();
        LspSnmpCollectorView lspSnmpCollector = new LspSnmpCollectorView();
        String params = obj.writeValueAsString(lspSnmpCollector);
        logger.info("Default params " + params);
        SourceCollector sourceCollector = new SourceCollector();
        sourceCollector.setId(1L);
        collectorData.setSourceCollector(sourceCollector);
        collectorData.setParams(params);

        Assertions.assertDoesNotThrow(() -> collectorValidationService.validateCollectorParams(collectorData));

    }

    @Test
    @DisplayName("Test Topo VPN validation")
    public void testTopoVpnValidation() throws JsonProcessingException {
        CollectorDataView collectorData = new CollectorDataView();
        collectorData.setName("My_Vpn_Collector");
        collectorData.setType(CollectorTypes.TOPO_VPN);

        ObjectMapper obj = new ObjectMapper();
        VpnCollectorView vpnCollector = new VpnCollectorView();
        String params = obj.writeValueAsString(vpnCollector);
        logger.info("Default params " + params);

        SourceCollector sourceCollector = new SourceCollector();
        sourceCollector.setId(1L);
        collectorData.setSourceCollector(sourceCollector);
        collectorData.setParams(params);

        Assertions.assertDoesNotThrow(() -> collectorValidationService.validateCollectorParams(collectorData));

    }

    @Test
    @DisplayName("Test Traffic validation")
    public void testTrafficValidation() throws JsonProcessingException {
        CollectorDataView collectorData = new CollectorDataView();
        collectorData.setName("My_Traffic_Collector");
        collectorData.setType(CollectorTypes.TRAFFIC_POLL);

        ObjectMapper obj = new ObjectMapper();
        TrafficCollectorView trafficCollector = new TrafficCollectorView();
        String params = obj.writeValueAsString(trafficCollector);
        SourceCollector sourceCollector = new SourceCollector();
        sourceCollector.setId(1L);
        collectorData.setSourceCollector(sourceCollector);
        collectorData.setParams(params);

        Assertions.assertDoesNotThrow(() -> collectorValidationService.validateCollectorParams(collectorData));

    }

    @Test
    @DisplayName("Test External script validation")
    public void testCustomCollectorValidation() throws JsonProcessingException {

        Mockito.doNothing().when(webClient).validateCapabilityOfCustomCollector(Mockito.any());

        CollectorDataView collectorData = new CollectorDataView();
        collectorData.setName("My_script");
        collectorData.setType(CollectorTypes.EXTERNAL_SCRIPT);

        ObjectMapper obj = new ObjectMapper();
        CustomCollector customCollector = new CustomCollector();
        customCollector.setExecutableScript("test.sh");
        logger.info("Default params " + obj.writeValueAsString(customCollector));
        String params = "{\"executableScript\":\"es1\",\"scriptLanguage\":\"PYTHON\",\"aggregatorProperties\":\"agg.properties\",\"compressedFile\":\"cw-planning#upload/sample1.txt\",\"inputPlanFile\":\"DB\",\"timeout\":\"10\"}";

        Assertions.assertThrows(CustomException.class, () -> collectorValidationService.validateCollectorParams(collectorData));
        SourceCollector sourceCollector = new SourceCollector();
        sourceCollector.setId(1L);
        collectorData.setSourceCollector(sourceCollector);
        collectorData.setParams(params);



        Assertions.assertDoesNotThrow(() -> collectorValidationService.validateCollectorParams(collectorData));
    }

    @Test
    @DisplayName("Test Demand collector validation")
    public void testDemand() throws JsonProcessingException {
        CollectorDataView collectorData = new CollectorDataView();
        collectorData.setName("Demand");
        collectorData.setType(CollectorTypes.TRAFFIC_DEMAND);

        ObjectMapper obj = new ObjectMapper();
        DemandCollector demandCollector = new DemandCollector();
        logger.info("Default params " + obj.writeValueAsString(demandCollector));
        String params = "{\"demandSteps\":[{\"id\":null,\"name\":\"N1\",\"stepNumber\":1,\"tool\":{\"dmds-for-p2mplsps\":{\"setTraffic\":\"ZERO\",\"serviceClass\":null,\"advanced\":{\"p2mplspsTable\":null,\"optionsFile\":null,\"noGlobalOptions\":false,\"suppressProgress\":false}}},\"enabled\":false},{\"id\":null,\"name\":\"N2\",\"stepNumber\":2,\"tool\":{\"demand-deduction\":{\"fixDemandsWithTraffic\":false,\"demandUpperBound\":0.0,\"removeZeroBwDemands\":true,\"zeroBwTolerance\":0.0,\"zeroFlowTolerance\":0.0,\"measurements\":{\"nodes\":true,\"nodesPriority\":2,\"interfaces\":true,\"interfacesPriority\":1,\"lsps\":true,\"lspsPriority\":2,\"flows\":true,\"flowsPriority\":2},\"advanced\":{\"demandsTable\":\"\",\"fixDemandsTable\":\"\",\"fixMulticastDemands\":false,\"reportFile\":\"\",\"trafficLevel\":\"\",\"scaleMeasurements\":[],\"measErrors\":\"SPREAD\",\"maxPercentLinkUtil\":\"\",\"onlyTunnelAs\":\"\",\"optionsFile\":\"\",\"noGlobalOptions\":false,\"suppressProgress\":false,\"computationTime\":10,\"warnDynamicLsps\":false,\"warnUnroutedLsps\":false}}},\"enabled\":false},{\"id\":null,\"name\":\"N3\",\"stepNumber\":3,\"tool\":{\"external-executable-script\":{\"executableScript\":\"es\",\"scriptLanguage\":\"PYTHON\",\"compressedFile\":\"collectors#user-upload/LotsOfFiles.tgz\",\"aggregatorProperties\":null,\"inputPlanFile\":\"DB\",\"timeout\":30}},\"enabled\":false},{\"id\":null,\"name\":\"N4\",\"stepNumber\":4,\"tool\":{\"copy-demands\":{\"network\":{\"id\":63,\"name\":null,\"type\":null}}},\"enabled\":false},{\"id\":null,\"name\":\"N5\",\"stepNumber\":5,\"tool\":{\"dmds-for-lsps\":{\"private\":false,\"setTraffic\":\"BW\",\"serviceClass\":null,\"advanced\":{\"lspsTable\":null,\"optionsFile\":null,\"noGlobalOptions\":false,\"suppressProgress\":false}}},\"enabled\":false},{\"id\":null,\"name\":\"N6\",\"stepNumber\":6,\"tool\":{\"dmd-mesh-creator\":{\"bothDirections\":true,\"deleteSameName\":true,\"serviceClass\":null,\"topology\":null,\"advanced\":{\"sourceList\":[],\"sourceNodes\":null,\"sourceSites\":null,\"sourceAs\":null,\"sourceEndpoints\":null,\"destNodes\":null,\"destSites\":null,\"destAs\":null,\"destEndpoints\":null,\"demandmeshTable\":null,\"outDemandmeshTable\":null,\"outDemandsTable\":null,\"externalAsInterface-endpoints\":true,\"externalAsInterfaceEndpoints\":true,\"respectAsRelationships\":true,\"externalMesh\":\"RESPECT\",\"setName\":null,\"setTagList\":null,\"optionsFile\":\"\",\"noGlobalOptions\":false,\"destEqualSource\":false,\"destList\":[],\"destination\":\"\",\"includeDemandToSelf\":true,\"suppressProgress\":false}}},\"enabled\":false}]}";
        SourceCollector sourceCollector = new SourceCollector();
        sourceCollector.setId(1L);
        collectorData.setSourceCollector(sourceCollector);
        collectorData.setParams(params);

        Assertions.assertDoesNotThrow(() -> collectorValidationService.validateCollectorParams(collectorData));
    }

    @Test
    @DisplayName("Test Netflow collector validation")
    public void testNetflow() throws JsonProcessingException {
        CollectorDataView collectorData = new CollectorDataView();
        collectorData.setName("Netflow");
        collectorData.setType(CollectorTypes.NETFLOW);

        ObjectMapper obj = new ObjectMapper();
        NetflowCollector netflowCollector = new NetflowCollector();
        CommonConfigs commonConfigs = new CommonConfigs();
        commonConfigs.setAddressFamily(List.of(CommonConfigs.AddressFamily.IPV4_IPV6));
        netflowCollector.setCommonConfigs(commonConfigs);
        String params = obj.writeValueAsString(netflowCollector);
        logger.info("Default params " + params);
        SourceCollector sourceCollector = new SourceCollector();
        sourceCollector.setId(1L);
        collectorData.setSourceCollector(sourceCollector);
        collectorData.setParams(params);

        Assertions.assertDoesNotThrow(() -> collectorValidationService.validateCollectorParams(collectorData));
    }
}

