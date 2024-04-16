package com.cisco.configService.migration.wae7xConfig.netAccess;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class SnmpGroupDefaultMap {

    @XmlElement(name="community-name", namespace = "http://tail-f.com/ns/ncs")
    private String communityName;
    @XmlElement(name="usm", namespace = "http://tail-f.com/ns/ncs")
    private Usm usm;

}
