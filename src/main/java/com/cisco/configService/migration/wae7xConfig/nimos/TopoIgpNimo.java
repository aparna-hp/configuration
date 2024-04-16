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
public class TopoIgpNimo {

    @XmlElement(name="network-access", namespace = "http://cisco.com/ns/wae/topo-igp-nimo")
    private String networkAccess;

    @XmlElement(name="collect-interfaces", namespace = "http://cisco.com/ns/wae/topo-igp-nimo")
    private Boolean collectInterfaces;

    @XmlElement(name="igp-config", namespace = "http://cisco.com/ns/wae/topo-igp-nimo")
    private List<IgpConfig> igpConfig = new ArrayList<IgpConfig>();

    @XmlElement(name="node-filter", namespace = "http://cisco.com/ns/wae/topo-igp-nimo")
    private String nodeFilter;

    @XmlElement(name="advanced", namespace = "http://cisco.com/ns/wae/topo-igp-nimo")
    private IgpAdvanced igpAdvanced = new IgpAdvanced();

    @Data
    @NoArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class IgpAdvanced {

        @XmlElement(name="nodes", namespace = "http://cisco.com/ns/wae/topo-igp-nimo")
        Nodes nodes = new Nodes();

        @XmlElement(name="interfaces", namespace = "http://cisco.com/ns/wae/topo-igp-nimo")
        Interfaces interfaces = new Interfaces();

        @Data
        @NoArgsConstructor
        @XmlAccessorType(XmlAccessType.FIELD)
        public static class Nodes {

            @XmlElement(name = "performance-data", namespace = "http://cisco.com/ns/wae/topo-igp-nimo")
            private Boolean performanceData;

            @XmlElement(name = "remove-node-suffix", namespace = "http://cisco.com/ns/wae/topo-igp-nimo")
            private List<Nodes.RemoveNodeSuffix> removeNodeSuffix = new ArrayList<>();

            @XmlElement(name = "qos-queues", namespace = "http://cisco.com/ns/wae/topo-igp-nimo")
            private Boolean discoverQosQueue;

            @XmlElement(name = "connect-timeout", namespace = "http://cisco.com/ns/wae/topo-igp-nimo")
            private Integer timeout;

            @XmlElement(name = "verbosity", namespace = "http://cisco.com/ns/wae/topo-igp-nimo")
            private Integer verbosity;

            @XmlElement(name = "net-recorder", namespace = "http://cisco.com/ns/wae/topo-igp-nimo")
            private String netRecorder;

            @Data
            @NoArgsConstructor
            @XmlAccessorType(XmlAccessType.FIELD)
            public static class RemoveNodeSuffix {

                @XmlElement(name = "suffix", namespace = "http://cisco.com/ns/wae/topo-igp-nimo")
                private String suffix;
            }
        }

        @Data
        @NoArgsConstructor
        @XmlAccessorType(XmlAccessType.FIELD)
        public static class Interfaces {

            @XmlElement(name="find-parallel-links", namespace = "http://cisco.com/ns/wae/topo-igp-nimo")
            private Boolean findParallelLinks;

            @XmlElement(name="ip-guessing",namespace = "http://cisco.com/ns/wae/topo-igp-nimo")
            private String ipGuessing;

            @XmlElement(name="lag",namespace = "http://cisco.com/ns/wae/topo-igp-nimo")
            private boolean discoverLags;

            @XmlElement(name="lag-port-match",namespace = "http://cisco.com/ns/wae/topo-igp-nimo")
            private String lagPortMatch;

            @XmlElement(name="cleanup-circuits",namespace = "http://cisco.com/ns/wae/topo-igp-nimo")
            private Boolean circuitCleanup ;

            @XmlElement(name="copy-descriptions",namespace = "http://cisco.com/ns/wae/topo-igp-nimo")
            private Boolean copyDescription ;

            @XmlElement(name="get-physical-ports",namespace = "http://cisco.com/ns/wae/topo-igp-nimo")
            private Boolean collectPhysicalPort ;

            @XmlElement(name="min-guess-prefix-length",namespace = "http://cisco.com/ns/wae/topo-igp-nimo")
            private Integer minIPGuessPrefixLength ;

            @XmlElement(name="min-prefix-length",namespace = "http://cisco.com/ns/wae/topo-igp-nimo")
            private Integer minPrefixLength ;

            @XmlElement(name="connect-timeout",namespace = "http://cisco.com/ns/wae/topo-igp-nimo")
            private Integer timeout ;

            @XmlElement(name="verbosity",namespace = "http://cisco.com/ns/wae/topo-igp-nimo")
            private Integer verbosity ;

            @XmlElement(name="net-recorder",namespace = "http://cisco.com/ns/wae/topo-igp-nimo")
            private String netRecorder ;
        }
    }
}
