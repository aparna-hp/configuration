package com.cisco.configService.migration.wae7xConfig.scheduler;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class Action {

    @XmlElement(name="name", namespace = "http://com/cisco/ns/wae/scheduler")
    private String name;
    @XmlElement(name="order", namespace = "http://com/cisco/ns/wae/scheduler")
    private Integer order;
    @XmlElement(name="rpc", namespace = "http://com/cisco/ns/wae/scheduler")
    private Rpc rpc = new Rpc();
}
