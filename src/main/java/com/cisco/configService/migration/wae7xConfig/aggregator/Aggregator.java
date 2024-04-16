package com.cisco.configService.migration.wae7xConfig.aggregator;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class Aggregator {

    @XmlElement(name="destination", namespace = "http://cisco.com/ns/wae/dare")
    private String destination;
    @XmlElement(name="sources", namespace = "http://cisco.com/ns/wae/dare")
    private Sources sources = new Sources();
    @XmlElement(name="dependencies", namespace = "http://cisco.com/ns/wae/dare")
    private Dependencies dependencies = new Dependencies();
    @XmlElement(name="final-network", namespace = "http://cisco.com/ns/wae/dare")
    private String finalNetwork;
}
