package com.cisco.configService.migration.wae7xConfig;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class Network {

    @XmlElement(name = "name", namespace = "http://cisco.com/ns/wae")
    private String name;

    @XmlElement(name = "nimo", namespace = "http://cisco.com/ns/wae")
    private Nimo nimo = new Nimo();

    @XmlElement(name = "plan-archive", namespace = "http://cisco.com/ns/wae/archive")
    private PlanArchive planArchive = new PlanArchive();
}
