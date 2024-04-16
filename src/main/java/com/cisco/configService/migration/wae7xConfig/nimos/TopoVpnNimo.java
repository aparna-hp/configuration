package com.cisco.configService.migration.wae7xConfig.nimos;

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
public class TopoVpnNimo {

    @XmlElement(name="source-network", namespace = "http://cisco.com/ns/wae/vpn")
    private String sourceNetwork;

    @XmlElement(name="network-access", namespace = "http://cisco.com/ns/wae/vpn")
    private String networkAccess;

    @XmlElement(name="vpn-types", namespace = "http://cisco.com/ns/wae/vpn")
    private List<VpnType> vpnTypes = new ArrayList<>();

    @XmlElement(name="advanced",namespace = "http://cisco.com/ns/wae/vpn")
    private VpnAdvanced vpnAdvanced = new VpnAdvanced();

    @Data
    @NoArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class VpnType{

        @XmlElement(name="vpn-type", namespace = "http://cisco.com/ns/wae/vpn")
        String vpnType;
    }

    @Data
    @NoArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class VpnAdvanced {

        @XmlElement(name="connect-timeout", namespace = "http://cisco.com/ns/wae/vpn")
        private Integer timeout;

        @XmlElement(name="verbosity",namespace = "http://cisco.com/ns/wae/vpn")
        private Integer verbosity;

        @XmlElement(name="debug",namespace = "http://cisco.com/ns/wae/vpn")
        private VpnDebug vpnDebug = new VpnDebug();
    }

    @Data
    @NoArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class VpnDebug {
        @XmlElement(name="net-recorder",namespace = "http://cisco.com/ns/wae/vpn")
        private String netRecorder;
    }
}


