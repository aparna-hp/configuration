package com.cisco.configService.migration.wae7xConfig.netAccess;

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
public class Group {

    @XmlElement(name="name", namespace = "http://tail-f.com/ns/ncs")
    private String name;
    @XmlElement(name="default-map", namespace = "http://tail-f.com/ns/ncs")
    private DefaultMap defaultMap;
    @XmlElement(name="umap", namespace = "http://tail-f.com/ns/ncs")
    private List<Umap> umap = new ArrayList<Umap>();

}
