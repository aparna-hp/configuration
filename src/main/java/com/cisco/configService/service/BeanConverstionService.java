package com.cisco.configService.service;

import com.cisco.configService.entity.Collector;
import com.cisco.configService.enums.CollectorTypes;
import com.cisco.configService.exception.CustomException;
import com.cisco.configService.migration.CollectorMigrationService;
import com.cisco.configService.model.ConfigParams;
import com.cisco.configService.model.common.IgpConfigs;
import com.cisco.configService.model.common.Interfaces;
import com.cisco.configService.model.common.LoginConfig;
import com.cisco.configService.model.common.Nodes;
import com.cisco.configService.model.common.ui.DebugView;
import com.cisco.configService.model.common.ui.IgpConfigsView;
import com.cisco.configService.model.composer.cli.CollectorData;
import com.cisco.configService.model.custom.CustomCollector;
import com.cisco.configService.model.demand.*;
import com.cisco.configService.model.demand.ui.*;
import com.cisco.configService.model.inventory.InventoryCollector;
import com.cisco.configService.model.inventory.ui.InventoryCollectorView;
import com.cisco.configService.model.layout.LayoutCollector;
import com.cisco.configService.model.layout.LayoutCollectorView;
import com.cisco.configService.model.lspSnmp.LspSnmpCollector;
import com.cisco.configService.model.lspSnmp.ui.LspSnmpCollectorView;
import com.cisco.configService.model.multicast.LoginFindMulticastCollector;
import com.cisco.configService.model.multicast.LoginPollMulticastCollector;
import com.cisco.configService.model.multicast.SnmpFindMulticastCollector;
import com.cisco.configService.model.multicast.SnmpPollMulticastCollector;
import com.cisco.configService.model.multicast.ui.LoginFindMulticastCollectorView;
import com.cisco.configService.model.multicast.ui.LoginPollMulticastCollectorView;
import com.cisco.configService.model.multicast.ui.SnmpFindMulticastCollectorView;
import com.cisco.configService.model.multicast.ui.SnmpPollMulticastCollectorView;
import com.cisco.configService.model.netflow.NetflowCollector;
import com.cisco.configService.model.parseConfig.ParseConfigCollector;
import com.cisco.configService.model.parseConfig.ui.GetConfigView;
import com.cisco.configService.model.parseConfig.ui.ParseConfigCollectorView;
import com.cisco.configService.model.parseConfig.ui.ParseConfigView;
import com.cisco.configService.model.pcepLsp.PcepLspCollector;
import com.cisco.configService.model.topoBgp.BgpCollector;
import com.cisco.configService.model.topoBgp.Debug;
import com.cisco.configService.model.topoBgp.ui.AdvancedView;
import com.cisco.configService.model.topoBgp.ui.BgpCollectorView;
import com.cisco.configService.model.topoBgpls.BgpLsCollector;
import com.cisco.configService.model.topoBgpls.ui.BgpLsCollectorView;
import com.cisco.configService.model.topoIgp.IgpCollector;
import com.cisco.configService.model.topoIgp.IgpConfigAdvanced;
import com.cisco.configService.model.topoIgp.ui.IgpCollectorView;
import com.cisco.configService.model.topoVpn.VpnCollector;
import com.cisco.configService.model.topoVpn.ui.VpnCollectorView;
import com.cisco.configService.model.trafficPoller.TrafficCollector;
import com.cisco.configService.model.trafficPoller.ui.TrafficCollectorView;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@Slf4j
public class BeanConverstionService {

    @Autowired
    CryptoService cryptoService;

