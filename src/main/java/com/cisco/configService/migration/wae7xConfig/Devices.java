package com.cisco.configService.migration.wae7xConfig;

import com.cisco.configService.migration.wae7xConfig.netAccess.Authgroups;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class Devices {


    @XmlElement(name = "authgroups", namespace = "http://tail-f.com/ns/ncs")
    private Authgroups authgroups = new Authgroups();
}
