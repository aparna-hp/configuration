package com.cisco.configService.migration.wae7xConfig.nimos;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class TrafficPollNimo {

    @XmlElement(name="source-network", namespace = "http://cisco.com/ns/wae/snmp-traffic-poller")
    private String sourceNetwork;

    @XmlElement(name="network-access", namespace = "http://cisco.com/ns/wae/snmp-traffic-poller")
    private String networkAccess;

    @XmlElement(name="enabled", namespace = "http://cisco.com/ns/wae/snmp-traffic-poller")
    private Boolean enabled;

    @XmlElement(name="iface-traffic-poller", namespace = "http://cisco.com/ns/wae/snmp-traffic-poller")
    private InterfaceTraffic interfaceTraffic = new InterfaceTraffic();

    @XmlElement(name="lsp-traffic-poller", namespace = "http://cisco.com/ns/wae/snmp-traffic-poller")
    private LspTraffic lspTraffic = new LspTraffic();

    @XmlElement(name="lsp-traffic-poller", namespace = "http://cisco.com/ns/wae/snmp-traffic-poller")
    private MacTraffic macTraffic = new MacTraffic();

    @XmlElement(name="advanced", namespace = "http://cisco.com/ns/wae/snmp-traffic-poller")
    private TrafficAdvanced trafficAdvanced = new TrafficAdvanced();

    @Data
    @NoArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class InterfaceTraffic {

        @XmlElement(name="enabled", namespace = "http://cisco.com/ns/wae/snmp-traffic-poller")
        private Boolean enabled;

        @XmlElement(name="period", namespace = "http://cisco.com/ns/wae/snmp-traffic-poller")
        private Integer period ;

        @XmlElement(name="qos-enabled", namespace = "http://cisco.com/ns/wae/snmp-traffic-poller")
        private Boolean qosEnabled;

        @XmlElement(name="vpn-enabled", namespace = "http://cisco.com/ns/wae/snmp-traffic-poller")
        private Boolean vpnEnabled = false;

    }

    @Data
    @NoArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class LspTraffic {

        @XmlElement(name="enabled", namespace = "http://cisco.com/ns/wae/snmp-traffic-poller")
        private Boolean enabled;

        @XmlElement(name="period", namespace = "http://cisco.com/ns/wae/snmp-traffic-poller")
        private Integer period ;

    }

    @Data
    @NoArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class MacTraffic {

        @XmlElement(name="enabled", namespace = "http://cisco.com/ns/wae/snmp-traffic-poller")
        private Boolean enabled;

        @XmlElement(name="period", namespace = "http://cisco.com/ns/wae/snmp-traffic-poller")
        private Integer period ;

    }

    @Data
    @NoArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class TrafficAdvanced {

        @XmlElement(name="snmp-traffic-poller ", namespace = "http://cisco.com/ns/wae/snmp-traffic-poller")
        private SnmpTrafficPoller snmpTrafficPoller = new SnmpTrafficPoller();

    }

    @Data
    @NoArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class SnmpTrafficPoller {

        @XmlElement(name="stats-computing-minimum-window-length", namespace = "http://cisco.com/ns/wae/snmp-traffic-poller")
        private Integer minWindowLengthStats;

        @XmlElement(name="stats-computing-maximum-window-length", namespace = "http://cisco.com/ns/wae/snmp-traffic-poller")
        private Integer maxWindowLengthStats = 450;

        @XmlElement(name="raw-counter-ttl", namespace = "http://cisco.com/ns/wae/snmp-traffic-poller")
        private Integer rawCounterTTL = 15;

        @XmlElement(name="discard-over-capacity", namespace = "http://cisco.com/ns/wae/snmp-traffic-poller")
        private Boolean discardOverCapacity = true;

        @XmlElement(name="log-file", namespace = "http://cisco.com/ns/wae/snmp-traffic-poller")
        private String logFile;

        @XmlElement(name="net-recorder", namespace = "http://cisco.com/ns/wae/snmp-traffic-poller")
        private String netRecorder;

        @XmlElement(name="connect-timeout", namespace = "http://cisco.com/ns/wae/snmp-traffic-poller")
        private Integer timeout;

        @XmlElement(name="verbosity", namespace = "http://cisco.com/ns/wae/snmp-traffic-poller")
        private Integer verbosity;
    }

}