    /*
    Get the tool specific configuration parameters from the entity bean.
     */
    public CollectorData populateToolParameters(Collector collectorEntity) {
        log.info("Converting the parameters of the Collector {} of type {} ", collectorEntity.getName(), collectorEntity.getType());
        log.debug("Entity parameters " + collectorEntity);
        CollectorData collectorData = new CollectorData();
        BeanUtils.copyProperties(collectorEntity, collectorData, UtilService.getNullPropertyNames(collectorEntity));

        ConfigParams collectorParams;

        ObjectMapper objectMapper = new ObjectMapper();

        final CollectorTypes collectorType = collectorData.getType();
        String params = collectorEntity.getParams();

        try {
            switch (collectorType) {
                case TOPO_IGP:
                    IgpCollectorView igpCollectorView = objectMapper.readValue(params, IgpCollectorView.class);
                    collectorParams = new IgpCollector();
                    BeanUtils.copyProperties(igpCollectorView, collectorParams);
                    populateIgpParameters((IgpCollector) collectorParams, igpCollectorView);
                    break;

                case TOPO_BGPLS_XTC:
                    BgpLsCollectorView bgpLsCollectorView = objectMapper.readValue(params, BgpLsCollectorView.class);
                    collectorParams = new BgpLsCollector();
                    BeanUtils.copyProperties(bgpLsCollectorView, collectorParams);
                    populateBgpLsParameters((BgpLsCollector) collectorParams, bgpLsCollectorView);
                    break;

                case TOPO_BGP:
                    BgpCollectorView bgpCollectorView = objectMapper.readValue(params, BgpCollectorView.class);
                    collectorParams = new BgpCollector();
                    BeanUtils.copyProperties(bgpCollectorView, collectorParams);
                    populateBgpParameters((BgpCollector) collectorParams, bgpCollectorView);
                    break;

                case LSP_PCEP_XTC:
                    collectorParams = new PcepLspCollector();
                    break;

                case TOPO_VPN:
                    VpnCollectorView vpnCollectorView = objectMapper.readValue(params, VpnCollectorView.class);
                    collectorParams = new VpnCollector();
                    BeanUtils.copyProperties(vpnCollectorView, collectorParams);
                    populateTopoVpnParameters((VpnCollector) collectorParams, vpnCollectorView);
                    break;

                case LSP_SNMP:
                    LspSnmpCollectorView lspSnmpCollectorView = objectMapper.readValue(params, LspSnmpCollectorView.class);
                    collectorParams = new LspSnmpCollector();
                    BeanUtils.copyProperties(lspSnmpCollectorView, collectorParams);
                    populateLspSnmpParameters((LspSnmpCollector) collectorParams, lspSnmpCollectorView);
                    break;

                case CONFIG_PARSE:
                    ParseConfigCollectorView parseConfigCollectorView = objectMapper.readValue(params, ParseConfigCollectorView.class);
                    collectorParams = new ParseConfigCollector();
                    BeanUtils.copyProperties(parseConfigCollectorView, collectorParams);
                    populateParseConfigParameters((ParseConfigCollector) collectorParams, parseConfigCollectorView);
                    break;

                case TRAFFIC_POLL:
                    TrafficCollectorView trafficCollectorView = objectMapper.readValue(params, TrafficCollectorView.class);
                    collectorParams = new TrafficCollector();
                    BeanUtils.copyProperties(trafficCollectorView, collectorParams);
                    populateTrafficPollerParameters((TrafficCollector) collectorParams, trafficCollectorView);
                    break;

                case INVENTORY:
                    InventoryCollectorView inventoryCollectorView = objectMapper.readValue(params, InventoryCollectorView.class);
                    collectorParams = new InventoryCollector();
                    BeanUtils.copyProperties(inventoryCollectorView, collectorParams);
                    populateInventoryParameters((InventoryCollector) collectorParams, inventoryCollectorView);
                    break;

                case LOGIN_FIND_MULTICAST:
                    LoginFindMulticastCollectorView loginFindMulticastCollectorView = objectMapper.readValue(params, LoginFindMulticastCollectorView.class);
                    collectorParams = new LoginFindMulticastCollector();
                    BeanUtils.copyProperties(loginFindMulticastCollectorView, collectorParams);
                    populateLoginFindMcParameters((LoginFindMulticastCollector)collectorParams,loginFindMulticastCollectorView);
                    break;

                case LOGIN_POLL_MULTICAST:
                    LoginPollMulticastCollectorView loginPollMulticastCollectorView = objectMapper.readValue(params, LoginPollMulticastCollectorView.class);
                    collectorParams = new LoginPollMulticastCollector();
                    BeanUtils.copyProperties(loginPollMulticastCollectorView, collectorParams);
                    populateLoginPollMcParameters((LoginPollMulticastCollector)collectorParams, loginPollMulticastCollectorView);
                    break;

                case SNMP_FIND_MULTICAST:
                    SnmpFindMulticastCollectorView snmpFindMulticastCollectorView = objectMapper.readValue(params, SnmpFindMulticastCollectorView.class);
                    collectorParams = new SnmpFindMulticastCollector();
                    BeanUtils.copyProperties(snmpFindMulticastCollectorView, collectorParams);
                    populateSnmpFindMcParameters((SnmpFindMulticastCollector)collectorParams, snmpFindMulticastCollectorView );
                    break;

                case SNMP_POLL_MULTICAST:
                    SnmpPollMulticastCollectorView snmpPollMulticastCollectorView = objectMapper.readValue(params, SnmpPollMulticastCollectorView.class);
                    collectorParams = new SnmpPollMulticastCollector();
                    BeanUtils.copyProperties(snmpPollMulticastCollectorView, collectorParams);
                    populateSnmpPollMcParameters((SnmpPollMulticastCollector) collectorParams, snmpPollMulticastCollectorView);
                    break;

                case MULTICAST:
                    throw new CustomException(HttpStatus.NOT_IMPLEMENTED,
                             "The multicast collector configuration cannot be retrieved. " +
                                    "Please use this API to get the configuration of individual Multicast collector types like Login find Multicast, Login Poll Multicast, SNMP Find Multicast, SNMP Poll Multicast.  ");

                case LAYOUT:
                    LayoutCollectorView layoutCollectorView = objectMapper.readValue(params, LayoutCollectorView.class);
                    collectorParams = new LayoutCollector();
                    BeanUtils.copyProperties(layoutCollectorView, collectorParams);
                    populateLayoutParameters((LayoutCollector) collectorParams, layoutCollectorView);
                    break;

                case NETFLOW:
                    collectorParams = objectMapper.readValue(params, NetflowCollector.class);
                    break;

                case EXTERNAL_SCRIPT:

                    collectorParams = objectMapper.readValue(params, CustomCollector.class);
                    break;

                case DEMAND_MESH_CREATOR:
                    DmdMeshCreatorView dmdMeshCreatorView = objectMapper.readValue(params, DmdMeshCreatorView.class);
                    collectorParams = new DmdMeshCreator();
                    BeanUtils.copyProperties(dmdMeshCreatorView, collectorParams);
                    populateDemandMeshParameters((DmdMeshCreator) collectorParams, dmdMeshCreatorView);
                    break;

                case DEMAND_FOR_LSPS:
                    DmdsForLspsView dmdsForLspsView = objectMapper.readValue(params, DmdsForLspsView.class);
                    collectorParams = new DmdsForLsps();
                    BeanUtils.copyProperties(dmdsForLspsView, collectorParams);
                    populateDmdForLspsParameters((DmdsForLsps) collectorParams, dmdsForLspsView);
                    break;

                case DEMAND_FOR_P2MP_LSPS:
                    DmdsForP2mplspsView dmdsForP2mplsps = objectMapper.readValue(params, DmdsForP2mplspsView.class);
                    collectorParams = new DmdsForP2mplsps();
                    BeanUtils.copyProperties(dmdsForP2mplsps, collectorParams);
                    populateDmdForP2mpLspsParameters((DmdsForP2mplsps) collectorParams, dmdsForP2mplsps);
                    break;

                case DEMAND_DEDUCTION:
                    DemandDeductionView demandDeductionView = objectMapper.readValue(params, DemandDeductionView.class);
                    collectorParams = new DemandDeduction();
                    BeanUtils.copyProperties(demandDeductionView, collectorParams);
                    populateDmdDecutionParameters((DemandDeduction) collectorParams, demandDeductionView);
                    break;

                case COPY_DEMANDS:
                    collectorParams = objectMapper.readValue(params, CopyDemands.class);
                    break;

                case TRAFFIC_DEMAND:
                    collectorParams = objectMapper.readValue(params, DemandCollectorView.class);
                    break;

                default:
                    collectorParams = new ConfigParams();
            }

            ObjectMapper Obj = new ObjectMapper();
            // Converting the Java object into a JSON string
            String collectorParamsAsStr = Obj.writeValueAsString(collectorParams);
            log.info("Collector params = " + collectorParamsAsStr);
            collectorData.setParams(collectorParamsAsStr);

        } catch (Exception e) {
            log.error("Error generating the tool configuration ", e);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error generating the configuration for the tool: " + collectorData.getName(),
                    e.getMessage());
        }

