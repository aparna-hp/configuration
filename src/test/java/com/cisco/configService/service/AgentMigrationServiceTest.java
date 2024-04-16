package com.cisco.configService.service;

import com.cisco.configService.enums.AgentTypes;
import com.cisco.configService.enums.RecordMode;
import com.cisco.configService.migration.AgentMigrationService;
import com.cisco.configService.model.preConfig.AgentData;
import com.cisco.configService.model.srPce.SrPceAgent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Locale;

@SpringBootTest
public class AgentMigrationServiceTest {

    @Autowired
    AgentMigrationService agentMigrationService;

    private static final Logger logger = LogManager.getLogger(AgentMigrationServiceTest.class);

    @Test
    public void testSrpceMigrate() {

        SrPceAgent srPceAgent = new SrPceAgent();
        srPceAgent.setEnabled(true);
        srPceAgent.setName("srPce");
        srPceAgent.setXtcHostIP("10.10.10.10");
        srPceAgent.setUseAuth(true);
        srPceAgent.setConnectionTimeoutInterval(50);
        srPceAgent.setMaxLspHistory(0);
        srPceAgent.setConnectionRetryCount(1);

        srPceAgent.getAdvanced().setPlaybackEventsDelay(10);
        srPceAgent.getAdvanced().setPoolSize(5);
        srPceAgent.getAdvanced().setEventsBufferTime(35);
        srPceAgent.getAdvanced().setTopologyCollectionStr("collection-only");
        srPceAgent.getAdvanced().setLspCollectionStr("collection-and-subscription");
        srPceAgent.setNetRecordStr("record");


        List<AgentData> agentDataList = agentMigrationService.migrateSrpceAgents(List.of(srPceAgent));
        Assertions.assertEquals(1, agentDataList.size());
        Assertions.assertEquals("srPce", agentDataList.get(0).getName());
        Assertions.assertEquals(AgentTypes.SR_PCE_AGENT, agentDataList.get(0).getType());

        String params = agentDataList.get(0).getParams();
        ObjectMapper objectMapper = new ObjectMapper();


        Assertions.assertDoesNotThrow(() ->{
            SrPceAgent migratedAgent = objectMapper.readValue(params, SrPceAgent.class);
            Assertions.assertTrue( migratedAgent.isEnabled());
            Assertions.assertEquals(SrPceAgent.AuthenticationType.BASIC, migratedAgent.getAuthenticationType());
            Assertions.assertEquals(srPceAgent.getXtcHostIP(), migratedAgent.getXtcHostIP());
            Assertions.assertEquals(10, migratedAgent.getPlaybackEventsDelay());
            Assertions.assertEquals(5, migratedAgent.getPoolSize());
            Assertions.assertEquals(35, migratedAgent.getEventsBufferTime());

            Assertions.assertEquals(1, migratedAgent.getConnectionRetryCount());
            Assertions.assertEquals(SrPceAgent.CollectionType.COLLECTION_ONLY, migratedAgent.getTopologyCollection());
            Assertions.assertEquals(SrPceAgent.CollectionType.COLLECTION_AND_SUBSCRIPTION, migratedAgent.getLspCollection());
            Assertions.assertEquals(RecordMode.RECORD, migratedAgent.getNetRecorderMode());
        });
    }
}
