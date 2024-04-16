package com.cisco.configService.service;

import com.cisco.collectionService.model.srPce.status.XtcAgentStatusDto;
import com.cisco.configService.entity.Agents;
import com.cisco.configService.enums.AgentTypes;
import com.cisco.configService.exception.CustomException;
import com.cisco.configService.migration.wae7xConfig.nimos.NetflowNimo;
import com.cisco.configService.model.netflow.agent.NetflowAgent;
import com.cisco.configService.model.netflow.agent.NodeFlowConfig;
import com.cisco.configService.model.netflow.status.NetflowClusterStatus;
import com.cisco.configService.model.preConfig.AgentData;
import com.cisco.configService.model.preConfig.AllAgentData;
import com.cisco.configService.model.trafficPoller.CPStatusResponse;
import com.cisco.configService.webClient.NetflowWebClient;
import com.cisco.configService.webClient.SrPceWebClient;
import com.cisco.configService.webClient.TrafficPollerWebClient;
import com.cisco.configService.webClient.WorkflowManagerWebClient;
import com.cisco.workflowmanager.JobInfo;
import com.cisco.workflowmanager.ScheduleStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AgentTest {

    @Autowired
    AgentService agentService;

    @MockBean
    WorkflowManagerWebClient workflowManagerWebClient;

    @MockBean
    SrPceWebClient srPceWebClient;

    @MockBean
    NetflowWebClient netflowWebClient;

    @MockBean
    TrafficPollerWebClient trafficPollerWebClient;

    private static final Logger logger = LogManager.getLogger(AgentTest.class);

    @Test
    @Order(1)
    @DisplayName("Delete the agent test")
    void deleteAgents(){
        AgentData agentData = new AgentData();
        agentData.setName("sr_pce_agent_delete");
        agentData.setType(AgentTypes.SR_PCE_AGENT);
        String agentParams = "{\"xtcHostIP\": \"10.225.120.119\"}";
        agentData.setParams(agentParams);
        agentService.addAgent(agentData);

        Assertions.assertNotNull(agentData.getId());

        Optional<Long> optionalAgentId =  agentService.deleteAgent(agentData.getId());
        Assertions.assertTrue(optionalAgentId.isPresent());
        Assertions.assertEquals(agentData.getId(), optionalAgentId.get());
    }

    @Test
    @Order(2)
    @DisplayName("Add and update the agent")
    void testAddUpdateAgent(){

        JobInfo jobInfo = new JobInfo();
        jobInfo.setName("sr_pce_agent");
        jobInfo.setScheduleStatus(ScheduleStatus.Active);

        Mockito.doReturn(Optional.of(jobInfo)).when(workflowManagerWebClient).createScheduler(Mockito.any());

        AgentData agentData = new AgentData();
        agentData.setName("sr_pce_agent");
        agentData.setType(AgentTypes.SR_PCE_AGENT);
        Assertions.assertThrows(CustomException.class, () -> agentService.addAgent(agentData));

        String agentParams = "{\"xtcHostIP\": \"10.225.120.119\"}";
        agentData.setParams(agentParams);
        agentService.addAgent(agentData);

        logger.info("Agent data " + agentData);
        Assertions.assertNotNull(agentData.getId());

        agentParams = "{\"xtcHostIP\": \"10.225.120.119\", \"xtcRestPort\": \"2020\"}";
        logger.info("Agent params with updated values = " + agentParams);

        agentData.setParams(agentParams);
        agentService.updateAgent(agentData);

        logger.info("Updated Agent data " + agentData);
        Assertions.assertNotNull(agentData.getId());

        //Verify that invalid SR PCE host fails validation.
        agentParams = "{\"xtcHostIP\": \"x.x.x.x\", \"xtcRestPort\": \"2020\"}";
        agentData.setParams(agentParams);
        Assertions.assertThrows(ConstraintViolationException.class, ()->agentService.updateAgent(agentData));
    }


    @Test
    @Order(3)
    @DisplayName("Get all the Agents")
    void testGetAllAgent() {
        List<AllAgentData> agentDataList = agentService.getAgentInfo(null);
        Assertions.assertTrue(agentDataList.size() > 0);
        agentDataList.forEach(allAgentData -> {
            logger.info("All Agent data " + allAgentData);
            Optional<AgentData> agentDataOptional = agentService.getAgent(allAgentData.getId());
            Assertions.assertTrue(agentDataOptional.isPresent());
            logger.info(" Agent data " + agentDataOptional.get());
        });

        agentDataList = agentService.getAgentInfo(AgentTypes.SR_PCE_AGENT);
        Assertions.assertTrue(agentDataList.size() > 0);
    }

    @Test
    @Order(5)
    @DisplayName("Get the Agents by Collector Id")
    void testGetAgentByCollectorId() {
        List<Agents> agentsList = agentService.getAgentByCollectorId(2L);
        Assertions.assertTrue(agentsList.size() > 0);
        agentsList.forEach(agents -> {
            logger.info(" Agent data " + agents);
            Assertions.assertNotNull(agents.getId());
        });
    }

    @Test
    @Order(6)
    @DisplayName("Add get sr pce agent status")
    void testGetSrPCEAgentStatus(){
        //Mocking the response from Sr pce Manager.
        ResponseEntity<XtcAgentStatusDto> responseEntity = new ResponseEntity<>(new XtcAgentStatusDto(), HttpStatus.OK);

        Mockito.doReturn(responseEntity).when(srPceWebClient).getSrPceAgentStatus(Mockito.any());

        Assertions.assertDoesNotThrow(()->agentService.getStatus(1L));
    }

    @Test
    @Order(7)
    @DisplayName("Add get agent default parameters")
    void testGetAgentDefaultParams() {
        String json  = agentService.getDefaultConfigParams(AgentTypes.SR_PCE_AGENT);
        Assertions.assertNotNull(json);
        logger.info("SR PCE default params = " + json);

        json  = agentService.getDefaultConfigParams(AgentTypes.NETFLOW_AGENT);
        Assertions.assertNotNull(json);
        logger.info("Netflow default params = " + json);
    }

    @Test
    @Order(8)
    @DisplayName("Add and netflow agent")
    void testAddNetflowAgent(){
        AgentData agentData = new AgentData();
        agentData.setName("netflow_agent");
        agentData.setType(AgentTypes.NETFLOW_AGENT);

        agentService.addAgent(agentData);

        logger.info("Agent data " + agentData);
        Assertions.assertNotNull(agentData.getId());

        NetflowAgent netflowAgent = new NetflowAgent();
        NodeFlowConfig nodeFlowConfig = new NodeFlowConfig();
        nodeFlowConfig.setName("flow1");
        nodeFlowConfig.setFlowSourceIP("x.x.x.x");
        nodeFlowConfig.setBGPSourceIP("y.y.y.y");
        netflowAgent.getNodeFlowConfigs().add(nodeFlowConfig);

        ObjectMapper objectMapper = new ObjectMapper();
        Assertions.assertDoesNotThrow(() -> {
            String params = objectMapper.writeValueAsString(netflowAgent);
            agentData.setParams(params);
            //Verify that invalid flow source ip and bgp source ip fails validation.
            Assertions.assertThrows(ConstraintViolationException.class, ()->agentService.updateAgent(agentData));
        });

        AgentData agentData2 = new AgentData();
        agentData2.setName("netflow_agent2");
        agentData2.setType(AgentTypes.NETFLOW_AGENT);

        logger.info("Adding a second netflow agent should throw the exception.");
        Assertions.assertThrows(CustomException.class, () -> agentService.addAgent(agentData2));
    }

    @Test
    @Order(9)
    @DisplayName("Add get netflow agent status")
    void testGetNetflowAgentStatus(){

        //Sample response from netflow service.
        String statusStr = "{\"agents\":[{\"startTime\":\"Jan 8, 2024, 12:42:35 PM\",\"upTime\":\"00d 17h 00m 20s 508ms\",\"lastHBReceived\":\"2024-01-09.05:42:55.825+0000\",\"lastHBAge\":\"00d 00h 00m 06s 265ms\",\"skewTime\":\"00d 00h 00m 00s 003ms\",\"computationSequence\":0,\"computationModel\":\"ias_computed_upon_request\",\"computingIAS\":false,\"jvmMemUtilitzation\":\"8303Mb/8303Mb/6982Mb\",\"daemonperiod\":\"60\",\"daemonStatus\":\"running\",\"bgpPort\":\"179\",\"bgpPortStatus\":\"up\",\"netflowPort\":\"2100\",\"netflowPortStatus\":\"up\",\"netflowDroppedPackets\":\"0\",\"netflowTrafficTotal\":\"0.0\",\"netflowTrafficPerSource\":\"\"}],\"controller\":{\"startTime\":\"Jan 8, 2024, 12:42:33 PM\",\"upTime\":\"00d 17h 00m 20s 799ms\",\"lastHBReceived\":\"2024-01-09.05:42:54.244+0000\",\"lastHBAge\":\"00d 00h 00m 07s 845ms\",\"jvmMemUtilitzation\":\"2075Mb/2075Mb/1737Mb\"},\"clusterSummary\":{\"configuredSize\":1,\"agentsUp\":1,\"daemonsUp\":1,\"computationMode\":\"ias_computed_upon_request\",\"lastResultTime\":null,\"lastNo-resultTime\":null,\"lastComputationDone\":null,\"maxDiffTime\":null,\"maxDiffTimeOK\":null,\"clusterAllOK\":true}}";
        ObjectMapper objectMapper = new ObjectMapper();

        Assertions.assertDoesNotThrow(() -> {
            NetflowClusterStatus netflowClusterStatus = objectMapper.readValue(statusStr, NetflowClusterStatus.class);

            //Mocking the response from Sr pce Manager.
            ResponseEntity<NetflowClusterStatus> responseEntity = new ResponseEntity<>(netflowClusterStatus, HttpStatus.OK);

            Mockito.doReturn(responseEntity).when(netflowWebClient).getNetflowStatus();

            AllAgentData netflowAgent = new AllAgentData();
            netflowAgent.setType(AgentTypes.NETFLOW_AGENT);

            agentService.populateNetflowStatus(netflowAgent);
            Assertions.assertTrue(netflowAgent.getNetflowStatus());

            agentService.populateDetailNetflowStatus(netflowAgent);
            Assertions.assertEquals(1, netflowAgent.getNetflowDetailStatus().getAgents().size());
            Assertions.assertEquals("179", netflowAgent.getNetflowDetailStatus().getAgents().get(0).getBgpPort());
            Assertions.assertEquals("up", netflowAgent.getNetflowDetailStatus().getAgents().get(0).getBgpPortStatus());
            Assertions.assertEquals("2075Mb/2075Mb/1737Mb", netflowAgent.getNetflowDetailStatus().getController().getJvmMemUtilitzation());
            Assertions.assertEquals(1, netflowAgent.getNetflowDetailStatus().getClusterSummary().getConfiguredSize());
            Assertions.assertEquals("ias_computed_upon_request", netflowAgent.getNetflowDetailStatus().getClusterSummary().getComputationMode());
            Assertions.assertEquals(1, netflowAgent.getNetflowDetailStatus().getClusterSummary().getAgentsUp());
            Assertions.assertEquals(1, netflowAgent.getNetflowDetailStatus().getClusterSummary().getDaemonsUp());
        });
    }

    @Test
    @Order(10)
    @DisplayName("Add get traffic pollert status")
    void testGetTrafficPollerStatus(){

        //Mocking the response from Traffic poller.
        ResponseEntity<CPStatusResponse> responseEntity = new ResponseEntity<>(new CPStatusResponse(true), HttpStatus.OK);

        Mockito.doReturn(responseEntity).when(trafficPollerWebClient).getTrafficPollerStatus(Mockito.anyLong());

        AllAgentData trafficPoller = new AllAgentData();
        trafficPoller.setType(AgentTypes.TRAFFIC_POLLER);
        trafficPoller.setId(1L);

        Agents agents = new Agents();
        agents.setId(1L);
        agents.setParams("{\"networkId\": 1}");

        Assertions.assertDoesNotThrow(()-> agentService.populateTrafficPollerStatus(agents,trafficPoller));
    }
}
