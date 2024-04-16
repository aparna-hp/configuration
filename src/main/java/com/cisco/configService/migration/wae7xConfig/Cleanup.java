package com.cisco.configService.migration.wae7xConfig;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class Cleanup {

    @XmlElement(name="enable")
    private Boolean enable;
    @XmlElement(name="retain-number-of-days")
    private Integer retainNumberOfDays;

}
