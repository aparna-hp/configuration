package com.cisco.configService.migration.wae7xConfig.netAccess;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class NodeAccess {

    @XmlElement(name="ip-address", namespace = "http://cisco.com/ns/wae/nimo")
    private String ipAddress;
    @XmlElement(name="auth-group", namespace = "http://cisco.com/ns/wae/nimo")
    private String authGroup;
    @XmlElement(name="snmp-group", namespace = "http://cisco.com/ns/wae/nimo")
    private String snmpGroup;
    @XmlElement(name="ip-manage", namespace = "http://cisco.com/ns/wae/nimo")
    private String ipManage;

}
