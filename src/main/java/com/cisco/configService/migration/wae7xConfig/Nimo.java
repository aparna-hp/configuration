package com.cisco.configService.migration.wae7xConfig;

import com.cisco.configService.migration.wae7xConfig.nimos.*;
import com.cisco.configService.model.pcepLsp.PcepLspCollector;
import com.cisco.configService.model.topoBgpls.ui.BgpLsCollectorView;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class Nimo {

    @XmlElement(name="topo-bgp-nimo", namespace = "http://cisco.com/ns/wae/topo-bgp-nimo")
    private TopoBgpNimo topoBgpNimo;

    @XmlElement(name="topo-igp-nimo", namespace = "http://cisco.com/ns/wae/topo-igp-nimo")
    private TopoIgpNimo topoIgpNimo;

    @XmlElement(name="lsp-snmp-nimo", namespace = "http://cisco.com/ns/wae/lsp-snmp-nimo")
    private LspSnmpNimo lspSnmpNimo;

    @XmlElement(name="inventory-nimo", namespace = "http://cisco.com/ns/wae/inventory-nimo")
    private InventoryNimo inventoryNimo;

    @XmlElement(name="traffic-poll-nimo",namespace = "http://cisco.com/ns/wae/snmp-traffic-poller")
    private TrafficPollNimo trafficPollNimo;

    @XmlElement(name="traffic-demands-nimo", namespace = "http://cisco.com/ns/wae/traffic-demands-nimo")
    private TrafficDemandsNimo trafficDemandsNimo;

    @XmlElement(name="topo-bgpls-xtc-nimo", namespace = "http://cisco.com/ns/wae/xtc-topology")
    private BgpLsCollectorView topoBgpLsXtcNimo;

    @XmlElement(name="lsp-pcep-xtc-nimo", namespace = "http://cisco.com/ns/wae/lsp-pcep-xtc-nimo")
    private PcepLspCollector pcepLspCollector;

    @XmlElement(name="cfg-parse-nimo", namespace = "http://cisco.com/ns/wae/cfg-parse-nimo")
    private CfgParseNimo cfgParseNimo;

    @XmlElement(name="login-find-multicast-nimo",namespace = "http://cisco.com/ns/wae/multicast-nimo")
    private LoginFindMulticastNimo loginFindMulticastNimo;

    @XmlElement(name="login-poll-multicast-nimo", namespace = "http://cisco.com/ns/wae/multicast-nimo")
    private LoginPollMulticastNimo loginPollMulticastNimo;

    @XmlElement(name="snmp-find-multicast-nimo",namespace = "http://cisco.com/ns/wae/multicast-nimo")
    private SnmpFindMulticastNimo snmpFindMulticastNimo;

    @XmlElement(name="snmp-poll-multicast-nimo",namespace = "http://cisco.com/ns/wae/multicast-nimo")
    private SnmpPollMulticastNimo snmpPollMulticastNimo;

    @XmlElement(name="topo-vpn-nimo", namespace = "http://cisco.com/ns/wae/vpn")
    private TopoVpnNimo topoVpnNimo;

    @XmlElement(name="netflow-nimo", namespace = "http://cisco.com/ns/wae/network/nimo/netflow")
    private NetflowNimo netflowNimo;

    @XmlElement(name="external-executable-nimo", namespace = "http://cisco.com/ns/wae/external-executable")
    private ExternalExecutableNimo externalExecutableNimo;
}
