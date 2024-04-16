package com.cisco.configService.service;

import com.cisco.configService.model.aggregator.Purge;
import com.cisco.configService.webClient.AggregatorWebClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;


@SpringBootTest
public class AggregatorServiceTest {

    @Autowired
    AggregatorService aggregatorService;

    @MockBean
    AggregatorWebClient webClient;

    @MockBean
    FileGatewayService fileGatewayService;

    private static final Logger log =
            LogManager.getLogger(AggregatorServiceTest.class);

    @Test
    @DisplayName("Test validate the external executor properties.")
    void testValidateAggrProperties() {

        Mockito.doNothing().when(webClient).validateCapabilityOfCustomCollector(Mockito.any());

        Assertions.assertDoesNotThrow(() -> {
            aggregatorService.validateAggregatorProperties("ExternalScript.properties");
        });
    }

    @Test
    @DisplayName("Test update the external executor properties.")
    void testUpdateAggrProperties() {

        Mockito.doNothing().when(webClient).updateCapability(Mockito.any());

        Assertions.assertDoesNotThrow(() -> {
            aggregatorService.updateAggregatorProperties(12L, "DemandTest",
                    35L,"ExternalScript.properties");
        });
    }

    @Test
    @DisplayName("Test Empty Global Aggregator properties.")
    void testGetEmptyAggrConfig() {

        Mockito.doReturn(Optional.empty()).when(webClient).getAggrConfig();

        Assertions.assertDoesNotThrow(() -> {
            aggregatorService.getAggrConfig();
        });
    }

    @Test
    @DisplayName("Test get Global Aggregator properties.")
    void testGetAggrConfig() {

        Mockito.doReturn(Optional.of("Here is the global aggregator properties")).when(webClient).getAggrConfig();

        Assertions.assertNotNull(aggregatorService.getAggrConfig());
        Assertions.assertNotNull(aggregatorService.downloadAggrConfig());

    }

    @Test
    @DisplayName("Test update Global Aggregator properties.")
    void testUpdateAggrConfig() {

        Mockito.doReturn(true).when(webClient).updateConfig(Mockito.any());
        Mockito.doReturn(true).when(fileGatewayService).saveFileStream(
                Mockito.any(), Mockito.any(), Mockito.any());

        Assertions.assertTrue(aggregatorService.updateAggrConfig("Here is the global aggregator properties"));
    }

    @Test
    @DisplayName("Test reset Global Aggregator properties.")
    void testResetAggrConfig() {

        Mockito.doReturn(true).when(webClient).resetConfig();

        Assertions.assertTrue(aggregatorService.resetAggrConfig());
    }

    @Test
    @DisplayName("Test get Purge properties.")
    void testGetPurgeConfig() {

        Mockito.doReturn(Optional.empty()).when(webClient).getAgingConfig();
        Purge purge = aggregatorService.getPurgeConfig();

        Assertions.assertNotNull(purge);
        Assertions.assertFalse(purge.isEnable());
        Assertions.assertEquals(0, purge.getL3Node());
        Assertions.assertEquals(0, purge.getL3Port());
        Assertions.assertEquals(0, purge.getL3Circuit());
    }

    @Test
    @DisplayName("Test update Purge properties.")
    void testUpdatePurgeConfig() {

        Mockito.doReturn(true).when(webClient).updateAgingConfig(Mockito.any());

        Assertions.assertTrue(aggregatorService.updatePurgeConfig(new Purge()));
    }

}
