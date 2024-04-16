package com.cisco.configService.migration.wae7xConfig.scheduler;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class Task {


    @XmlElement(name="name", namespace = "http://com/cisco/ns/wae/scheduler")
    private String name;
    @XmlElement(name="enabled", namespace = "http://com/cisco/ns/wae/scheduler")
    private Boolean enabled;
    @XmlElement(name="action", namespace = "http://com/cisco/ns/wae/scheduler")
    private List<Action> action = new ArrayList<Action>();
    @XmlElement(name="triggers", namespace = "http://com/cisco/ns/wae/scheduler")
    private Triggers triggers = new Triggers();

}
