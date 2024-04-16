package com.cisco.configService.migration.wae7xConfig.scheduler;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class Rpc {

    @XmlElement(name="path", namespace = "http://com/cisco/ns/wae/scheduler")
    private String path;
    @XmlElement(name = "status-path", namespace = "http://com/cisco/ns/wae/scheduler")
    private String statusPath;

}
