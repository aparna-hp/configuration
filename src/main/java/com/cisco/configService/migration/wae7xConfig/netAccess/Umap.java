package com.cisco.configService.migration.wae7xConfig.netAccess;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class Umap {

    @XmlElement(name="local-user", namespace = "http://tail-f.com/ns/ncs")
    private String localUser;
    @XmlElement(name="remote-name", namespace = "http://tail-f.com/ns/ncs")
    private String remoteName;
    @XmlElement(name="remote-password", namespace = "http://tail-f.com/ns/ncs")
    private String remotePassword;

}
