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
public class NetworkAccess {

    @XmlElement(name = "name", namespace = "http://cisco.com/ns/wae/nimo")
    private String name;
    @XmlElement(name = "default-auth-group", namespace = "http://cisco.com/ns/wae/nimo")
    private String defaultAuthGroup;
    @XmlElement(name = "default-snmp-group", namespace = "http://cisco.com/ns/wae/nimo")
    private String defaultSnmpGroup;
    @XmlElement(name = "login-protocol", namespace = "http://cisco.com/ns/wae/nimo")
    private String loginProtocol;
    @XmlElement(name = "node-access", namespace = "http://cisco.com/ns/wae/nimo")
    private List<NodeAccess> nodeAccess = new ArrayList<NodeAccess>();

    @Override
    public String toString() {
        return "NetworkAccess{" +
                "name='" + name + '\'' +
                ", defaultAuthGroup='" + defaultAuthGroup + '\'' +
                ", defaultSnmpGroup='" + defaultSnmpGroup + '\'' +
                ", loginProtocol='" + loginProtocol + '\'' +
                ", nodeAccess=" + nodeAccess.size() +
                '}';
    }
}
