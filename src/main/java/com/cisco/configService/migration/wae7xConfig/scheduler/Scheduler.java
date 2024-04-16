package com.cisco.configService.migration.wae7xConfig.scheduler;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class Scheduler {

    @XmlElementWrapper(name="tasks", namespace = "http://com/cisco/ns/wae/scheduler")
    @XmlElement(name="task", namespace = "http://com/cisco/ns/wae/scheduler")
    private List<Task> task;

}
