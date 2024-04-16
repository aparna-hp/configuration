package com.cisco.configService.migration.wae7xConfig.scheduler;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class Cron {

    @XmlElement(name="minute", namespace = "http://com/cisco/ns/wae/scheduler")
    private String minute;
    @XmlElement(name="hour", namespace = "http://com/cisco/ns/wae/scheduler")
    private String hour;
    @XmlElement(name="day-of-month", namespace = "http://com/cisco/ns/wae/scheduler")
    private String dayOfMonth;
    @XmlElement(name="month", namespace = "http://com/cisco/ns/wae/scheduler")
    private String month;
    @XmlElement(name="day-of-week", namespace = "http://com/cisco/ns/wae/scheduler")
    private String dayOfWeek;

}
