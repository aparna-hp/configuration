package com.cisco.configService.controller;

import com.cisco.configService.entity.Network;
import com.cisco.configService.enums.CollectorTypes;
import com.cisco.configService.model.composer.CollectorDataView;
import com.cisco.configService.model.composer.NetworkApiResponse;
import com.cisco.configService.model.composer.NetworkDataView;
import com.cisco.configService.model.preConfig.AllNodeProfileData;
import com.cisco.configService.service.NetworkService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
public class NetworkControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    NetworkService networkService;

    private static final Logger logger =
            LogManager.getLogger(NetworkControllerTest.class);

    @Test
    @DisplayName("Test Add Network")
    void testAddNetworkController() throws Exception {

        NetworkDataView postNetwork = new NetworkDataView();
        postNetwork.setName("testNetwork");

        AllNodeProfileData nodeProfileData = new AllNodeProfileData();
        nodeProfileData.setId(1L);
        nodeProfileData.setName("NodeProfile");

        postNetwork.setNodeProfileData(nodeProfileData);
        Mockito.doReturn(new NetworkApiResponse()).when(networkService).saveNetwork(Mockito.any(NetworkDataView.class));

        ObjectMapper mapper = new ObjectMapper();
        String requestJson=mapper.writeValueAsString(postNetwork);

        logger.info("Input Json = " + requestJson);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/networks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Test get all network")
    void testGetNetworkController() throws Exception {

        Network network1 = new Network();
        network1.setName("testNetwork");
        network1.setId(1L);

        Network network2 = new Network();
        network2.setName("testNetwork2");
        network2.setId(2L);
        Mockito.doReturn(Lists.newArrayList(network1,network2)).when(networkService).getAllNetworkData();

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/networks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$" , hasSize(2)))
                .andExpect(jsonPath("$[0].id" , is(1)))
                .andExpect(jsonPath("$[1].id" , is(2)));
    }

    @Test
    @DisplayName("Test Add Topo IGP and Topo BGP network ")
    void testAddIgpNwtworkCollectors() throws Exception {

        NetworkDataView postNetwork = new NetworkDataView();
        postNetwork.setName("IgpNetwork");

        CollectorDataView collectors = new CollectorDataView();
        collectors.setName("Igp Collector");
        collectors.setType(CollectorTypes.TOPO_IGP);
        collectors.setParams("{\"networkName\":\"string\",\"networkAccess\":\"string\",\"igpConfigs\":{\"igpIndex\":1,\"seedRouter\":\"xxx.xxx.xxx.xxx\",\"igpProtocol\":\"string\",\"advanced\":{\"backupRouter\":\"xxx.xxx.xxx.xxx\",\"getSegment\":true,\"isisLevel\":2,\"ospfArea\":\"string\",\"ospfProcessIds\":[1,2,4],\"isisProcessIds\":[1,2,4],\"removeNullProcessId\":false,\"databaseFile\":\"string\",\"runIGPOffline\":false,\"nodeTag\":\"string\",\"timeout\":60,\"verbosity\":60,\"forceLoginPlatform\":\"string\",\"fallbackLoginPlatform\":\"string\",\"sendEnablePassword\":true,\"telnetUserName\":\"string\",\"telnetPassword\":\"string\",\"loginRecordMode\":\"string\",\"loginRecordDir\":\"string\"}},\"collectInterfaces\":true,\"nodeFilterName\":\"string\",\"advanced\":{\"nodes\":{\"performanceData\":true,\"removeNodeSuffix\":[\"string\",\"string\"],\"discoverQosQueue\":true,\"qosNodeFilterName\":\"string\",\"timeout\":60,\"verbosity\":60,\"netRecorder\":\"Off\",\"netRecordFile\":\"string\"},\"interfaces\":{\"findParallelLinks\":true,\"ipGuessing\":\"string\",\"discoverLags\":true,\"lagPortMatch\":\"string\",\"circuitCleanup\":true,\"copyDescription\":true,\"collectPhysicalPort\":true,\"minIPGuessPrefixLength\":10,\"minPrefixLength\":2,\"timeout\":60,\"verbosity\":60,\"netRecorder\":\"Off\",\"netRecordFile\":\"string\"}}}");

        postNetwork.setCollectors(Set.of(collectors));
        AllNodeProfileData nodeProfileData = new AllNodeProfileData();
        nodeProfileData.setId(1L);
        nodeProfileData.setName("NodeProfile");

        postNetwork.setNodeProfileData(nodeProfileData);

        Mockito.doReturn(new NetworkApiResponse()).when(networkService).saveNetwork(postNetwork);

        ObjectMapper mapper = new ObjectMapper();
        String requestJson=mapper.writeValueAsString(postNetwork);

        logger.info("Input Json = " + requestJson);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/networks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated());

    }
}
