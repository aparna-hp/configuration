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
public class TopoBgpNimo {

    @XmlElement(name="source-network", namespace = "http://cisco.com/ns/wae/topo-bgp-nimo")
    private String sourceNetwork;

    @XmlElement(name="network-access", namespace = "http://cisco.com/ns/wae/topo-bgp-nimo")
    private String networkAccess;

    @XmlElement(name="peer-protocol", namespace = "http://cisco.com/ns/wae/topo-bgp-nimo")
    private List<PeerProtocol> peerProtocol = new ArrayList<PeerProtocol>();

    @XmlElement(name="min-prefix-length", namespace = "http://cisco.com/ns/wae/topo-bgp-nimo")
    private Integer minPrefixLength;

    @XmlElement(name="min-IPv6-prefix-length", namespace = "http://cisco.com/ns/wae/topo-bgp-nimo")
    private Integer minIPv6PrefixLength;

    @XmlElement(name="login-multi-hop", namespace = "http://cisco.com/ns/wae/topo-bgp-nimo")
    private Boolean loginToRouterForMultihop;

    @XmlElement(name="advanced" ,namespace = "http://cisco.com/ns/wae/topo-bgp-nimo")
    private BgpAdvanced bgpAdvanced = new BgpAdvanced();

    @Data
    @NoArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class BgpAdvanced {

        @XmlElement(name="force-login-platform", namespace = "http://cisco.com/ns/wae/topo-bgp-nimo")
        private String forceLoginPlatform;

        @XmlElement(name="fallback-login-platform", namespace = "http://cisco.com/ns/wae/topo-bgp-nimo")
        private String fallbackLoginPlatform;

        @XmlElement(name="try-send-enable", namespace = "http://cisco.com/ns/wae/topo-bgp-nimo")
        private Boolean sendEnablePassword;

        @XmlElement(name="telnet-username-prompt", namespace = "http://cisco.com/ns/wae/topo-bgp-nimo")
        private String telnetUserName;

        @XmlElement(name="find-internal-asn-links", namespace = "http://cisco.com/ns/wae/topo-bgp-nimo")
        private Boolean findInternalASNLinks;

        @XmlElement(name="find-non-ip-exit-interface", namespace = "http://cisco.com/ns/wae/topo-bgp-nimo")
        private Boolean findNonIPExitInterface;

        @XmlElement(name="find-internal-exit-interfaces", namespace = "http://cisco.com/ns/wae/topo-bgp-nimo")
        private Boolean findInternalExitInterface;

        @XmlElement(name="get-mac-address", namespace = "http://cisco.com/ns/wae/topo-bgp-nimo")
        private Boolean getMacAddress;

        @XmlElement(name="use-dns", namespace = "http://cisco.com/ns/wae/topo-bgp-nimo")
        private Boolean useDNS;

        @XmlElement(name="force-check-all", namespace = "http://cisco.com/ns/wae/topo-bgp-nimo")
        private Boolean forceCheckAll;

        @XmlElement(name="connect-timeout", namespace = "http://cisco.com/ns/wae/topo-bgp-nimo")
        private Integer timeout = 60;

        @XmlElement(name="debug", namespace = "http://cisco.com/ns/wae/topo-bgp-nimo")
        private BgpDebug bgpDebug = new BgpDebug();

        @Data
        @NoArgsConstructor
        @XmlAccessorType(XmlAccessType.FIELD)
        public static class BgpDebug {

            @XmlElement(name="net-recorder", namespace = "http://cisco.com/ns/wae/topo-bgp-nimo")
            private String netRecorder;

            @XmlElement(name="login-record-mode", namespace = "http://cisco.com/ns/wae/topo-bgp-nimo")
            private String loginRecordMode;
        }

    }

}
