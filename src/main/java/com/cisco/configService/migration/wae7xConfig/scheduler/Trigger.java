package com.cisco.configService.migration.wae7xConfig.scheduler;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class Trigger {

    @XmlElement(name="cron", namespace = "http://com/cisco/ns/wae/scheduler")
    private Cron cron;
}
