package com.cisco.configService.migration.wae7xConfig.netAccess;

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
public class Authgroups {

    @XmlElement(name="group", namespace = "http://tail-f.com/ns/ncs")
    private List<Group> group = new ArrayList<>();

    @XmlElement(name="snmp-group", namespace = "http://tail-f.com/ns/ncs")
    private List<SnmpGroup> snmpGroup = new ArrayList<>();

}
