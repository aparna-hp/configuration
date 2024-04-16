package com.cisco.configService.service;

import com.cisco.configService.entity.Collector;
import com.cisco.configService.enums.CollectorTypes;
import com.cisco.configService.model.composer.cli.CollectorData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@SpringBootTest
public class CollectorServiceTest {

    @Autowired
    CollectorService collectorService;

    private static final Logger logger = LogManager.getLogger(CollectorServiceTest.class);

    @Test
    public void testGetCollectorTypes() {
        List<CollectorTypes> types = collectorService.getAllCollectorTypes();
        logger.info("Collector Type :" + types);
        Assertions.assertTrue(types.size() > 0);
    }

    @Test
    public void testGetDefaultCollectorParams() {
        String param = collectorService.getDefaultConfigParams(CollectorTypes.TOPO_BGP);
        Assertions.assertTrue(param.length() > 0);

        param = collectorService.getDefaultConfigParams(CollectorTypes.TOPO_IGP);
        Assertions.assertTrue(param.length() > 0);

        param = collectorService.getDefaultConfigParams(CollectorTypes.TOPO_BGPLS_XTC);
        Assertions.assertTrue(param.length() > 0);

        param = collectorService.getDefaultConfigParams(CollectorTypes.LSP_PCEP_XTC);
        Assertions.assertTrue(param.length() > 0);

        param = collectorService.getDefaultConfigParams(CollectorTypes.LSP_SNMP);
        Assertions.assertTrue(param.length() > 0);

        param = collectorService.getDefaultConfigParams(CollectorTypes.TOPO_VPN);
        Assertions.assertTrue(param.length() > 0);

        param = collectorService.getDefaultConfigParams(CollectorTypes.CONFIG_PARSE);
        Assertions.assertTrue(param.length() > 0);

        param = collectorService.getDefaultConfigParams(CollectorTypes.TRAFFIC_POLL);
        Assertions.assertTrue(param.length() > 0);

        param = collectorService.getDefaultConfigParams(CollectorTypes.INVENTORY);
        Assertions.assertTrue(param.length() > 0);

        param = collectorService.getDefaultConfigParams(CollectorTypes.LAYOUT);
        Assertions.assertTrue(param.length() > 0);

        param = collectorService.getDefaultConfigParams(CollectorTypes.TRAFFIC_DEMAND);
        Assertions.assertTrue(param.length() > 0);

        param = collectorService.getDefaultConfigParams(CollectorTypes.LOGIN_FIND_MULTICAST);
        Assertions.assertTrue(param.length() > 0);

        param = collectorService.getDefaultConfigParams(CollectorTypes.LOGIN_POLL_MULTICAST);
        Assertions.assertTrue(param.length() > 0);

        param = collectorService.getDefaultConfigParams(CollectorTypes.SNMP_FIND_MULTICAST);
        Assertions.assertTrue(param.length() > 0);

        param = collectorService.getDefaultConfigParams(CollectorTypes.SNMP_POLL_MULTICAST);
        Assertions. assertTrue(param.length() > 0);

        param = collectorService.getDefaultConfigParams(CollectorTypes.EXTERNAL_SCRIPT);
        Assertions. assertTrue(param.length() > 0);

        param = collectorService.getDefaultConfigParams(CollectorTypes.NETFLOW);
        Assertions. assertTrue(param.length() > 0);
    }

    @Test
    public void testGetCollector() {
        Iterable<Collector> collectors = collectorService.getAllCollectors();
        Assertions.assertTrue(StreamSupport.stream(collectors.spliterator(), false).findAny().isPresent());
        logger.info("All collector :" + collectors);

        Optional<CollectorData> optionalCollectorData = collectorService.getCollector(1L);
        Assertions.assertTrue(optionalCollectorData.isPresent());
        logger.info("Collector Data = " + optionalCollectorData.get());
    }

}
