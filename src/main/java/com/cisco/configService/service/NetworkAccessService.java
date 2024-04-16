package com.cisco.configService.service;

import com.cisco.configService.AppPropertiesReader;
import com.cisco.configService.exception.CustomException;
import com.cisco.configService.model.preConfig.AllNodeProfileData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@Slf4j
public class NetworkAccessService {

    @Autowired
    AppPropertiesReader propertiesReader;

    @Autowired
    NodeProfileService nodeProfileService;

    @Autowired
    PreConfigFileGeneratorService preConfigFileGeneratorService;

    @Autowired
    FileGatewayService fileGatewayService;

    static String NETWORK_ACCESS_FILE_NAME = "network-access.txt";

    //Update the network access for all the node profiles
    public boolean updateNetworkAccess(String networkAccess){
        log.info("Uploading the network access file");
        log.debug("Network access contents ::" + networkAccess );
        try {
            /* Save file in the shared File System */
            fileGatewayService.saveFileStream(new ByteArrayInputStream(networkAccess.getBytes()),
                    null, NETWORK_ACCESS_FILE_NAME);

            List<AllNodeProfileData> nodeProfileDataList = nodeProfileService.getAllNodeProfileData();
            for(AllNodeProfileData nodeProfileData : nodeProfileDataList){
                log.debug("Updating the network access file associated with node profile {} and id {}"
                        , nodeProfileData.getName(), nodeProfileData.getName());
                preConfigFileGeneratorService.generateNetAccess(nodeProfileData.getId());
            }
        } catch (Exception e) {
            log.error("Error updating the network access ", e);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating the network access",
                    "Could not update the network access. Error: " + e.getMessage());
        }

        return true;
    }

    //Reset Network Access for all the ndoe profiles
    public boolean resetNetworkAccess(){
        log.info("Resetting the network access file");
        try {
            log.debug("Deleting the network access stored in the shared file system.");
            /* Delete network access file in the shared File System */
            if(!fileGatewayService.delete(null, NETWORK_ACCESS_FILE_NAME)) {
                log.debug("The network access is already in reset state");
            }

            List<AllNodeProfileData> nodeProfileDataList = nodeProfileService.getAllNodeProfileData();
            for(AllNodeProfileData nodeProfileData : nodeProfileDataList){
                log.debug("Resetting the network access file for node profile {} with id {} ",
                        nodeProfileData.getName() ,nodeProfileData.getId());
                preConfigFileGeneratorService.generateNetAccess(nodeProfileData.getId());
            }
        } catch (Exception e) {
            log.error("Error resetting the network access ", e);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating the network access",
                    "Could not reset the network access. Error: " + e.getMessage());
        }

        return true;
    }

    //Get Network Access
    public String getNetworkAccess() {
        try {
            log.debug("Verify if the user updated network access is present.");
            InputStream inputStream = downloadNetworkAccess();
            if(null == inputStream){
                log.debug("User updated network access is not present. Get the default file.");
                ClassPathResource classPathResource = new ClassPathResource(propertiesReader.getNetworkAccessPath());
                inputStream = classPathResource.getInputStream();
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Error getting the network access details.", e);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error getting the network access details.",
                    "Error getting the network access details." + e.getMessage());
        }
    }

    //Download Network Access
    public InputStream downloadNetworkAccess() {
        InputStream networkAccessFile;
        try {
            log.debug("Verify if the user updated network access is present.");
            networkAccessFile = fileGatewayService.downloadFile(null, NETWORK_ACCESS_FILE_NAME);
            if (null == networkAccessFile) {
                log.debug("User updated network access is not present. Get the default file.");
                ClassPathResource classPathResource = new ClassPathResource(propertiesReader.getNetworkAccessPath());
                networkAccessFile = classPathResource.getInputStream();
            }
            return networkAccessFile;
        } catch (Exception e){
            log.error("Error downloading the network access file ", e);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error downloading the network access file ",
                    "Error downloading the network access file." + e.getMessage());
        }
    }


}
