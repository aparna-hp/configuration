package com.cisco.configService.migration.wae7xConfig.agents;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class CfgParse {

    @XmlElement(name="name", namespace = "http://cisco.com/ns/wae/cfg-parse-agent")
    private String name;

    @XmlElement(name="advanced", namespace = "http://cisco.com/ns/wae/cfg-parse-agent")
    private GetConfigAdvanced advanced;

    @Data
    @NoArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class GetConfigAdvanced {

        @XmlElement(name="force-login-platform",namespace ="http://cisco.com/ns/wae/cfg-parse-agent")
        private String forceLoginPlatform;

        @XmlElement(name="fallback-login-platform",namespace = "http://cisco.com/ns/wae/cfg-parse-agent")
        private String fallbackLoginPlatform;

        @XmlElement(name="try-send-enable",namespace ="http://cisco.com/ns/wae/cfg-parse-agent")
        private Boolean sendEnablePassword;

        @XmlElement(name="telnet-username-prompt",namespace = "http://cisco.com/ns/wae/cfg-parse-agent")
        private String telnetUserName;

        @XmlElement(name="telnet-password-prompt",namespace = "http://cisco.com/ns/wae/cfg-parse-agent")
        private String telnetPassword;

        @XmlElement(name="connect-timeout",namespace ="http://cisco.com/ns/wae/cfg-parse-agent")
        private Integer timeout;

        @XmlElement(name="verbosity",namespace = "http://cisco.com/ns/wae/cfg-parse-agent")
        private Integer verbosity;

        @XmlElement(name="debug",namespace = "http://cisco.com/ns/wae/cfg-parse-agent")
        private GetConfigDebug debug;

        @Data
        @NoArgsConstructor
        @XmlAccessorType(XmlAccessType.FIELD)
        public static class GetConfigDebug {

            @XmlElement(name="login-record-mode", namespace = "http://cisco.com/ns/wae/cfg-parse-agent")
            private String loginRecordMode;
        }

    }


}
