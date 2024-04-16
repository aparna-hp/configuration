package com.cisco.configService.migration.wae7xConfig;

import com.cisco.configService.migration.wae7xConfig.agents.Agents;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class Wae {


    @XmlElement(name = "components", namespace = "http://cisco.com/ns/wae")
    private Components components = new Components();

    @XmlElement(name = "agents", namespace = "http://cisco.com/ns/wae")
    private Agents agents = new Agents();

    @XmlElement(name = "nimos", namespace = "http://cisco.com/ns/wae")
    private Nimos nimos = new Nimos();

}
