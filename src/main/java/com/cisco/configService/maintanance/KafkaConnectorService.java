package com.cisco.configService.maintanance;

import com.cisco.robot.connector.kafka.KafkaConnectorManager;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;

import java.util.Properties;

@Slf4j
public class KafkaConnectorService {

    static KafkaConnectorManager kafkaConnectorManager;

    public static synchronized KafkaConnectorManager getInstance(String applicationName, String kafkaUrl) throws Exception {
        log.info("Inside init of KafkaConnectorService");
        Properties producerProps = new Properties();
        // The default for min.insync.replicas is now 2, but we use a replica factor of 2,
        // so we want to make sure we get an ACK from only 1 broker when we publish.
        // Otherwise, if 1 of the 2 replica brokers is down, we can't publish.
        producerProps.put(ProducerConfig.ACKS_CONFIG, "1");
        kafkaConnectorManager = new KafkaConnectorManager(
                applicationName,
                kafkaUrl,
                1000,
                producerProps);
        return kafkaConnectorManager;
    }

    @PreDestroy
    public void destroy() {
        if (kafkaConnectorManager != null) {
            kafkaConnectorManager.shutdown();
        }
        kafkaConnectorManager = null;
    }
}
