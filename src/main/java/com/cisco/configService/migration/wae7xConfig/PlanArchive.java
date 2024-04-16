package com.cisco.configService.migration.wae7xConfig;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class PlanArchive {

    @XmlElement(name="archive-dir")
    private String archiveDir;
    @XmlElement(name="include-netint")
    private Boolean includeNetint;
    @XmlElement(name="cleanup")
    private Cleanup cleanup;

}
