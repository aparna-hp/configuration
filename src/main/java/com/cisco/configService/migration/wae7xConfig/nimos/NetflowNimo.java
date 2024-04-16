package com.cisco.configService.migration.wae7xConfig.nimos;

import com.cisco.configService.model.netflow.CommonConfigs;
import com.cisco.configService.model.netflow.DemandConfigs;
import com.cisco.configService.model.netflow.IASConfigs;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class NetflowNimo {

    @XmlElement(name="source-network", namespace = "http://cisco.com/ns/wae/network/nimo/netflow")
    private String sourceNetwork;

    @XmlElement(name="config", namespace = "http://cisco.com/ns/wae/network/nimo/netflow")
    private NetflowConfig config;

    @Data
    @NoArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class NetflowConfig {

        @XmlElement(name="common", namespace = "http://cisco.com/ns/wae/network/nimo/netflow")
        private CommonConfigs common = new CommonConfigs();

        @XmlElement(name="ias-flows", namespace = "http://cisco.com/ns/wae/network/nimo/netflow")
        private IASConfigs iasFlows = new IASConfigs();

        @XmlElement(name="demands", namespace = "http://cisco.com/ns/wae/network/nimo/netflow")
        private DemandConfigs demands = new DemandConfigs();

    }
}
