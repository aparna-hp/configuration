package com.cisco.configService.migration.wae7xConfig.nimos;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class PeerProtocol {

    @XmlElement(name="protocol", namespace = "http://cisco.com/ns/wae/topo-bgp-nimo")
    private String protocol;


}
