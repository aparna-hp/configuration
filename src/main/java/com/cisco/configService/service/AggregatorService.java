package com.cisco.configService.service;

import com.cisco.aggregator.AgeingFlag;
import com.cisco.aggregator.AgeingFlagsConfig;
import com.cisco.aggregator.ExtScriptCapabilityInfo;
import com.cisco.aggregator.UserFileInfo;
import com.cisco.collectionService.utils.StringUtil;
import com.cisco.configService.AppPropertiesReader;
import com.cisco.configService.exception.CustomException;
import com.cisco.configService.model.aggregator.Purge;
import com.cisco.configService.webClient.AggregatorWebClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

@Service
@Slf4j
public class AggregatorService {

    @Autowired
    AggregatorWebClient aggregatorWebClient;

    @Autowired
    FileGatewayService fileGatewayService;

    @Autowired
    AppPropertiesReader propertiesReader;

    public static final String AGGREGATOR_CONFIG_PROPERTIES = "Aggregator_config.properties";

    public void validateAggregatorProperties(String aggregatorProperties){

        log.info("Validating the aggregator properties {} associated with custom collector", aggregatorProperties);
        if(StringUtil.isEmpty(aggregatorProperties)) {
            /* throw new CustomException(HttpStatus.BAD_REQUEST, "The aggregator capabilities is empty.",
                    "Missing aggregator capabilities for the custom collector.");*/
            //Making the aggregator properties for external script optional.
            return;
        }
        try {
            ExtScriptCapabilityInfo extScriptCapabilityInfo = new ExtScriptCapabilityInfo();
            extScriptCapabilityInfo.setCapabilityFilename(aggregatorProperties);
            log.debug("Ext script capability info " + extScriptCapabilityInfo);

            aggregatorWebClient.validateCapabilityOfCustomCollector(extScriptCapabilityInfo);
            log.info("The aggregator property validation is completed.");

        } catch (Exception e){
            log.error("Error encountered in validating the aggregator capabilities.",e);
            throw new CustomException(HttpStatus.BAD_REQUEST, "Error validating the aggregator capabilities.",
                    "Error validating the aggregator capabilities." + e.getMessage());
        }

    }

    public void updateAggregatorProperties(Long networkId, String networkName, Long collectorId, String aggregatorProperties){

        log.info("Updating the aggregator properties {} associated with custom collector", aggregatorProperties);
        if(StringUtil.isEmpty(aggregatorProperties)) {
            /*throw new CustomException(HttpStatus.BAD_REQUEST, "The aggregator capabilities is empty.",
                    "Missing aggregator capabilities for the custom collector.");*/
            //Making the aggregator properties for external script optional.
            return;
        }
        try {
            ExtScriptCapabilityInfo extScriptCapabilityInfo = new ExtScriptCapabilityInfo();
            extScriptCapabilityInfo.setNetworkId(networkId);
            extScriptCapabilityInfo.setNetworkName(networkName);
            extScriptCapabilityInfo.setCollectorId(collectorId);
            extScriptCapabilityInfo.setCapabilityFilename(aggregatorProperties);
            log.debug("Ext script capability info " + extScriptCapabilityInfo);

            aggregatorWebClient.updateCapability(extScriptCapabilityInfo);
            log.info("The aggregator property is updated successfully.");

        } catch (Exception e){
            log.error("Error encountered while updating the aggregator capabilities.",e);
            throw new CustomException(HttpStatus.BAD_REQUEST, "Error updating the aggregator capabilities.",
                    "Error updating the aggregator capabilities." + e.getMessage());
        }

    }

    //Update the aggregator config
    public boolean updateAggrConfig(String aggrConfig){
        //Save the user entered aggregator config to shared file system.
        fileGatewayService.saveFileStream(new ByteArrayInputStream(aggrConfig.getBytes()),
                propertiesReader.getUserUploadDirectory(),
                AGGREGATOR_CONFIG_PROPERTIES);

        //Invoke the aggregator API to update.
        UserFileInfo userFileInfo = new UserFileInfo();
        userFileInfo.setFilename(AGGREGATOR_CONFIG_PROPERTIES);
        return aggregatorWebClient.updateConfig(userFileInfo);
    }

    //Update the Purge config
    public boolean updatePurgeConfig(Purge purgeConfig){
        //Invoke the aggregator API to update.
        AgeingFlagsConfig ageingFlagsConfig = new AgeingFlagsConfig();
        ageingFlagsConfig.setEnabled(purgeConfig.isEnable());

        Map<AgeingFlag, Integer> map = new TreeMap<>();
        map.put(AgeingFlag.L3_NODE, purgeConfig.getL3Node());
        map.put(AgeingFlag.L3_PORT, purgeConfig.getL3Port());
        map.put(AgeingFlag.L3_CIRCUIT, purgeConfig.getL3Circuit());

        ageingFlagsConfig.setTables(map);
        return aggregatorWebClient.updateAgingConfig(ageingFlagsConfig);
    }

    //Get the Purge config
    public Purge getPurgeConfig(){
        Purge purgeConfig = new Purge();

        //Invoke the aggregator API to get Purge config.
        Optional<AgeingFlagsConfig> optionalAgeingFlagsConfig = aggregatorWebClient.getAgingConfig();
        if(optionalAgeingFlagsConfig.isEmpty()){
            log.debug("The purge configuration from aggregator service is empty.");
            return purgeConfig;
        }

        purgeConfig.setEnable(optionalAgeingFlagsConfig.get().isEnabled());
        Map<AgeingFlag, Integer> map = optionalAgeingFlagsConfig.get().getTables();
        purgeConfig.setL3Node(map.computeIfAbsent(AgeingFlag.L3_NODE, value -> 0));
        purgeConfig.setL3Port(map.computeIfAbsent(AgeingFlag.L3_PORT, value -> 0));
        purgeConfig.setL3Circuit(map.computeIfAbsent(AgeingFlag.L3_CIRCUIT, value -> 0));

        log.debug("Purge configuration :" + purgeConfig);
        return purgeConfig;
    }


    //Reset aggregator config
    public boolean resetAggrConfig(){
        return  aggregatorWebClient.resetConfig();
    }

    //Get Aggregator config
    public Optional<String> getAggrConfig() {
      return aggregatorWebClient.getAggrConfig();
    }

    //Download aggregator config
    public InputStream downloadAggrConfig() {
        Optional<String> aggConfig =  aggregatorWebClient.getAggrConfig();
        if(aggConfig.isEmpty()){
            log.debug("Aggregator config is empty.");
            return null;
        }

        return new ByteArrayInputStream(aggConfig.get().getBytes());
    }

}
