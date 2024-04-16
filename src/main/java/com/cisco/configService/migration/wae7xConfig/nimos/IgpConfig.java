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
public class IgpConfig {

    @XmlElement(name="index", namespace = "http://cisco.com/ns/wae/topo-igp-nimo")
    private Integer igpIndex;
    @XmlElement(name="seed-router", namespace = "http://cisco.com/ns/wae/topo-igp-nimo")
    private String seedRouter;
    @XmlElement(name="igp-protocol", namespace = "http://cisco.com/ns/wae/topo-igp-nimo")
    private String igpProtocol;
    @XmlElement(name="advanced", namespace = "http://cisco.com/ns/wae/topo-igp-nimo")
    private IgpConfigAdvanced  advanced = new IgpConfigAdvanced();

    @Data
    @NoArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class IgpConfigAdvanced {

        @XmlElement(name="backup-router",namespace = "http://cisco.com/ns/wae/topo-igp-nimo")
        private String backupRouter;

        @XmlElement(name="get-segments",namespace = "http://cisco.com/ns/wae/topo-igp-nimo")
        private Boolean getSegment;

        @XmlElement(name="ospf-area",namespace = "http://cisco.com/ns/wae/topo-igp-nimo")
        private String ospfArea;

        @XmlElement(name="ospf-proc-ids",namespace = "http://cisco.com/ns/wae/topo-igp-nimo")
        private List<Integer> ospfProcessIds = new ArrayList<>();

        @XmlElement(name="isis-proc-ids",namespace = "http://cisco.com/ns/wae/topo-igp-nimo")
        private List<Integer> isisProcessIds = new ArrayList<>();

        @XmlElement(name="remove-null-proc-ids",namespace = "http://cisco.com/ns/wae/topo-igp-nimo")
        private Boolean removeNullProcessId;

        @XmlElement(name="db-file",namespace = "http://cisco.com/ns/wae/topo-igp-nimo")
        private String dbFile;

        @XmlElement(name="offline",namespace = "http://cisco.com/ns/wae/topo-igp-nimo")
        private Boolean runIGPOffline;

        @XmlElement(name="add-node-tag",namespace = "http://cisco.com/ns/wae/topo-igp-nimo")
        private String nodeTag;

        @XmlElement(name="force-login-platform",namespace = "http://cisco.com/ns/wae/topo-igp-nimo")
        private String forceLoginPlatform;

        @XmlElement(name="fallback-login-platform",namespace = "http://cisco.com/ns/wae/topo-igp-nimo")
        private String fallbackLoginPlatform;

        @XmlElement(name="try-send-enable",namespace = "http://cisco.com/ns/wae/topo-igp-nimo")
        private Boolean sendEnablePassword;

        @XmlElement(name="telnet-username-prompt",namespace = "http://cisco.com/ns/wae/topo-igp-nimo")
        private String telnetUserName;

        @XmlElement(name="telnet-password-prompt",namespace = "http://cisco.com/ns/wae/topo-igp-nimo")
        private String telnetPassword;

        @XmlElement(name="connect-timeout",namespace = "http://cisco.com/ns/wae/topo-igp-nimo")
        private Integer timeout;

        @XmlElement(name="verbosity",namespace = "http://cisco.com/ns/wae/topo-igp-nimo")
        private Integer verbosity;

        @XmlElement(name="login-record-mode",namespace = "http://cisco.com/ns/wae/topo-igp-nimo")
        private String netRecorder;
    }


}
