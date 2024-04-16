package com.cisco.configService.migration.wae7xConfig.nimos;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class InventoryNimo {

    @XmlElement(name="source-network", namespace = "http://cisco.com/ns/wae/inventory-nimo")
    private String sourceNetwork;

    @XmlElement(name="advanced", namespace = "http://cisco.com/ns/wae/inventory-nimo")
    private InventoryAdvanced inventoryAdvanced = new InventoryAdvanced();

    @Data
    @NoArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class InventoryAdvanced{

        @XmlElement(name="action-timeout", namespace = "http://cisco.com/ns/wae/inventory-nimo")
        private Integer actionTimeout;

        @XmlElement(name="get-inventory-options ", namespace = "http://cisco.com/ns/wae/inventory-nimo")
        private GetInventory getInventoryOptions = new GetInventory();

        @XmlElement(name="build-inventory-options", namespace = "http://cisco.com/ns/wae/inventory-nimo")
        private BuildInventory buildInventoryOptions = new BuildInventory();

    }

    @Data
    @NoArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class GetInventory{

        @XmlElement(name="login-allowed", namespace = "http://cisco.com/ns/wae/inventory-nimo")
        private Boolean loginAllowed;

        @XmlElement(name="net-recorder", namespace = "http://cisco.com/ns/wae/inventory-nimo")
        private String netRecorder;

        @XmlElement(name="verbosity", namespace = "http://cisco.com/ns/wae/inventory-nimo")
        private Integer verbosity;
    }

    @Data
    @NoArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class BuildInventory{

        @XmlElement(name="exclude-file", namespace = "http://cisco.com/ns/wae/inventory-nimo")
        private String excludeFile;

        @XmlElement(name="guess-template-if-nomatch", namespace = "http://cisco.com/ns/wae/inventory-nimo")
        private Boolean guessTemplateIfNoMatch ;

        @XmlElement(name="template-file", namespace = "http://cisco.com/ns/wae/inventory-nimo")
        private String templateFile;

        @XmlElement(name="hardware-spec-file", namespace = "http://cisco.com/ns/wae/inventory-nimo")
        private String hardwareSpecFile;

        @XmlElement(name="verbosity", namespace = "http://cisco.com/ns/wae/inventory-nimo")
        private Integer verbosity;
    }

}
