package com.cisco.configService.migration.wae7xConfig.aggregator;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class Aging {

    @XmlElement(name="aging-enabled", namespace = "http://cisco.com/ns/wae/dare")
    private Boolean agingEnabled;
    @XmlElement(name="l3-node-aging-duration", namespace = "http://cisco.com/ns/wae/dare")
    private Integer l3NodeAgingDuration;
    @XmlElement(name="l3-port-aging-duration", namespace = "http://cisco.com/ns/wae/dare")
    private Integer l3PortAgingDuration;
    @XmlElement(name="l3-circuit-aging-duration", namespace = "http://cisco.com/ns/wae/dare")
    private Integer l3CircuitAgingDuration;
}
