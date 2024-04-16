package com.cisco.configService.controller;

import com.cisco.configService.enums.CollectorTypes;
import com.cisco.configService.model.composer.CollectorDataView;
import com.cisco.configService.service.CollectorService;
import com.cisco.configService.service.CollectorValidationService;
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

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
public class CollectorControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    CollectorService collectorService;

    @MockBean
    CollectorValidationService nimoValidationService;

    @Test
    @DisplayName("Test get Collector types")
    void testGetCollectorTypes() throws Exception {

        Mockito.doReturn(Arrays.asList(CollectorTypes.values())).when(collectorService).getAllCollectorTypes();

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/networks/collectors/type"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$" , hasSize(CollectorTypes.values().length)));
    }

    @Test
    @DisplayName("Test get default Collector configuration parameters")
    void testGetDefaultCollectorConfigs() throws Exception{

        String bgpConfig = "[{\"name\":\"sourceNetwork\",\"type\":\"java.lang.String\"},{\"name\":\"protocol\",\"type\":\"com.cisco.configService.common.IgpProtocol\"},{\"name\":\"minPrefixLength\",\"type\":\"int\"},{\"name\":\"loginMultiHop\",\"type\":\"boolean\"},{\"name\":\"findInternalAsLink\",\"type\":\"boolean\"}]";

        Mockito.doReturn(bgpConfig).when(collectorService).getDefaultConfigParams(CollectorTypes.TOPO_BGP);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/networks/collectors/default-parameters")
                        .param("collectorType", CollectorTypes.TOPO_BGP.name()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN_VALUE))
                .andExpect(jsonPath("$" , hasSize(5)));
    }

    @Test
    @DisplayName("Test validate Collector configuration parameters")
    void testValidateCollectorConfigs() throws Exception{
        String nimoStr = "{\n" +
                "  \"id\": null,\n" +
                "  \"name\": \"string\",\n" +
                "  \"type\": \"TOPO_IGP\",\n" +
                "  \"networkId\": null,\n" +
                "  \"consolidationType\": \"DARE\",\n" +
                "  \"nodeProfileData\": null,\n" +
                "  \"params\": \"{\\\"igpConfigs\\\":[{\\\"igpIndex\\\":1,\\\"seedRouter\\\":\\\"1.2.3.4\\\",\\\"igpProtocol\\\":\\\"OSPF\\\",\\\"advanced\\\":{\\\"backupRouter\\\":null,\\\"getSegment\\\":false,\\\"isisLevel\\\":\\\"1_2\\\",\\\"ospfArea\\\":\\\"0\\\",\\\"ospfProcessIds\\\":[],\\\"isisProcessIds\\\":[],\\\"removeNullProcessId\\\":false,\\\"runIGPOffline\\\":\\\"OFF\\\",\\\"nodeTag\\\":null,\\\"loginConfig\\\":{\\\"forceLoginPlatform\\\":null,\\\"fallbackLoginPlatform\\\":null,\\\"sendEnablePassword\\\":false,\\\"telnetUserName\\\":null,\\\"telnetPassword\\\":null},\\\"timeout\\\":60,\\\"verbosity\\\":30,\\\"loginRecordMode\\\":\\\"OFF\\\"}}],\\\"collectInterfaces\\\":false,\\\"advanced\\\":{\\\"nodes\\\":{\\\"performanceData\\\":false,\\\"removeNodeSuffix\\\":[],\\\"discoverQosQueue\\\":true,\\\"qosNodeFilterName\\\":null,\\\"timeout\\\":60,\\\"verbosity\\\":30,\\\"netRecorder\\\":\\\"OFF\\\"},\\\"interfaces\\\":{\\\"findParallelLinks\\\":false,\\\"ipGuessing\\\":\\\"SAFE\\\",\\\"discoverLags\\\":false,\\\"lagPortMatch\\\":\\\"GUESS\\\",\\\"circuitCleanup\\\":false,\\\"copyDescription\\\":false,\\\"collectPhysicalPort\\\":false,\\\"minIPGuessPrefixLength\\\":0,\\\"minPrefixLength\\\":30,\\\"timeout\\\":60,\\\"verbosity\\\":30,\\\"netRecorder\\\":\\\"OFF\\\"}}}\",\n" +
                "  \"sourceCollector\": null,\n" +
                "  \"agents\": [],\n" +
                "  \"nodeFilters\": []\n" +
                "}";
        Mockito.doNothing().when(nimoValidationService).validateCollectorParams(Mockito.any(),Mockito.anyBoolean());

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/networks/collectors/validate-collector")
                        .content(nimoStr)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Test get collector by Id")
    void testGetCollectorById() throws Exception {

        CollectorDataView nimoData = new CollectorDataView();
        nimoData.setId(66L);
        nimoData.setName("TOPO_IGP");
        nimoData.setType(CollectorTypes.TOPO_IGP);

        Mockito.doReturn(Optional.of(nimoData)).when(collectorService).getCollector(66L);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/networks/collectors/66"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/networks/collectors/1"))
                .andExpect(status().isNotFound());
    }
}
