package com.cisco.configService.service;

import com.cisco.collectionService.utils.StringUtil;
import com.cisco.configService.AppPropertiesReader;
import com.cisco.configService.entity.Collector;
import com.cisco.configService.enums.CollectorTypes;
import com.cisco.configService.exception.CustomException;
import com.cisco.configService.repository.CollectorRepository;
import com.cisco.cwplanning.cwputils.objstore.FileSystemUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Service
@Slf4j
public class FileGatewayService {

    @Autowired
    @Lazy
    FileSystemUtils fileSystemUtils;

    static String FILE_SEPERATOR = "/";
    static String NAME_SEPERATOR = "_";
    static String DB_EXTENTION = ".db";
    static String SAGE_COLLECTOR_TYPE = "SAGE";

    @Autowired
    private AppPropertiesReader properties;

    @Autowired
    private CollectorUtilService collectorUtilService;

    @Autowired
    private CollectorRepository collectorRepository;

    public String upload(MultipartFile file, String filePath, String fileName) {
        log.info("Uploading the file with name {}, original name {}, content type {}, size {} at relative path {}",
                file.getName(), file.getOriginalFilename(), file.getContentType(), file.getSize(), filePath);

        try {
            if (null == fileName) {
                fileName = file.getOriginalFilename();
            }

            String fullPath = properties.getUserUploadDirectory() + fileName;
            if (null != filePath) {
                fullPath = properties.getUserUploadDirectory() + filePath + FILE_SEPERATOR + fileName;
            }

            /* Save file in the shared File System */
            fileSystemUtils.saveMultiPartFile(file, properties.getMountDirectory(), fullPath);
            fileSystemUtils.setFullPermission(properties.getMountDirectory(), fullPath);
        } catch (Exception e) {
            log.error("Error storing the file ", e);
            throw new CustomException("Could not store the file. Error: " + e.getMessage());
        }

        //Return the path where the file got uploaded.
        if(null != filePath) {
            return filePath + FILE_SEPERATOR + fileName;
        }
        return fileName;
    }

    public boolean saveFileStream(InputStream fileIStream, String filePath, String fileName) {
        log.info("Uploading the file into the filePath {} with name {} ", filePath, fileName);

        if (null == filePath) {
            filePath = properties.getUserUploadDirectory();
        }

        try {
            /* Save file in the shared File System */
            if(!fileSystemUtils.isFilePresent(properties.getMountDirectory(), filePath)){
                log.info("Create the folder " + filePath);
                fileSystemUtils.createDirectories(Path.of(properties.getMountDirectory()+filePath));
            }
            fileSystemUtils.setFullPermission(properties.getMountDirectory(), filePath + FILE_SEPERATOR + fileName);
            return fileSystemUtils.saveFileStream(fileIStream, properties.getMountDirectory(), filePath + FILE_SEPERATOR + fileName);
        } catch (Exception e) {
            log.error("Error storing the file ", e);
            throw new CustomException("Could not store the file. Error: " + e.getMessage());
        }
    }

    public InputStream downloadFile(String filePath, String fileName) {
        log.info("Download the file {} at relative path {}", fileName, filePath);
        String fullPath = properties.getUserUploadDirectory() + fileName;
        if (null != filePath) {
            fullPath = properties.getUserUploadDirectory() + filePath + FILE_SEPERATOR + fileName;
        }
        return fileSystemUtils.getFileStream(properties.getMountDirectory(), fullPath);
    }

    public InputStream downloadPlanFile(Long networkId, Long collectorId, String collectorType) {

        String filePath = properties.getNetworkDirectory() +
                FILE_SEPERATOR + networkId + properties.getNetworkFolderSuffix()
                + FILE_SEPERATOR + collectorId + NAME_SEPERATOR + collectorType + DB_EXTENTION;

        log.info("Download the plan file belonging to path " + filePath);

        return fileSystemUtils.getFileStream(properties.getMountDirectory(), filePath);
    }

    public List<String> getPlanFilePath(String networkName, CollectorTypes collectorType, String collectorName) {
        log.info("Get plan file paths associated with network name {}, collector type {} and collector name {} ",
                networkName, collectorType, collectorName);
        List<String> planFileList = new ArrayList<>();
        Long networkId = collectorUtilService.validateNetworkNameAndGetId(networkName);
        if(null == collectorType && StringUtil.isEmpty(collectorName)){
            log.debug("No collector type or collector name specified. Return the sage file path.");
            planFileList.add(properties.getMountDirectory() + properties.getNetworkDirectory() +
                    FILE_SEPERATOR + networkId + properties.getNetworkFolderSuffix()
                            + FILE_SEPERATOR + CollectorService.DARE_COLLECTOR_ID + NAME_SEPERATOR + SAGE_COLLECTOR_TYPE + DB_EXTENTION );
        } else if(null != collectorName){
            log.debug("Return the collector plan file path associated with collector name {}." , collectorName);
            Collector collector = collectorUtilService.validateCollectorName(collectorName);
            planFileList.add(properties.getMountDirectory() + properties.getNetworkDirectory() +
                    FILE_SEPERATOR + networkId + properties.getNetworkFolderSuffix()
                    + FILE_SEPERATOR + collector.getId() + NAME_SEPERATOR + collector.getType() + DB_EXTENTION );

        } else {
            log.debug("Return the collector plan file path associated with collector type {}." , collectorType);
            List<Long> collectorList = collectorRepository.findCollectorIdByNetworkIdAndType(networkId, collectorType);
            if(null == collectorList || collectorList.size() == 0){
                throw new CustomException(HttpStatus.NOT_FOUND, "The collector type " + collectorType +
                        " is not defined under collection " + networkName);
            }
            collectorList.forEach(id ->
                planFileList.add(properties.getMountDirectory() + properties.getNetworkDirectory() +
                        FILE_SEPERATOR + networkId + properties.getNetworkFolderSuffix()
                        + FILE_SEPERATOR + id + NAME_SEPERATOR + collectorType + DB_EXTENTION ));
        }
        log.debug("Plan file path computed :: " + Arrays.toString(planFileList.toArray()));
        return planFileList;
    }

    public boolean delete(String filePath, String fileName) {
        log.info("Delete the file {} at relative path {} ", fileName, filePath);
        String fullPath = properties.getUserUploadDirectory() + fileName;
        if (null != filePath) {
            fullPath = properties.getUserUploadDirectory() + filePath + FILE_SEPERATOR + fileName;
        }
        try {
            /* Delete file in the shared File System */
            return fileSystemUtils.removeFile(properties.getMountDirectory(), fullPath);
        } catch (Exception e) {
            log.error("Error deleting the file ", e);
            throw new CustomException("Could not delete the file. Error: " + e.getMessage());
        }
    }
}
