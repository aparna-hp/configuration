package com.cisco.configService.migration.wae7xConfig.aggregator;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class Source {

    @XmlElement(name="network", namespace = "http://cisco.com/ns/wae/dare")
    private String network;
    @XmlElement(name="nimo", namespace = "http://cisco.com/ns/wae/dare")
    private String nimo;
    @XmlElement(name="direct-source", namespace = "http://cisco.com/ns/wae/dare")
    private Boolean directSource;

}
