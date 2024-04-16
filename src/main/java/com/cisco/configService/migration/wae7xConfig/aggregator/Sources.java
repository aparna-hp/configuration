package com.cisco.configService.migration.wae7xConfig.aggregator;

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
public class Sources {

    @XmlElement(name="source", namespace = "http://cisco.com/ns/wae/dare")
    private List<Source> source = new ArrayList<Source>();


}
