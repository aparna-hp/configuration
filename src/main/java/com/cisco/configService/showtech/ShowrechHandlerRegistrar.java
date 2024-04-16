package com.cisco.configService.showtech;

import com.cisco.robot.skeleton.Conductor;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ShowrechHandlerRegistrar {

    @Autowired
    ShowtechHandler showtechHandler;

    @PostConstruct
    public void init() {
        // initialize conductor for skeleton functions such as meta monitoring
        try {
            log.info("init Initializing conductor");
            final Conductor conductor = new Conductor();
            // Exception will be thrown if you try to register the Maintenance handler
            // before init
            conductor.init("collection-service", false);
            // we do not want the library to attempt to parse a missing configuration file
            conductor.registerShowTechHandler(showtechHandler);
            conductor.run();
            log.info("init Conductor initilazation done");
        } catch (Exception e) {
            log.error("Failed to init conductor", e);
        }
    }
}
