package com.cisco.configService.migration.wae7xConfig.nimos;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class LspSnmpNimo {

    @XmlElement(name="source-network", namespace = "http://cisco.com/ns/wae/lsp-snmp-nimo")
    private String sourceNetwork;

    @XmlElement(name="network-access", namespace = "http://cisco.com/ns/wae/lsp-snmp-nimo")
    private String networkAccess;

    @XmlElement(name="get-frr-lsps", namespace = "http://cisco.com/ns/wae/lsp-snmp-nimo")
    private Boolean getFrrLsps;

    @XmlElement(name="advanced", namespace = "http://cisco.com/ns/wae/lsp-snmp-nimo")
    private LspSnmpAdvanced lspSnmpAdvanced = new LspSnmpAdvanced();

    @Data
    @NoArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class LspSnmpAdvanced {

        @XmlElement(name="use-calculated-hops", namespace = "http://cisco.com/ns/wae/lsp-snmp-nimo")
        private Boolean useCalculatedHops;

        @XmlElement(name="find-actual-paths", namespace = "http://cisco.com/ns/wae/lsp-snmp-nimo")
        private Boolean findActualPaths;

        @XmlElement(name="get-extras", namespace = "http://cisco.com/ns/wae/lsp-snmp-nimo")
        private Boolean getExtras;

        @XmlElement(name="use-signaled-name", namespace = "http://cisco.com/ns/wae/lsp-snmp-nimo")
        private Boolean useSignaledName;

        @XmlElement(name="auto-bw", namespace = "http://cisco.com/ns/wae/lsp-snmp-nimo")
        private Boolean autoBw;

        @XmlElement(name="connect-timeout", namespace = "http://cisco.com/ns/wae/lsp-snmp-nimo")
        private Integer timeout;

        @XmlElement(name="verbosity",namespace = "http://cisco.com/ns/wae/lsp-snmp-nimo")
        private Integer verbosity;

        @XmlElement(name="debug",namespace = "http://cisco.com/ns/wae/lsp-snmp-nimo")
        private LspDebug lspDebug = new LspDebug();
    }

    @Data
    @NoArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class LspDebug {

        @XmlElement(name="net-recorder",namespace = "http://cisco.com/ns/wae/lsp-snmp-nimo")
        private String netRecorder;
    }

}
