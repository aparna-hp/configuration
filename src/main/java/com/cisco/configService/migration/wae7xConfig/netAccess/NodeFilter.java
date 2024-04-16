package com.cisco.configService.migration.wae7xConfig.netAccess;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class NodeFilter {

    public enum TYPE {
        INCLUDE, EXCLUDE, IGNORE
    }

    @XmlElement(name="name", namespace = "http://cisco.com/ns/wae/nimo")
    String name;

    @XmlElement(name="node-filter", namespace = "http://cisco.com/ns/wae/nimo")
    String nodeFilter;

    @XmlElement(name="node-filter-list", namespace = "http://cisco.com/ns/wae/nimo")
    List<NodeFilterList> nodeFilterList;

    @XmlElement(name="regex-filter", namespace = "http://cisco.com/ns/wae/nimo")
    String regexFilter;

    @XmlElement(name="regex", namespace = "http://cisco.com/ns/wae/nimo")
    String regex;

    @Data
    @NoArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class NodeFilterList{

        @XmlElement(name="node", namespace = "http://cisco.com/ns/wae/nimo")
        String node;
    }
}
