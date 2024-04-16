package com.cisco.configService.migration.wae7xConfig.netAccess;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class Usm {

    @XmlElement(name="remote-name", namespace = "http://tail-f.com/ns/ncs")
    private String remoteName;

    @XmlElement(name="security-level", namespace = "http://tail-f.com/ns/ncs")
    private String securityLevel;

    @XmlElement(name="auth", namespace = "http://tail-f.com/ns/ncs")
    private Auth auth = new Auth();

    @XmlElement(name="priv", namespace = "http://tail-f.com/ns/ncs")
    private Priv priv = new Priv();
}
