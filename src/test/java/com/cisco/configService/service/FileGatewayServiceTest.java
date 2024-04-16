package com.cisco.configService.service;

import com.cisco.configService.enums.CollectorTypes;
import com.cisco.configService.exception.CustomException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.io.InputStream;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FileGatewayServiceTest {

    @Autowired
    FileGatewayService fileGatewayService;

    String fileName = "IpManage-1.txt",
            filePath = "upload/";

    @Order(1)
    @Test
    public void testUploadService() {
        MockMultipartFile jsonFile = new MockMultipartFile(fileName, fileName, "application/json",
                "{\"key1\": \"value1\",\"key2\": \"value2\"}".getBytes());

        Assertions.assertNotNull(fileGatewayService.upload(jsonFile, filePath, fileName));
    }

    @Order(2)
    public void testDownloadService() {
        InputStream inputStream = fileGatewayService.downloadFile(filePath, fileName);
        Assertions.assertNotNull(inputStream);

    }

    @Order(3)
    public void testDownloadPlanFile() {
        InputStream inputStream = fileGatewayService.downloadPlanFile(1L, 1L, CollectorTypes.TOPO_IGP.name());
        Assertions.assertNotNull(inputStream);

    }

    @Order(4)
    public void testDeleteService() {
        boolean status = fileGatewayService.delete( filePath,fileName);
        Assertions.assertTrue(status);

    }

    @Test
    public void testGetPlanFilePath(){
        Assertions.assertThrows(CustomException.class, ()->fileGatewayService.getPlanFilePath(
                null, CollectorTypes.TOPO_IGP, null));

        Assertions.assertEquals(1, fileGatewayService
                .getPlanFilePath("Network_1", CollectorTypes.TOPO_IGP,"IGP_COLLECTOR").size());

        Assertions.assertEquals(1, fileGatewayService
                .getPlanFilePath("Network_1", CollectorTypes.TOPO_IGP, null).size());

        Assertions.assertEquals(1, fileGatewayService
                .getPlanFilePath("Network_1", null,"IGP_COLLECTOR").size());

        Assertions.assertEquals(1, fileGatewayService
                .getPlanFilePath("Network_1", null, null).size());

    }


}

