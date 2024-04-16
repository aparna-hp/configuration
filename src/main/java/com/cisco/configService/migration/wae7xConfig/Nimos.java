package com.cisco.configService.migration.wae7xConfig;

import com.cisco.configService.migration.wae7xConfig.netAccess.NetworkAccess;
import com.cisco.configService.migration.wae7xConfig.netAccess.NodeFilter;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class Nimos {


    @XmlElementWrapper(name = "network-access", namespace = "http://cisco.com/ns/wae/nimo")
    @XmlElement(name = "network-access", namespace = "http://cisco.com/ns/wae/nimo")
    private List<NetworkAccess> networkAccess = new ArrayList<>();

    @XmlElementWrapper(name = "node-filter", namespace = "http://cisco.com/ns/wae/nimo")
    @XmlElement(name = "node-filter", namespace = "http://cisco.com/ns/wae/nimo")
    private List<NodeFilter> nodeFilter = new ArrayList<>();
}
