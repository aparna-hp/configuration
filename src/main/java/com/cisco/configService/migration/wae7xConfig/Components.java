package com.cisco.configService.migration.wae7xConfig;

import com.cisco.configService.migration.wae7xConfig.aggregator.Aggregators;
import com.cisco.configService.migration.wae7xConfig.scheduler.Scheduler;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class Components {

    @XmlElement(name="aggregators", namespace = "http://cisco.com/ns/wae/dare")
    private Aggregators aggregators = new Aggregators();

    @XmlElement(name="scheduler", namespace = "http://com/cisco/ns/wae/scheduler")
    private Scheduler scheduler = new Scheduler();

}
