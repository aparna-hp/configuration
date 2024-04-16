package com.cisco.configService.migration.wae7xConfig.nimos;

import com.cisco.configService.model.demand.ui.DemandStepView;
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
public class TrafficDemandsNimo {

    @XmlElement(name = "source-network", namespace = "http://cisco.com/ns/wae/traffic-demands-nimo")
    private String sourceNetwork;

    @XmlElement(name = "demand-mesh-config", namespace = "http://cisco.com/ns/wae/traffic-demands-nimo")
    private DemandMeshConfig demandMeshConfig;

    @XmlElement(name = "connect-timeout", namespace = "http://cisco.com/ns/wae/traffic-demands-nimo")
    private Long connectTimeout;

    @Data
    @NoArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class DemandMeshConfig {

        @XmlElement(name = "demand-mesh-steps", namespace = "http://cisco.com/ns/wae/traffic-demands-nimo")
        private final List<DemandStepView> demandMeshSteps = new ArrayList<>();

    }
}
