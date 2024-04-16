package com.cisco.configService.migration.wae7xConfig.agents;

import com.cisco.configService.model.srPce.SrPceAgent;
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
public class Agents {

    @XmlElementWrapper(name = "cfg-parse", namespace = "http://cisco.com/ns/wae/cfg-parse-agent")
    @XmlElement(name = "cfg-parse", namespace = "http://cisco.com/ns/wae/cfg-parse-agent")
    private List<CfgParse> cfgParseAgent;

    @XmlElementWrapper(name = "xtc",namespace = "http://cisco.com/ns/wae/xtc-agent")
    @XmlElement(name = "xtc",namespace = "http://cisco.com/ns/wae/xtc-agent")
    private List<SrPceAgent> srPceAgent = new ArrayList<>();
}
