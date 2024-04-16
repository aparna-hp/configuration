package com.cisco.configService;

import com.cisco.configService.maintanance.KafkaConnectorService;
import com.cisco.configService.maintanance.MaintenanceService;
import com.cisco.cwplanning.cwputils.CwpUtilsApplication;
import com.cisco.robot.connector.kafka.KafkaConnectorManager;
import com.cisco.robot.proto.backup_restore.MaintenanceModeState;
import com.cisco.robot.proto.backup_restore.SystemStatus;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication (scanBasePackageClasses = {ConfigServiceApplication.class, CwpUtilsApplication.class})
@Slf4j
public  class ConfigServiceApplication {

	@Value("${maintenanceModeKafkaTopic}")
	private String maintenanceModeKafkaTopic;

	@Value("${applicationName}")
	private String applicationName;

	@Value("${kafkaUrl}")
	private String kafkaUrl;

	public static void main(String[] args) {
		SpringApplication.run(ConfigServiceApplication.class, args);
	}

	@PostConstruct
	public void onApplicationEvent() {
		log.info("Setting startup environment");
		try {
			initMaintenanceModeHandler();
		} catch (Exception e) {
			log.error("Error initializing the maintenance mode.",e);
		}
	}

	private void initMaintenanceModeHandler() throws Exception {
		final MaintenanceService maintmodehdlr = new MaintenanceService();
		SystemStatus currentStatus = maintmodehdlr.getSystemStatus();
		if (currentStatus != null && currentStatus.getMaintenanceModeState() == MaintenanceModeState.ON) {
			log.info("Collection service is Already in maintenance mode on startup");
			maintmodehdlr.setInMaintenanceMode(true);
		} else {
			log.info("Collection service is NOT in maintenance mode on startup");
		}
		final KafkaConnectorManager connMgr = KafkaConnectorService.getInstance(applicationName, kafkaUrl);
		connMgr.subscribe(maintenanceModeKafkaTopic, maintmodehdlr);
	}
}