        return collectorData;
    }

    /*
    Populate Topo Igp Collector specific configuration parameters different from view bean
     */
    private void populateIgpParameters(IgpCollector igpCollector, IgpCollectorView igpCollectorView) {
        igpCollector.setIgpConfigs(new ArrayList<>());
        //Copy Igp config debug values
        for (IgpConfigsView igpConfigsView : igpCollectorView.getIgpConfigs()) {
            IgpConfigs igpConfigs = new IgpConfigs();
            BeanUtils.copyProperties(igpConfigsView, igpConfigs);
            BeanUtils.copyProperties(igpConfigsView.getAdvanced(), igpConfigs.getAdvanced());

            IgpConfigAdvanced igpConfigAdvanced = igpConfigs.getAdvanced();
            DebugView debugView = igpConfigsView.getAdvanced().getDebug();
            decryptCredentials(igpConfigs.getAdvanced().getLoginConfig());

            igpConfigAdvanced.setNetRecorder(debugView.getNetRecorder());
            igpConfigAdvanced.setVerbosity(debugView.getVerbosity());

            igpCollector.getIgpConfigs().add(igpConfigs);
        }

        BeanUtils.copyProperties(igpCollectorView.getAdvanced().getNodes(),igpCollector.getAdvanced().getNodes());
        BeanUtils.copyProperties(igpCollectorView.getAdvanced().getInterfaces(), igpCollector.getAdvanced().getInterfaces());

        Nodes nodes = igpCollector.getAdvanced().getNodes();
        DebugView debugView = igpCollectorView.getAdvanced().getNodes().getDebug();
        nodes.setNetRecorder(debugView.getNetRecorder());
        nodes.setVerbosity(debugView.getVerbosity());

        Interfaces interfaces = igpCollector.getAdvanced().getInterfaces();
        debugView = igpCollectorView.getAdvanced().getInterfaces().getDebug();
        interfaces.setNetRecorder(debugView.getNetRecorder());
        interfaces.setVerbosity(debugView.getVerbosity());

    }

    /*
    Populate Topo Igp Collector specific configuration parameters different from view bean
     */
    private void populateBgpLsParameters(BgpLsCollector bgpLsCollector, BgpLsCollectorView bgpLsCollectorView) {
        Nodes nodes = bgpLsCollector.getAdvanced().getNodes();
        Interfaces interfaces = bgpLsCollector.getAdvanced().getInterfaces();

        BeanUtils.copyProperties(bgpLsCollectorView.getAdvanced().getNodes(), nodes);
        BeanUtils.copyProperties(bgpLsCollectorView.getAdvanced().getInterfaces(), interfaces);

        DebugView debugView = bgpLsCollectorView.getAdvanced().getNodes().getDebug();
        nodes.setNetRecorder(debugView.getNetRecorder());
        nodes.setVerbosity(debugView.getVerbosity());

        debugView = bgpLsCollectorView.getAdvanced().getInterfaces().getDebug();
        interfaces.setNetRecorder(debugView.getNetRecorder());
        interfaces.setVerbosity(debugView.getVerbosity());

    }

    /*
    Populate Topo Bgp Collector specific configuration parameters different from view bean
     */
    public void populateBgpParameters(BgpCollector bgpCollector, BgpCollectorView bgpCollectorView) {

        BeanUtils.copyProperties(bgpCollectorView.getAdvanced(), bgpCollector.getAdvanced());

        AdvancedView advancedView = bgpCollectorView.getAdvanced();
        bgpCollector.setProtocol(advancedView.getProtocol());
        bgpCollector.setMinPrefixLength(advancedView.getMinPrefixLength());
        bgpCollector.setMinIPv6PrefixLength(advancedView.getMinIPv6PrefixLength());
        bgpCollector.setLoginToRouterForMultihop(advancedView.getLoginToRouterForMultihop());
        decryptCredentials(bgpCollector.getAdvanced().getLoginConfig());
        Debug debug = new Debug();
        debug.setNetRecorder(bgpCollectorView.getAdvanced().getDebug().getNetRecorder());
        debug.setVerbosity(bgpCollectorView.getAdvanced().getDebug().getVerbosity());
        debug.setTimeout(bgpCollectorView.getAdvanced().getTimeout());
        debug.setLoginRecordMode(bgpCollectorView.getAdvanced().getDebug().getLoginRecordMode());
        bgpCollector.setDebug(debug);
    }

    /*
    Populate Lsp snmp Collector specific configuration parameters different from view bean
     */
    public void populateLspSnmpParameters(LspSnmpCollector lspSnmpCollector, LspSnmpCollectorView lspSnmpCollectorView) {

        BeanUtils.copyProperties(lspSnmpCollectorView.getAdvanced(), lspSnmpCollector.getAdvanced());

        com.cisco.configService.model.common.Debug debug = new com.cisco.configService.model.common.Debug();
        debug.setNetRecorder(lspSnmpCollectorView.getAdvanced().getDebug().getNetRecorder());
        debug.setVerbosity(lspSnmpCollectorView.getAdvanced().getDebug().getVerbosity());
        debug.setTimeout(lspSnmpCollectorView.getAdvanced().getTimeout());

        lspSnmpCollector.setDebug(debug);
    }

    /*
   Populate Topo Vpn Collector specific configuration parameters different from view bean
    */
    public void populateTopoVpnParameters(VpnCollector vpnCollector, VpnCollectorView vpnCollectorView) {
        com.cisco.configService.model.common.Debug debug = new com.cisco.configService.model.common.Debug();
        debug.setNetRecorder(vpnCollectorView.getAdvanced().getDebug().getNetRecorder());
        debug.setVerbosity(vpnCollectorView.getAdvanced().getDebug().getVerbosity());
        debug.setTimeout(vpnCollectorView.getAdvanced().getTimeout());

        vpnCollector.setDebug(debug);
    }

    /*
  Populate Parse specific configuration parameters different from view bean
   */
    public void populateParseConfigParameters(ParseConfigCollector parseConfigCollector, ParseConfigCollectorView parseConfigCollectorView) {

        GetConfigView getConfigView = parseConfigCollectorView.getGetConfig();
        BeanUtils.copyProperties(getConfigView , parseConfigCollector.getGetConfig());
        BeanUtils.copyProperties(getConfigView.getLoginConfig(),
                parseConfigCollector.getGetConfig().getLoginConfig());

        com.cisco.configService.model.parseConfig.Debug debug = new com.cisco.configService.model.parseConfig.Debug();
        debug.setNetRecorder(getConfigView.getDebug().getNetRecorder());
        debug.setVerbosity(getConfigView.getDebug().getVerbosity());
        debug.setTimeout(getConfigView.getTimeout());

        parseConfigCollector.getGetConfig().setDebug(debug);
        decryptCredentials(parseConfigCollector.getGetConfig().getLoginConfig());

        ParseConfigView parseConfigView = parseConfigCollectorView.getParseConfig();
        BeanUtils.copyProperties(parseConfigView, parseConfigCollector.getParseConfig());
        BeanUtils.copyProperties(parseConfigView.getParseConfigAdvanced(), parseConfigCollector.getParseConfig().getParseConfigAdvanced());

        parseConfigCollector.getParseConfig().getParseConfigAdvanced().setVerbosity(parseConfigView.getParseConfigAdvanced().getDebug().getVerbosity());
        parseConfigCollector.getParseConfig().getParseConfigAdvanced().setTimeout(parseConfigView.getParseConfigAdvanced().getTimeout());

    }

    /*
  Populate Inventory Collector specific configuration parameters different from view bean
   */
    public void populateInventoryParameters(InventoryCollector inventoryCollector, InventoryCollectorView inventoryCollectorView) {

        BeanUtils.copyProperties(inventoryCollectorView.getAdvanced().getBuildInventoryOptions(),
                inventoryCollector.getAdvanced().getBuildInventoryOptions());
        BeanUtils.copyProperties(inventoryCollectorView.getAdvanced().getGetInventoryOptions(),
                inventoryCollector.getAdvanced().getGetInventoryOptions());
        inventoryCollector.getAdvanced().setActionTimeout(inventoryCollectorView.getAdvanced().getActionTimeout());

        inventoryCollector.getAdvanced().getGetInventoryOptions().setNetRecorder(inventoryCollectorView.getAdvanced()
                .getGetInventoryOptions().getDebug().getNetRecorder());
        inventoryCollector.getAdvanced().getGetInventoryOptions().setVerbosity(inventoryCollectorView.getAdvanced()
                .getGetInventoryOptions().getDebug().getVerbosity());

        inventoryCollector.getAdvanced().getBuildInventoryOptions().setVerbosity(inventoryCollectorView.getAdvanced()
                .getBuildInventoryOptions().getDebug().getVerbosity());
    }

    /*
  Populate Layout Collector specific configuration parameters different from view bean
   */
    public void populateLayoutParameters(LayoutCollector layoutCollector, LayoutCollectorView layoutCollectorView) {
        layoutCollector.setConnectTimeout(layoutCollectorView.getAdvanced().getConnectTimeout());
        layoutCollector.setAdvancedToolOptions(layoutCollectorView.getAdvanced().getAdvancedToolOptions());
    }

    /*
     Populate Traffic poller Collector specific configuration parameters different from view bean
    */
    public void populateTrafficPollerParameters(TrafficCollector trafficCollector, TrafficCollectorView trafficCollectorView) {

        BeanUtils.copyProperties(trafficCollectorView.getSnmpTrafficPoller(), trafficCollector.getSnmpTrafficPoller());

        com.cisco.configService.model.common.Debug debug = new com.cisco.configService.model.common.Debug();
        debug.setTimeout(trafficCollectorView.getSnmpTrafficPoller().getTimeout());
        debug.setNetRecorder(trafficCollectorView.getSnmpTrafficPoller().getDebug().getNetRecorder());
        debug.setVerbosity(trafficCollectorView.getSnmpTrafficPoller().getDebug().getVerbosity());

        trafficCollector.getSnmpTrafficPoller().setDebug(debug);
    }

    /*
    Populate Demand mesh specific configuration parameters different from view bean
    */
    public void populateDemandMeshParameters(DmdMeshCreator dmdCollector, DmdMeshCreatorView dmdCollectorView) {
        BeanUtils.copyProperties(dmdCollectorView.getDemandMeshAdvancedView(), dmdCollector.getAdvanced());
        dmdCollector.getAdvanced().setVerbosity(dmdCollectorView.getDemandMeshAdvancedView().getDebug().getVerbosity());
    }

    /*
   Populate Demand for LSPs specific configuration parameters different from view bean
   */
    public void populateDmdForLspsParameters(DmdsForLsps dmdCollector, DmdsForLspsView dmdCollectorView) {
        BeanUtils.copyProperties(dmdCollectorView.getDemandForLspAdvancedView(), dmdCollector.getAdvanced());
        dmdCollector.getAdvanced().setVerbosity(dmdCollectorView.getDemandForLspAdvancedView().getDebug().getVerbosity());
    }

    /*
    Populate Demand for P2mp LSPs specific configuration parameters different from view bean
     */
    public void populateDmdForP2mpLspsParameters(DmdsForP2mplsps dmdCollector, DmdsForP2mplspsView dmdCollectorView) {
        BeanUtils.copyProperties(dmdCollectorView.getDemandForP2mpAdvancedView(), dmdCollector.getAdvanced());
        dmdCollector.getAdvanced().setVerbosity(dmdCollectorView.getDemandForP2mpAdvancedView().getDebug().getVerbosity());
    }

    /*
    Populate Demand Deduction specific configuration parameters different from view bean
    */
    public void populateDmdDecutionParameters(DemandDeduction dmdCollector, DemandDeductionView dmdCollectorView) {

        BeanUtils.copyProperties(dmdCollectorView.getAdvancedView(), dmdCollector.getAdvanced());
        dmdCollector.getAdvanced().setVerbosity(dmdCollectorView.getAdvancedView().getDebug().getVerbosity());
    }

    /*
   Populate Login find multicast specific configuration parameters different from view bean
   */
    public void populateLoginFindMcParameters(LoginFindMulticastCollector mcCollector, LoginFindMulticastCollectorView mcCollectorView) {
        mcCollector.setVerbosity(mcCollectorView.getDebug().getVerbosity());
        mcCollector.setLoginRecordMode(mcCollectorView.getDebug().getNetRecorder());
    }

    /*
      Populate Login Poll multicast specific configuration parameters different from view bean
      */
    public void populateLoginPollMcParameters(LoginPollMulticastCollector mcCollector, LoginPollMulticastCollectorView mcCollectorView) {
        mcCollector.setVerbosity(mcCollectorView.getDebug().getVerbosity());
        mcCollector.setLoginRecordMode(mcCollectorView.getDebug().getNetRecorder());
    }

    /*
  Populate Snmp find multicast specific configuration parameters different from view bean
  */
    public void populateSnmpFindMcParameters(SnmpFindMulticastCollector mcCollector, SnmpFindMulticastCollectorView mcCollectorView) {
        mcCollector.setVerbosity(mcCollectorView.getDebug().getVerbosity());
        mcCollector.setNetRecorder(mcCollectorView.getDebug().getNetRecorder());
    }

    /*
 Populate Snmp Poll multicast specific configuration parameters different from view bean
 */
    public void populateSnmpPollMcParameters(SnmpPollMulticastCollector mcCollector, SnmpPollMulticastCollectorView mcCollectorView) {
        mcCollector.setVerbosity(mcCollectorView.getDebug().getVerbosity());
        mcCollector.setNetRecorder(mcCollectorView.getDebug().getNetRecorder());
    }

    /*
    This method is used to decrypt the Login config telnet password.
     */
    private void decryptCredentials(LoginConfig loginConfig) {
        log.debug("Decrypting the telnet password.");
        byte[] decrypted;
        if (null != loginConfig.getEncodedTelnetPassword() && loginConfig.getEncodedTelnetPassword().length > 0) {
            decrypted = cryptoService.aesDecrypt(loginConfig.getEncodedTelnetPassword());
            if (null != decrypted) {
                loginConfig.setTelnetPassword(new String(decrypted));
            }
        }
    }

}
