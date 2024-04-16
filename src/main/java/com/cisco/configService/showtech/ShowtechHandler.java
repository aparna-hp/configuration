package com.cisco.configService.showtech;

import com.cisco.robot.skeleton.serviceability.ShowTechHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class ShowtechHandler extends ShowTechHandler{

    @Autowired
    ShowtechService showtechService;

    //TODO: implement this method with logic to handle show tech command
    public void handleShowTech() {
        log.info("ShowtechHandler: handling show tech command...");
        String showTechBaseDir;
        Map<String, String> showTechOptionsMap;
        try {
            // Retrieve show tech CLI command options
            showTechOptionsMap = super.getShowTechOptions();

            // Retrieve base directory for show tech command to write data to
            showTechBaseDir = super.getShowTechBaseDir();

            //Copy the config files, agent status and record files
            showtechService.collectShowtechFiles(showTechBaseDir);

            // All done with handling show tech, so notify the show tech controller
            super.notifyShowTechDone();
        } catch (Exception e) {
            log.error("ShowtechHandler: error encountered:" ,e);
        }
        log.info("ShowtechHandler: handling show tech command successful.");
    }



}
