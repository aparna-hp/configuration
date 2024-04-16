package com.cisco.configService.migration.wae7xConfig.nimos;

import com.cisco.configService.model.multicast.ui.LoginPollMulticastCollectorView;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class LoginPollMulticastNimo {

    @XmlElement(name="source-network", namespace = "http://cisco.com/ns/wae/multicast-nimo")
    private String sourceNetwork;

    @XmlElement(name="advanced", namespace = "http://cisco.com/ns/wae/multicast-nimo")
    private LoginPollMulticastCollectorView advanced = new LoginPollMulticastCollectorView();
}
