package com.cisco.configService.migration.wae7xConfig;

import jakarta.xml.bind.annotation.*;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@lombok.Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "config", namespace = "http://tail-f.com/ns/config/1.0")
public class Config {

    @XmlElementWrapper(name = "networks", namespace = "http://cisco.com/ns/wae")
    @XmlElement(name = "network", namespace = "http://cisco.com/ns/wae")
    private List<Network> networks = new ArrayList<>();

    @XmlElement(name = "wae", namespace = "http://cisco.com/ns/wae")
    private Wae wae = new Wae();

    @XmlElement(name = "devices", namespace = "http://tail-f.com/ns/ncs")
    private Devices devices = new Devices();

}
