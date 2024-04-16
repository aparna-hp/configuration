package com.cisco.configService.exception;

import com.cisco.configService.enums.CollectorTypes;
import com.cisco.configService.model.composer.CollectorDataView;
import com.cisco.configService.model.srPce.SrPceAgent;
import com.cisco.configService.model.topoBgp.BgpCollector;
import com.cisco.configService.model.topoBgpls.BgpLsCollector;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ValidationServiceTest {

    @Autowired
    ValidationService<BgpCollector> validationService;

    @Autowired
    ValidationService<CollectorDataView> collectorDataViewValidationService;

    @Autowired
    ValidationService<SrPceAgent> srPceValidationService;

    @Autowired
    ValidationService<BgpLsCollector> bgpLsCollectorValidationService;

    private static final Logger logger = LogManager.getLogger(ValidationServiceTest.class);


    @Test
    @DisplayName("Verify BGP collector.")
    void testValidationService() {

        BgpCollector bgpCollector = new BgpCollector();
        bgpCollector.setMinIPv6PrefixLength(4);

        Assertions.assertDoesNotThrow(() -> validationService.validateInput(bgpCollector));
    }

    @Test
    @DisplayName("Verify CollectorData")
    void testCollectorConstraint() {

        CollectorDataView collectorData = new CollectorDataView();
        collectorData.setType(CollectorTypes.TOPO_IGP);

        collectorData.setParams("json here");
        collectorData.setName("Test");

        Assertions.assertDoesNotThrow(() -> collectorDataViewValidationService.validateInput(collectorData));
    }

    @Test
    @DisplayName("Verify SrPce agent Data")
    void testAgentConstraint() {
        ObjectMapper mapper = new ObjectMapper();

       String param = "{\"xtcRestPort\":\"\",\"connectionRetryCount\":\"\",\"topologyCollection\": null ,\"lspCollection\": null ,\"connectionTimeoutInterval\":\"\",\"poolSize\":\"\",\"keepAliveInterval\":\"\",\"batchSize\":\"\",\"enabled\":\"\",\"xtcHostIP\":\"1.1.1.1\",\"keepAliveThreshold\":\"\",\"eventsBufferTime\":\"\",\"netPlaybackDir\":\"\",\"eventBufferingEnabled\":\"\",\"netRecordDir\":\"\",\"playbackEventsDelay\":\"\",\"authenticatioType\":null,\"credentials\":\"\",\"maxLspHistory\":\"\",\"netRecorderMode\":null}";
        try {
            SrPceAgent agentData = mapper.readValue(param, SrPceAgent.class);
            Assertions.assertDoesNotThrow(() -> srPceValidationService.validateInput(agentData));

        } catch (JsonProcessingException e) {
            logger.error("Error forming the Sr pce object",e);
        }


    }

    @Test
    @DisplayName("Verify Bgpls collector")
    void testBgplsConstraints() {

        ObjectMapper mapper = new ObjectMapper();

        String param = "{\"primarySrPceAgent\":37,\"secondarySrPceAgent\":46,\"asn\":\"5\",\"igpProtocol\":\"ISIS\",\"extendedTopologyDiscovery\":true,\"reactiveEnabled\":true,\"advanced\":{\"SR_PCE\":{\"singleEndedEbgpDiscovery\":true,\"pauseXtcAgent\":true,\"xtcAgentPauseActionTimeout\":null},\"Nodes\":{\"performanceData\":false,\"removeNodeSuffix\":null,\"discoverQosQueue\":false,\"qosNodeFilterName\":null,\"timeout\":60,\"verbosity\":30,\"netRecorder\":\"OFF\",\"netRecordFile\":null},\"Interfaces\":{\"findParallelLinks\":false,\"ipGuessing\":\"SAFE\",\"discoverLags\":false,\"lagPortMatch\":\"GUESS\",\"circuitCleanup\":false,\"copyDescription\":false,\"collectPhysicalPort\":false,\"minIPGuessPrefixLength\":0,\"minPrefixLength\":30,\"timeout\":60,\"verbosity\":30,\"netRecorder\":\"OFF\",\"netRecordFile\":null}}}";

        try {
            BgpLsCollector bgpLsCollector = mapper.readValue(param, BgpLsCollector.class);
            Assertions.assertDoesNotThrow(() -> bgpLsCollectorValidationService.validateInput(bgpLsCollector));

        } catch (JsonProcessingException e) {
            logger.error("Error forming the Sr pce object",e);
        }


    }
}
