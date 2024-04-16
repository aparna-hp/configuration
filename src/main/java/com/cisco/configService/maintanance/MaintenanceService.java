package com.cisco.configService.maintanance;

import com.cisco.robot.connector.kafka.KafkaMessageDispatcher;
import com.cisco.robot.messaging.RobotMessage;
import com.cisco.robot.messaging.Util;
import com.cisco.robot.proto.signal.CommonReqSignal;
import com.cisco.robot.skeleton.serviceability.MaintenanceModeHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Service
@Slf4j
public class MaintenanceService extends MaintenanceModeHandler implements KafkaMessageDispatcher {

    private static final String SUCCESS = "Success";

    String localInstanceName;

    private static volatile boolean inMaintenanceMode;

    @Override
    public String getClientId() {
        return  this.getLocalInstanceName();
    }

    /**
     * The local instance name is also just the hostname of this container.
     */
    public  synchronized String getLocalInstanceName() {
        if (localInstanceName == null) {
            try {
                localInstanceName = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                log.warn("Failed to get local hostname. Cannot get local Instance Name yet.", e);
            }
        }
        return localInstanceName;
    }

    public  boolean isInMaintenanceMode() {
        return inMaintenanceMode;
    }

    public  void setInMaintenanceMode(boolean inMaintenanceMode) {
        MaintenanceService.inMaintenanceMode = inMaintenanceMode;
    }

    @Override
    public void onMessage(RobotMessage robotMsg) {
        log.info("Got maintenance mode RobotMessage: {}", robotMsg);

        CommonReqSignal.Builder commonReqSignalBuilder = (CommonReqSignal.Builder) Util.unmarshalRobotMsg(robotMsg, CommonReqSignal.newBuilder());

        if (commonReqSignalBuilder != null) {
            CommonReqSignal commonReqSignal = commonReqSignalBuilder.build();
            log.info("Got CommonReqSignal: {}", commonReqSignal);

            switch (commonReqSignal.getType()) {
                case MAINTENANCE_SIGNAL_INIT -> {
                    inMaintenanceMode=true;
                    super.notifyMaintenanceModeActivated(true, SUCCESS, commonReqSignal);
                    log.info("Entering maintenance mode");
                }
                case MAINTENANCE_SIGNAL_EXIT -> {
                    inMaintenanceMode=false;
                    super.notifyMaintenanceModeDeactivated(true, SUCCESS, commonReqSignal);
                    log.info("Exiting maintenance mode");
                }
                default -> log.info("Skipping signal");
            }
        }
    }
}
