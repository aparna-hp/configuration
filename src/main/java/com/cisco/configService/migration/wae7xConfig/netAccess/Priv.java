package com.cisco.configService.migration.wae7xConfig.netAccess;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class Priv {

    @XmlElement(name="aes", namespace = "http://tail-f.com/ns/ncs")
    private RemotePassword aes;

    @XmlElement(name="des", namespace = "http://tail-f.com/ns/ncs")
    private RemotePassword des;

}
