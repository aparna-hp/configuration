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
public class CfgParseNimo {

    @XmlElement(name = "cfg-parse-agent", namespace = "http://cisco.com/ns/wae/cfg-parse-nimo")
    private String cfgParseAgent;

    @XmlElement(name = "source-network", namespace = "http://cisco.com/ns/wae/cfg-parse-nimo")
    private String sourceNetwork;

    @XmlElement(name="parse", namespace = "http://cisco.com/ns/wae/cfg-parse-nimo")
    private ParseConfig parseConfig;

    @Data
    @NoArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ParseConfig {

        @XmlElement(name="igp-protocol", namespace = "http://cisco.com/ns/wae/cfg-parse-nimo")
        private String igpProtocol;

        @XmlElement(name="isis-level", namespace = "http://cisco.com/ns/wae/cfg-parse-nimo")
        private String isisLevel;

        @XmlElement(name="ospf-area", namespace = "http://cisco.com/ns/wae/cfg-parse-nimo")
        private String ospfArea;

        @XmlElement(name="asn", namespace = "http://cisco.com/ns/wae/cfg-parse-nimo")
        private Integer asn;

        @XmlElement(name="include-object", namespace = "http://cisco.com/ns/wae/cfg-parse-nimo")
        List<String> includeObjects = new ArrayList<>();

        @XmlElement(name="advanced", namespace = "http://cisco.com/ns/wae/cfg-parse-nimo")
        ParseConfigAdv parseConfigAdvanced = new ParseConfigAdv();

        @Data
        @NoArgsConstructor
        @XmlAccessorType(XmlAccessType.FIELD)
        public static class ParseConfigAdv {

            @XmlElement(name="circuit-match", namespace = "http://cisco.com/ns/wae/cfg-parse-nimo")
            String circuitMatch;

            @XmlElement(name="lag-port-match", namespace = "http://cisco.com/ns/wae/cfg-parse-nimo")
            String lagPortMatch;

            @XmlElement(name="ospf-process-id", namespace = "http://cisco.com/ns/wae/cfg-parse-nimo")
            String ospfProcessIds;

            @XmlElement(name="isis-instance-id", namespace = "http://cisco.com/ns/wae/cfg-parse-nimo")
            String isisInstanceIds;

            @XmlElement(name="select-loopback-int", namespace = "http://cisco.com/ns/wae/cfg-parse-nimo")
            Integer selectLoopBackInt;

            @XmlElement(name="resolve-references", namespace = "http://cisco.com/ns/wae/cfg-parse-nimo")
            Boolean resolveReferences;

            @XmlElement(name="use-multithreading", namespace = "http://cisco.com/ns/wae/cfg-parse-nimo")
            Boolean useMultiThreading;

            @XmlElement(name="filter-show-commands", namespace = "http://cisco.com/ns/wae/cfg-parse-nimo")
            Boolean filterShowCommands;

            @XmlElement(name="build-topology", namespace = "http://cisco.com/ns/wae/cfg-parse-nimo")
            Boolean buildTopology;

            @XmlElement(name="shared-media", namespace = "http://cisco.com/ns/wae/cfg-parse-nimo")
            Boolean sharedMedia;

            @XmlElement(name="source", namespace = "http://cisco.com/ns/wae/cfg-parse-nimo")
            String source;

            @XmlElement(name="connect-timeout",namespace ="http://cisco.com/ns/wae/cfg-parse-nimo")
            private Integer timeout;

            @XmlElement(name="verbosity",namespace = "http://cisco.com/ns/wae/cfg-parse-nimo")
            private Integer verbosity;
        }
    }

}
