package com.cisco.configService.service;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NetworkAccessServiceTest {

    @MockBean
    FileGatewayService fileGatewayService;

    @Autowired
    NetworkAccessService networkAccessService;

    static final String NETWORK_ACCESS_TEXT = "Here is the updated content of network access.";

    @Order(1)
    @Test
    public void testUploadService() {
        Mockito.doReturn(true).when(fileGatewayService).saveFileStream(Mockito.any(),
                Mockito.any(),Mockito.any());

        Assertions.assertTrue(networkAccessService.updateNetworkAccess(NETWORK_ACCESS_TEXT));
    }

    @Order(2)
    @Test
    public void testDownloadService() {
        Mockito.doReturn(new ByteArrayInputStream(NETWORK_ACCESS_TEXT.getBytes()))
                .when(fileGatewayService).downloadFile(Mockito.any(), Mockito.any());

        InputStream inputStream = networkAccessService.downloadNetworkAccess();
        Assertions.assertNotNull(inputStream);
        Assertions.assertDoesNotThrow(() -> {
            String output = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            Assertions.assertEquals(NETWORK_ACCESS_TEXT, output);
        });

    }

    @Order(3)
    @Test
    public void testGetFile() {
        Mockito.doReturn(new ByteArrayInputStream(NETWORK_ACCESS_TEXT.getBytes()))
                .when(fileGatewayService).downloadFile(Mockito.any(), Mockito.any());

        String output = networkAccessService.getNetworkAccess();
        Assertions.assertNotNull(output);
        Assertions.assertEquals(NETWORK_ACCESS_TEXT, output);
    }

    @Test
    @Order(4)
    public void testResetService() {
        Mockito.doReturn(true)
                .when(fileGatewayService).delete(Mockito.any(), Mockito.any());

        Mockito.doReturn(new ByteArrayInputStream("NETWORK_ACCESS_TEXT".getBytes()))
                .when(fileGatewayService).downloadFile(Mockito.any(), Mockito.any());

        boolean status = networkAccessService.resetNetworkAccess();
        Assertions.assertTrue(status);
        String output = networkAccessService.getNetworkAccess();
        Assertions.assertNotNull(output);
        Assertions.assertNotEquals(NETWORK_ACCESS_TEXT, output);
    }
}

