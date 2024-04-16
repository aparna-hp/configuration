package com.cisco.configService.migration;

import com.cisco.collectionService.utils.StringUtil;
import com.cisco.configService.enums.AgentTypes;
import com.cisco.configService.enums.CollectorTypes;
import com.cisco.configService.enums.IgpProtocol;
import com.cisco.configService.enums.RecordMode;
import com.cisco.configService.migration.wae7xConfig.Network;
import com.cisco.configService.migration.wae7xConfig.aggregator.Aggregator;
import com.cisco.configService.migration.wae7xConfig.aggregator.Dependency;
import com.cisco.configService.migration.wae7xConfig.aggregator.Source;
import com.cisco.configService.migration.wae7xConfig.nimos.*;
import com.cisco.configService.model.AllConfigurations;
import com.cisco.configService.model.ConfigParams;
import com.cisco.configService.model.common.Interfaces;
import com.cisco.configService.model.common.LoginConfig;
import com.cisco.configService.model.common.ui.IgpConfigsView;
import com.cisco.configService.model.common.ui.NodesView;
import com.cisco.configService.model.composer.CollectorDataView;
import com.cisco.configService.model.composer.NetworkDataView;
import com.cisco.configService.model.composer.SourceCollector;
import com.cisco.configService.model.custom.CustomCollector;
import com.cisco.configService.model.demand.ui.*;
import com.cisco.configService.model.inventory.ui.InventoryCollectorView;
import com.cisco.configService.model.lspSnmp.ui.LspSnmpCollectorView;
import com.cisco.configService.model.multicast.ui.*;
import com.cisco.configService.model.netflow.CommonConfigs;
import com.cisco.configService.model.netflow.NetflowCollector;
import com.cisco.configService.model.netflow.agent.NetflowAgent;
import com.cisco.configService.model.parseConfig.ParseConfig;
import com.cisco.configService.model.parseConfig.ParseConfigAdvanced;
import com.cisco.configService.model.parseConfig.ui.ParseConfigCollectorView;
import com.cisco.configService.model.parseConfig.ui.ParseConfigView;
import com.cisco.configService.model.pcepLsp.PcepLspCollector;
import com.cisco.configService.model.preConfig.*;
import com.cisco.configService.model.scheduler.TaskConfigData;
import com.cisco.configService.model.topoBgp.BgpCollector;
import com.cisco.configService.model.topoBgp.ui.BgpCollectorView;
import com.cisco.configService.model.topoBgpls.ui.BgpLsCollectorView;
import com.cisco.configService.model.topoIgp.ui.IgpCollectorView;
import com.cisco.configService.model.topoIgp.ui.IgpConfigAdvancedView;
import com.cisco.configService.model.topoVpn.VpnCollector;
import com.cisco.configService.model.topoVpn.ui.VpnCollectorView;
import com.cisco.configService.model.trafficPoller.ui.TrafficCollectorView;
import com.cisco.configService.service.CollectorValidationService;
import com.cisco.configService.service.UtilService;
import com.cisco.workflowmanager.ConsolidationType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.beans.FeatureDescriptor;
import java.util.*;
import java.util.stream.Stream;

@Service
@Slf4j
public class CollectorMigrationService {

    @Autowired
    CollectorValidationService collectorValidationService;

    public void migrateNetwork(List<Network> networkList, Map<String, List<NodeFilterData>> nodeFilterDataMap,
                               List<Aggregator> aggregatorList,
                               AllConfigurations allConfigurations, Map<String, String> collectorToNetworkMap,
                               Map<String, TaskConfigData> collectorTasks, Map<String, ParseConfigCollectorView> pcCollectors) {

        Map<String, NetworkDataView> networkDataMap = new HashMap<>();
        Map<String, ConsolidationType> collectorToConsolidationType = new HashMap<>();
        List<String> destinationNetworks = new ArrayList<>();
        Map<String, Network> dareToLoginFindMulticast = new HashMap<>();
        Map<String, Network> dareToLoginPollMulticast = new HashMap<>();
        Map<String, Network> dareToSnmpFindMulticast = new HashMap<>();
        Map<String, Network> dareToSnmpPollMulticast = new HashMap<>();

        Map<String, CollectorTypes> collectorNameToType = new HashMap<>();

        for (Aggregator aggregator : aggregatorList) {
            log.debug("Migrating aggregator " + aggregator);
            String networkName = StringUtil.isEmpty(aggregator.getFinalNetwork()) ?
                    aggregator.getDestination() : aggregator.getFinalNetwork();
            if (!StringUtil.isEmpty(aggregator.getDestination())) {
                destinationNetworks.add(aggregator.getDestination());
            }
            NetworkDataView networkData = new NetworkDataView();
            networkData.setName(networkName);
            networkDataMap.put(networkName, networkData);

            for (Source source : aggregator.getSources().getSource()) {
                String collectorName = source.getNetwork();
                populateInternalMaps(networkName, collectorName, source.getDirectSource(), collectorTasks, collectorToNetworkMap);
                collectorToConsolidationType.put(collectorName, ConsolidationType.DARE);
            }

            for (Dependency dependency : aggregator.getDependencies().getDependency()) {
                String collectorName = dependency.getNetwork();
                populateInternalMaps(networkName, collectorName, dependency.getDirectSource(), collectorTasks, collectorToNetworkMap);
                collectorToConsolidationType.put(collectorName, ConsolidationType.SAGE);
            }
        }

        for (Network network : networkList) {
            String collectorName = network.getName();
            String parentNetworkName = collectorToNetworkMap.get(collectorName);
            log.debug("Migrating the collector " + collectorName + " with parent Network " + parentNetworkName);
            if (null == parentNetworkName) {
                log.info("The collector {} is not associated with any dare/sage network. Skip migrating the network.", collectorName);
                continue;
            }

            if (null != network.getPlanArchive()) {
                collectorTasks.get(collectorName).setArchive(!StringUtil.isEmpty
                        (network.getPlanArchive().getArchiveDir()));
            } else {
                collectorTasks.get(collectorName).setArchive(false);
            }

            NetworkDataView parentNetworkData = networkDataMap.get(parentNetworkName);
            CollectorDataView collectorDataView = new CollectorDataView();
            collectorDataView.setName(collectorName);
            collectorDataView.setConsolidationType(collectorToConsolidationType.get(collectorName));

            if (network.getNimo().getTopoIgpNimo() != null) {
                migrateTopoIgp(network, collectorDataView, parentNetworkData, allConfigurations, nodeFilterDataMap);
            } else if (network.getNimo().getTopoBgpNimo() != null) {
                migrateTopoBgp(network, collectorDataView, parentNetworkData, destinationNetworks);
            } else if (network.getNimo().getLspSnmpNimo() != null) {
                migrateLspSnmp(network, collectorDataView, parentNetworkData, destinationNetworks);
            } else if (network.getNimo().getTopoVpnNimo() != null) {
                migrateTopoVpn(network, collectorDataView, parentNetworkData, destinationNetworks);
            } else if (network.getNimo().getInventoryNimo() != null) {
                migrateInventory(network, collectorDataView, parentNetworkData, destinationNetworks);
            } else if (network.getNimo().getTrafficPollNimo() != null) {
                migrateTP(network, collectorDataView, parentNetworkData, destinationNetworks);
            } else if (network.getNimo().getTrafficDemandsNimo() != null) {
                migrateDemand(network, collectorDataView, parentNetworkData, destinationNetworks);
            } else if (network.getNimo().getTopoBgpLsXtcNimo() != null) {
                migrateBgpLs(network, collectorDataView, parentNetworkData, allConfigurations, nodeFilterDataMap);
            } else if (network.getNimo().getPcepLspCollector() != null) {
                migratePcepLsp(network, collectorDataView, parentNetworkData, destinationNetworks, allConfigurations);
            } else if (network.getNimo().getCfgParseNimo() != null) {
                migratePc(network, collectorDataView, parentNetworkData, destinationNetworks, pcCollectors);
            } else if (network.getNimo().getNetflowNimo() != null) {
                migrateNetflow(network, collectorDataView, parentNetworkData, destinationNetworks, allConfigurations);
            } else if (network.getNimo().getExternalExecutableNimo() != null) {
                migrateExternalScript(network,collectorDataView,parentNetworkData,destinationNetworks);
            }else if (network.getNimo().getLoginFindMulticastNimo() != null) {
                if (StringUtil.isEmpty(network.getNimo().getLoginFindMulticastNimo().getSourceNetwork())) {
                    log.error("The source network is missing for login find Multicast collector. Skip processing the collector " + collectorName);
                    continue;
                }
                dareToLoginFindMulticast.put(parentNetworkName, network);
            } else if (network.getNimo().getLoginPollMulticastNimo() != null) {
                if (StringUtil.isEmpty(network.getNimo().getLoginPollMulticastNimo().getSourceNetwork())) {
                    log.error("The source network is missing for login find Multicast collector. Skip processing the collector " + collectorName);
                    continue;
                }
                dareToLoginPollMulticast.put(parentNetworkName, network);
            } else if (network.getNimo().getSnmpFindMulticastNimo() != null) {
                if (StringUtil.isEmpty(network.getNimo().getSnmpFindMulticastNimo().getSourceNetwork())) {
                    log.error("The source network is missing for Snmp find Multicast collector. Skip processing the collector " + collectorName);
                    continue;
                }
                dareToSnmpFindMulticast.put(parentNetworkName, network);
            } else if (network.getNimo().getSnmpPollMulticastNimo() != null) {
                if (StringUtil.isEmpty(network.getNimo().getSnmpPollMulticastNimo().getSourceNetwork())) {
                    log.error("The source network is missing for Snmp poll Multicast collector. Skip processing the collector " + collectorName);
                    continue;
                }
                dareToSnmpPollMulticast.put(parentNetworkName, network);
            }

            if (null != collectorDataView.getType()) {
                collectorNameToType.put(collectorName, collectorDataView.getType());
            }
        }

        migrateMulticastForAllNetworks(dareToSnmpFindMulticast, dareToSnmpPollMulticast, dareToLoginFindMulticast, dareToLoginPollMulticast,
                networkDataMap, destinationNetworks, collectorToConsolidationType, collectorNameToType);

        allConfigurations.setNetworkDataList(new ArrayList<>(networkDataMap.values()));
    }

    private void populateInternalMaps(String networkName, String collectorName, Boolean isDirect,
                                      Map<String, TaskConfigData> collectorTasks,
                                      Map<String, String> collectorToNetworkMap) {
        log.debug("Populating the map for source collector {} belonging to network {} ", collectorName, networkName);

        TaskConfigData taskConfigData = new TaskConfigData();
        taskConfigData.setTaskName(collectorName);
        taskConfigData.setCollectorName(collectorName);
        if (null == isDirect || isDirect) {
            taskConfigData.setAggreagate(true);
        }
        taskConfigData.setCollect(true);
        collectorTasks.put(collectorName, taskConfigData);

        collectorToNetworkMap.put(collectorName, networkName);
    }

    private void populateValidatedCollectorParams(CollectorDataView collectorDataView,
                                                  NetworkDataView networkData, ConfigParams collectorParameters, boolean... validate) {
        try {
            String params = new ObjectMapper().writeValueAsString(collectorParameters);
            collectorDataView.setParams(params);
            if (validate != null && validate.length > 0 && validate[0]) {
                collectorValidationService.validateCollectorParams(collectorDataView, true);
            }
            networkData.getCollectors().add(collectorDataView);
        } catch (JsonProcessingException e) {
            log.error("Error migrating Nimo " + collectorDataView.getName() + " of type " + collectorDataView.getType(), e);
        }
    }

    public void migrateTopoIgp(Network network, CollectorDataView collectorDataView,
                               NetworkDataView parentNetworkDataView, AllConfigurations allConfigurations,
                               Map<String, List<NodeFilterData>> nodeFilterDataMap) {
        TopoIgpNimo topoIgpNimo = network.getNimo().getTopoIgpNimo();
        log.debug("Migrating collector " + topoIgpNimo);

        List<IgpConfig> igpConfigList = topoIgpNimo.getIgpConfig();
        if (igpConfigList.size() == 0) {
            log.error("The Topo Igp Nimo is missing the mandatory igp configs configuration. " +
                    "Skip processing the Nimo.");
            return;
        }
        collectorDataView.setType(CollectorTypes.TOPO_IGP);

        IgpCollectorView igpCollectorView = new IgpCollectorView();

        setNodeProfile(topoIgpNimo.getNetworkAccess(), topoIgpNimo.getNodeFilter(),
                nodeFilterDataMap, parentNetworkDataView, allConfigurations);

        List<IgpConfigsView> igpConfigDataList = new ArrayList<>();

        for (IgpConfig igpConfig : igpConfigList) {
            IgpConfigsView igpConfigData = new IgpConfigsView();
            BeanUtils.copyProperties(igpConfig, igpConfigData);

            //igpConfigData.setIgpIndex(igpConfig.getIndex());
            IgpConfigAdvancedView igpConfigAdvancedData = new IgpConfigAdvancedView();
            IgpConfig.IgpConfigAdvanced igpConfigAdvanced =
                    igpConfig.getAdvanced();
            BeanUtils.copyProperties(igpConfigAdvanced, igpConfigAdvancedData);

            LoginConfig loginConfig = new LoginConfig();
            BeanUtils.copyProperties(igpConfigAdvanced, loginConfig, UtilService.getNullPropertyNames(igpConfigAdvanced));
            igpConfigAdvancedData.setLoginConfig(loginConfig);

            igpConfigData.setAdvanced(igpConfigAdvancedData);
            String netRecorder = igpConfig.getAdvanced().getNetRecorder();
            if (!StringUtil.isEmpty(netRecorder)) {
                igpConfigData.getAdvanced().getDebug().setNetRecorder(RecordMode.valueOf(netRecorder.toUpperCase(Locale.ROOT)));
            }

            Integer verbosity = igpConfig.getAdvanced().getVerbosity();
            if (null != verbosity) {
                igpConfigData.getAdvanced().getDebug().setVerbosity(verbosity);
            }
            log.debug("Adding IgpConfig " + igpConfigData);
            igpConfigDataList.add(igpConfigData);
        }

        igpCollectorView.setIgpConfigs(igpConfigDataList);
        BeanUtils.copyProperties(topoIgpNimo.getIgpAdvanced().getNodes(), igpCollectorView.getAdvanced().getNodes());

        for (TopoIgpNimo.IgpAdvanced.Nodes.RemoveNodeSuffix removeNodeSuffix : topoIgpNimo.getIgpAdvanced().getNodes().getRemoveNodeSuffix()) {
            igpCollectorView.getAdvanced().getNodes().getRemoveNodeSuffix().add(removeNodeSuffix.getSuffix());
        }
        Optional.ofNullable(topoIgpNimo.getIgpAdvanced().getNodes().getVerbosity()).ifPresent(value ->
                igpCollectorView.getAdvanced().getNodes().getDebug().setVerbosity(value));
        Optional.ofNullable(topoIgpNimo.getIgpAdvanced().getNodes().getNetRecorder()).ifPresent(value ->
                igpCollectorView.getAdvanced().getNodes().getDebug().setNetRecorder(RecordMode.valueOf(value.toUpperCase(Locale.ROOT))));

        BeanUtils.copyProperties(topoIgpNimo.getIgpAdvanced().getInterfaces(), igpCollectorView.getAdvanced().getInterfaces());

        String ipGuessing = topoIgpNimo.getIgpAdvanced().getInterfaces().getIpGuessing();
        if (!StringUtil.isEmpty(ipGuessing)) {
            igpCollectorView.getAdvanced().getInterfaces().setIpGuessing(Interfaces.IpGuessing.valueOf(ipGuessing.toUpperCase(Locale.ROOT)));
        }

        String lagPortMatch = topoIgpNimo.getIgpAdvanced().getInterfaces().getLagPortMatch();
        if (!StringUtil.isEmpty(lagPortMatch)) {
            igpCollectorView.getAdvanced().getInterfaces().setLagPortMatch(Interfaces.LagPortMatch.valueOf(lagPortMatch.toUpperCase(Locale.ROOT)));
        }

        Integer verbosity = topoIgpNimo.getIgpAdvanced().getInterfaces().getVerbosity();
        if (null != verbosity) {
            igpCollectorView.getAdvanced().getInterfaces().getDebug().setVerbosity(verbosity);
        }

        String netRecorder = topoIgpNimo.getIgpAdvanced().getInterfaces().getNetRecorder();
        if (!StringUtil.isEmpty(netRecorder)) {
            igpCollectorView.getAdvanced().getInterfaces().getDebug().setNetRecorder(RecordMode.valueOf(netRecorder.toUpperCase(Locale.ROOT)));
        }
        populateValidatedCollectorParams(collectorDataView, parentNetworkDataView, igpCollectorView);

        log.debug("After migration igp view " + igpCollectorView);

    }

    public void migrateTopoBgp(Network network, CollectorDataView collectorDataView,
                               NetworkDataView parentNetworkDataView, List<String> destinationNetworks) {
        TopoBgpNimo topoBgpNimo = network.getNimo().getTopoBgpNimo();

        if (StringUtil.isEmpty(topoBgpNimo.getSourceNetwork())) {
            log.error("The source network is missing for topo bgp collector. Skip processing the collector {}",
                    collectorDataView.getName());
            return;
        }
        log.debug("Migrating collector " + topoBgpNimo);

        collectorDataView.setType(CollectorTypes.TOPO_BGP);
        SourceCollector sourceCollector = new SourceCollector();
        sourceCollector.setName(topoBgpNimo.getSourceNetwork());
        if (destinationNetworks.contains(sourceCollector.getName())) {
            sourceCollector.setType(CollectorTypes.DARE);
        }
        collectorDataView.setSourceCollector(sourceCollector);

        BgpCollectorView bgpCollectorView = new BgpCollectorView();
        BeanUtils.copyProperties(topoBgpNimo, bgpCollectorView.getAdvanced());
        BeanUtils.copyProperties(topoBgpNimo.getBgpAdvanced(), bgpCollectorView.getAdvanced());

        for (PeerProtocol peerProtocol : topoBgpNimo.getPeerProtocol()) {
            bgpCollectorView.getAdvanced().getProtocol().add(BgpCollector.BgpProtocol.valueOf(peerProtocol.getProtocol()));
        }

        LoginConfig loginConfig = new LoginConfig();
        BeanUtils.copyProperties(topoBgpNimo.getBgpAdvanced(), loginConfig, UtilService.getNullPropertyNames(topoBgpNimo.getBgpAdvanced()));
        bgpCollectorView.getAdvanced().setLoginConfig(loginConfig);

        String loginRecordMode = topoBgpNimo.getBgpAdvanced().getBgpDebug().getLoginRecordMode();
        if (null != loginRecordMode) {
            bgpCollectorView.getAdvanced().getDebug().setNetRecorder(RecordMode.valueOf(loginRecordMode.toUpperCase(Locale.ROOT)));
        }

        String netRecordMode = topoBgpNimo.getBgpAdvanced().getBgpDebug().getNetRecorder();
        if (!StringUtil.isEmpty(netRecordMode)) {
            bgpCollectorView.getAdvanced().getDebug().setNetRecorder(RecordMode.valueOf(netRecordMode.toUpperCase(Locale.ROOT)));
        }

        populateValidatedCollectorParams(collectorDataView, parentNetworkDataView, bgpCollectorView);

        log.debug("After migration Bgp view " + bgpCollectorView);
    }

    public void migrateLspSnmp(Network network, CollectorDataView collectorDataView,
                               NetworkDataView parentNetworkDataView, List<String> destinationNetworks) {
        LspSnmpNimo lspSnmpNimo = network.getNimo().getLspSnmpNimo();

        if (StringUtil.isEmpty(lspSnmpNimo.getSourceNetwork())) {
            log.error("The source network is missing for lsp snmp collector. Skip processing the collector {}",
                    collectorDataView.getName());
            return;
        }
        log.debug("Migrating collector " + lspSnmpNimo);

        collectorDataView.setType(CollectorTypes.LSP_SNMP);
        SourceCollector sourceCollector = new SourceCollector();
        sourceCollector.setName(lspSnmpNimo.getSourceNetwork());
        if (destinationNetworks.contains(sourceCollector.getName())) {
            sourceCollector.setType(CollectorTypes.DARE);
        }
        collectorDataView.setSourceCollector(sourceCollector);

        LspSnmpCollectorView lspSnmpCollectorView = new LspSnmpCollectorView();
        BeanUtils.copyProperties(lspSnmpNimo, lspSnmpCollectorView);
        BeanUtils.copyProperties(lspSnmpNimo.getLspSnmpAdvanced(), lspSnmpCollectorView.getAdvanced());

        String netRecordMode = lspSnmpNimo.getLspSnmpAdvanced().getLspDebug().getNetRecorder();
        if (!StringUtil.isEmpty(netRecordMode)) {
            lspSnmpCollectorView.getAdvanced().getDebug().setNetRecorder(RecordMode.valueOf(netRecordMode.toUpperCase(Locale.ROOT)));
        }

        Integer verbosity = lspSnmpNimo.getLspSnmpAdvanced().getVerbosity();
        if (null != verbosity) {
            lspSnmpCollectorView.getAdvanced().getDebug().setVerbosity(verbosity);
        }
        populateValidatedCollectorParams(collectorDataView, parentNetworkDataView, lspSnmpCollectorView);

        log.debug("After migration lsp view " + lspSnmpCollectorView);

    }

    public void migrateTopoVpn(Network network, CollectorDataView collectorDataView,
                               NetworkDataView parentNetworkDataView, List<String> destinationNetworks) {
        TopoVpnNimo topoVpnNimo = network.getNimo().getTopoVpnNimo();

        if (StringUtil.isEmpty(topoVpnNimo.getSourceNetwork())) {
            log.error("The source network is missing for lsp snmp collector. Skip processing the collector {}",
                    collectorDataView.getName());
            return;
        }
        log.debug("Migrating collector " + topoVpnNimo);

        collectorDataView.setType(CollectorTypes.TOPO_VPN);
        SourceCollector sourceCollector = new SourceCollector();
        sourceCollector.setName(topoVpnNimo.getSourceNetwork());
        if (destinationNetworks.contains(sourceCollector.getName())) {
            sourceCollector.setType(CollectorTypes.DARE);
        }
        collectorDataView.setSourceCollector(sourceCollector);

        VpnCollectorView vpnCollectorView = new VpnCollectorView();
        for (TopoVpnNimo.VpnType type : topoVpnNimo.getVpnTypes()) {
            vpnCollectorView.getVpnType().add(VpnCollector.VpnType.valueOf(type.getVpnType()));
        }

        if (null != topoVpnNimo.getVpnAdvanced().getTimeout()) {
            vpnCollectorView.getAdvanced().setTimeout(topoVpnNimo.getVpnAdvanced().getTimeout());
        }


        String netRecordMode = topoVpnNimo.getVpnAdvanced().getVpnDebug().getNetRecorder();
        if (!StringUtil.isEmpty(netRecordMode)) {
            vpnCollectorView.getAdvanced().getDebug().setNetRecorder(RecordMode.valueOf(netRecordMode.toUpperCase(Locale.ROOT)));
        }

        Integer verbosity = topoVpnNimo.getVpnAdvanced().getVerbosity();
        if (null != verbosity) {
            vpnCollectorView.getAdvanced().getDebug().setVerbosity(verbosity);
        }
        populateValidatedCollectorParams(collectorDataView, parentNetworkDataView, vpnCollectorView);

        log.debug("After migration vpn view " + vpnCollectorView);

    }

    /*
        Migrate the multicast collectors associated with all the networks.
     */
    public void migrateMulticastForAllNetworks(Map<String, Network> dareToSnmpFindMulticast,
                                               Map<String, Network> dareToSnmpPollMulticast,
                                               Map<String, Network> dareToLoginFindMulticast,
                                               Map<String, Network> dareToLoginPollMulticast,
                                               Map<String, NetworkDataView> networkDataMap,
                                               List<String> destinationNetworks,
                                               Map<String, ConsolidationType> collectorToConsolidationType,
                                               Map<String, CollectorTypes> collectorNameToType) {
        log.info("Migrate multicast collector.");
        if (dareToSnmpFindMulticast.size() > 0) {
            for (String parentNetworkName : dareToSnmpFindMulticast.keySet()) {
                migrateMulticast(dareToSnmpFindMulticast.get(parentNetworkName), dareToSnmpPollMulticast.get(parentNetworkName),
                        dareToLoginFindMulticast.get(parentNetworkName), dareToLoginPollMulticast.get(parentNetworkName), networkDataMap.get(parentNetworkName),
                        destinationNetworks, collectorToConsolidationType, collectorNameToType);
                dareToSnmpPollMulticast.remove(parentNetworkName);
                dareToLoginFindMulticast.remove(parentNetworkName);
                dareToLoginPollMulticast.remove(parentNetworkName);
            }
        }

        if (dareToSnmpPollMulticast.size() > 0) {
            for (String parentNetworkName : dareToSnmpPollMulticast.keySet()) {
                migrateMulticast(null, dareToSnmpPollMulticast.get(parentNetworkName),
                        dareToLoginFindMulticast.get(parentNetworkName), dareToLoginPollMulticast.get(parentNetworkName), networkDataMap.get(parentNetworkName),
                        destinationNetworks, collectorToConsolidationType, collectorNameToType);
                dareToLoginFindMulticast.remove(parentNetworkName);
                dareToLoginPollMulticast.remove(parentNetworkName);
            }
        }

        if (dareToLoginFindMulticast.size() > 0) {
            for (String parentNetworkName : dareToLoginFindMulticast.keySet()) {
                migrateMulticast(null, null,
                        dareToLoginFindMulticast.get(parentNetworkName), dareToLoginPollMulticast.get(parentNetworkName), networkDataMap.get(parentNetworkName),
                        destinationNetworks, collectorToConsolidationType, collectorNameToType);
                dareToLoginPollMulticast.remove(parentNetworkName);
            }
        }

        if (dareToLoginPollMulticast.size() > 0) {
            for (String parentNetworkName : dareToLoginPollMulticast.keySet()) {
                migrateMulticast(null, null, null, dareToLoginPollMulticast.get(parentNetworkName),
                        networkDataMap.get(parentNetworkName), destinationNetworks, collectorToConsolidationType, collectorNameToType);
            }
        }
    }

    /**
     * Migrate the different types of multicast nimo into a single collector.
     *
     * @param loginFindNetwork      Login Find Multicast Nimo
     * @param loginPollNetwork      Login Poll Multicast Nimo
     * @param snmpFindNetwork       Snmp Find Multicast Nimo
     * @param snmpPollNetwork       Snmp Poll Multicast Nimo
     * @param parentNetworkDataView Parent Network
     * @param destinationNetworks   List of dare network names
     * @param collectorTypesMap     Collector name to Type mapping
     */
    public void migrateMulticast(Network snmpFindNetwork, Network snmpPollNetwork, Network loginFindNetwork, Network loginPollNetwork,
                                 NetworkDataView parentNetworkDataView, List<String> destinationNetworks,
                                 Map<String, ConsolidationType> collectorToConsolidationType, Map<String, CollectorTypes> collectorTypesMap) {

        CollectorDataView collectorDataView = new CollectorDataView();
        String mcCollectorName = parentNetworkDataView.getName() + "_" + CollectorTypes.MULTICAST;
        collectorDataView.setName(mcCollectorName);
        collectorDataView.setType(CollectorTypes.MULTICAST);

        MulticastCollectorView multicastCollectorView = new MulticastCollectorView();

        if (null != snmpFindNetwork) {
            SnmpFindMulticastNimo snmpFindMulticastNimo = snmpFindNetwork.getNimo().getSnmpFindMulticastNimo();

            log.debug("Migrating snmp find collector " + snmpFindMulticastNimo);
            String sourceNetworkName = snmpFindMulticastNimo.getSourceNetwork();
            setMulticastSourceNetwork(snmpFindNetwork.getName(), sourceNetworkName, collectorDataView, destinationNetworks,
                    collectorToConsolidationType, collectorTypesMap);

            SnmpFindMulticastCollectorView snmpFindMulticastCollectorView = new SnmpFindMulticastCollectorView();
            BeanUtils.copyProperties(snmpFindMulticastNimo.getAdvanced(), snmpFindMulticastCollectorView);

            String netRecordMode = snmpFindMulticastNimo.getAdvanced().getRecordMode();
            if (!StringUtil.isEmpty(netRecordMode)) {
                snmpFindMulticastCollectorView.getDebug().setNetRecorder(RecordMode.valueOf(netRecordMode.toUpperCase(Locale.ROOT)));
            }

            log.debug("After migration Snmp find Multicast view " + snmpFindMulticastCollectorView);

            multicastCollectorView.setSnmpFindMulticastCollector(snmpFindMulticastCollectorView);
        }

        if (null != snmpPollNetwork) {
            SnmpPollMulticastNimo snmpPollMulticastNimo = snmpPollNetwork.getNimo().getSnmpPollMulticastNimo();

            log.debug("Migrating snmp poll collector " + snmpPollMulticastNimo);
            String sourceNetworkName = snmpPollMulticastNimo.getSourceNetwork();
            setMulticastSourceNetwork(snmpPollNetwork.getName(), sourceNetworkName, collectorDataView, destinationNetworks,
                    collectorToConsolidationType, collectorTypesMap);


            SnmpPollMulticastCollectorView snmpFindMulticastCollectorView = new SnmpPollMulticastCollectorView();
            BeanUtils.copyProperties(snmpPollMulticastNimo.getAdvanced(), snmpFindMulticastCollectorView);

            String netRecordMode = snmpPollMulticastNimo.getAdvanced().getRecordMode();
            if (!StringUtil.isEmpty(netRecordMode)) {
                snmpFindMulticastCollectorView.getDebug().setNetRecorder(RecordMode.valueOf(netRecordMode.toUpperCase(Locale.ROOT)));
            }

            log.debug("After migration snmp poll Multicast view " + snmpFindMulticastCollectorView);

            multicastCollectorView.setSnmpPollMulticastCollector(snmpFindMulticastCollectorView);
        }

        if (null != loginFindNetwork) {
            LoginFindMulticastNimo loginFindMulticastNimo = loginFindNetwork.getNimo().getLoginFindMulticastNimo();

            log.debug("Migrating login find collector " + loginFindMulticastNimo);
            String sourceNetworkName = loginFindMulticastNimo.getSourceNetwork();
            setMulticastSourceNetwork(loginFindNetwork.getName(), sourceNetworkName, collectorDataView, destinationNetworks,
                    collectorToConsolidationType, collectorTypesMap);

            LoginFindMulticastCollectorView loginFindMulticastCollectorView = new LoginFindMulticastCollectorView();
            BeanUtils.copyProperties(loginFindMulticastNimo.getAdvanced(), loginFindMulticastCollectorView);

            String netRecordMode = loginFindMulticastNimo.getAdvanced().getLoginRecordMode();
            if (!StringUtil.isEmpty(netRecordMode)) {
                loginFindMulticastCollectorView.getDebug().setNetRecorder(RecordMode.valueOf(netRecordMode.toUpperCase(Locale.ROOT)));
            }

            log.debug("After migration Login find Multicast view " + loginFindMulticastCollectorView);

            multicastCollectorView.setLoginFindMulticastCollector(loginFindMulticastCollectorView);

        }

        if (null != loginPollNetwork) {
            LoginPollMulticastNimo loginPollMulticastNimo = loginPollNetwork.getNimo().getLoginPollMulticastNimo();

            log.debug("Migrating login poll collector " + loginPollMulticastNimo);
            String sourceNetworkName = loginPollMulticastNimo.getSourceNetwork();
            setMulticastSourceNetwork(loginPollNetwork.getName(), sourceNetworkName, collectorDataView, destinationNetworks,
                    collectorToConsolidationType, collectorTypesMap);

            LoginPollMulticastCollectorView loginFindMulticastCollectorView = new LoginPollMulticastCollectorView();
            BeanUtils.copyProperties(loginPollMulticastNimo.getAdvanced(), loginFindMulticastCollectorView);

            String netRecordMode = loginPollMulticastNimo.getAdvanced().getLoginRecordMode();
            if (!StringUtil.isEmpty(netRecordMode)) {
                loginFindMulticastCollectorView.getDebug().setNetRecorder(RecordMode.valueOf(netRecordMode.toUpperCase(Locale.ROOT)));
            }

            log.debug("After migration Login Poll Multicast view " + loginFindMulticastCollectorView);

            multicastCollectorView.setLoginPollMulticastCollector(loginFindMulticastCollectorView);
        }

        populateValidatedCollectorParams(collectorDataView, parentNetworkDataView, multicastCollectorView);

        //Update the source network name with type = multicast of other collectors
        for (CollectorDataView collector : parentNetworkDataView.getCollectors()) {
            SourceCollector sourceCollector = collector.getSourceCollector();
            if (null != sourceCollector && !collectorTypesMap.containsKey(sourceCollector.getName())) {
                if (null == sourceCollector.getType() || !sourceCollector.getType().equals(CollectorTypes.DARE)) {
                    log.debug("The source collector is a multicast collectors which is not in the map. " +
                            "Update the source collector name as {} for collector {}.", mcCollectorName, collector);
                    sourceCollector.setName(mcCollectorName);
                }
            }
        }
    }

    public void migrateInventory(Network network, CollectorDataView collectorDataView,
                                 NetworkDataView parentNetworkDataView, List<String> destinationNetworks) {
        InventoryNimo inventoryNimo = network.getNimo().getInventoryNimo();

        if (StringUtil.isEmpty(inventoryNimo.getSourceNetwork())) {
            log.error("The source network is missing for inventory collector. Skip processing the collector {}",
                    collectorDataView.getName());
            return;
        }
        log.debug("Migrating Inv collector " + inventoryNimo);

        collectorDataView.setType(CollectorTypes.INVENTORY);
        SourceCollector sourceCollector = new SourceCollector();
        sourceCollector.setName(inventoryNimo.getSourceNetwork());
        if (destinationNetworks.contains(sourceCollector.getName())) {
            sourceCollector.setType(CollectorTypes.DARE);
        }
        collectorDataView.setSourceCollector(sourceCollector);

        InventoryCollectorView inventoryCollectorView = new InventoryCollectorView();
        Optional.ofNullable(inventoryNimo.getInventoryAdvanced().getActionTimeout()).ifPresent(value ->
                inventoryCollectorView.getAdvanced().setActionTimeout(value));
        Optional.ofNullable(inventoryNimo.getInventoryAdvanced().getGetInventoryOptions().getLoginAllowed()).ifPresent(value ->
                inventoryCollectorView.getAdvanced().getGetInventoryOptions().setLoginAllowed(value));
        String netRecordMode = inventoryNimo.getInventoryAdvanced().getGetInventoryOptions().getNetRecorder();
        if (!StringUtil.isEmpty(netRecordMode)) {
            inventoryCollectorView.getAdvanced().getGetInventoryOptions().getDebug().setNetRecorder(
                    RecordMode.valueOf(netRecordMode.toUpperCase(Locale.ROOT)));
        }

        Integer verbosity = inventoryNimo.getInventoryAdvanced().getGetInventoryOptions().getVerbosity();
        if (null != verbosity) {
            inventoryCollectorView.getAdvanced().getGetInventoryOptions().getDebug().setVerbosity(verbosity);
        }

        BeanUtils.copyProperties(inventoryNimo.getInventoryAdvanced().getBuildInventoryOptions(),
                inventoryCollectorView.getAdvanced().getBuildInventoryOptions());

        verbosity = inventoryNimo.getInventoryAdvanced().getBuildInventoryOptions().getVerbosity();
        if (null != verbosity) {
            inventoryCollectorView.getAdvanced().getBuildInventoryOptions().getDebug().setVerbosity(verbosity);
        }

        populateValidatedCollectorParams(collectorDataView, parentNetworkDataView, inventoryCollectorView);

        log.debug("After migration Inventory view " + inventoryCollectorView);

    }

    public void migrateTP(Network network, CollectorDataView collectorDataView,
                          NetworkDataView parentNetworkDataView, List<String> destinationNetworks) {
        TrafficPollNimo trafficPollNimo = network.getNimo().getTrafficPollNimo();

        if (StringUtil.isEmpty(trafficPollNimo.getSourceNetwork())) {
            log.error("The source network is missing for traffic collector. Skip processing the collector {}",
                    collectorDataView.getName());
            return;
        }
        log.debug("Migrating TP collector " + trafficPollNimo);

        collectorDataView.setType(CollectorTypes.TRAFFIC_POLL);
        SourceCollector sourceCollector = new SourceCollector();
        sourceCollector.setName(trafficPollNimo.getSourceNetwork());
        if (destinationNetworks.contains(sourceCollector.getName())) {
            sourceCollector.setType(CollectorTypes.DARE);
        }
        collectorDataView.setSourceCollector(sourceCollector);

        TrafficCollectorView trafficCollector = new TrafficCollectorView();
        Optional.ofNullable(trafficPollNimo.getEnabled()).ifPresent(trafficCollector::setEnabled);

        BeanUtils.copyProperties(trafficPollNimo.getInterfaceTraffic(), trafficCollector.getInterfaceTraffic());
        BeanUtils.copyProperties(trafficPollNimo.getLspTraffic(), trafficCollector.getLspTraffic());
        BeanUtils.copyProperties(trafficPollNimo.getMacTraffic(), trafficCollector.getMacTraffic());
        BeanUtils.copyProperties(trafficPollNimo.getTrafficAdvanced().getSnmpTrafficPoller(), trafficCollector.getSnmpTrafficPoller());

        Integer timeout = trafficPollNimo.getTrafficAdvanced().getSnmpTrafficPoller().getTimeout();
        if (null != timeout) {
            trafficCollector.getSnmpTrafficPoller().setTimeout(timeout);
        }

        String netRecordMode = trafficPollNimo.getTrafficAdvanced().getSnmpTrafficPoller().getNetRecorder();
        if (!StringUtil.isEmpty(netRecordMode)) {
            trafficCollector.getSnmpTrafficPoller().getDebug().setNetRecorder(RecordMode.valueOf(netRecordMode.toUpperCase(Locale.ROOT)));
        }

        Integer verbosity = trafficPollNimo.getTrafficAdvanced().getSnmpTrafficPoller().getVerbosity();
        if (null != verbosity) {
            trafficCollector.getSnmpTrafficPoller().getDebug().setVerbosity(verbosity);
        }

        populateValidatedCollectorParams(collectorDataView, parentNetworkDataView, trafficCollector);

        log.debug("After migration traffic poller view " + trafficCollector);

    }

    public void migrateDemand(Network network, CollectorDataView collectorDataView,
                              NetworkDataView parentNetworkDataView, List<String> destinationNetworks) {
        TrafficDemandsNimo trafficDemandsNimo = network.getNimo().getTrafficDemandsNimo();

        if (StringUtil.isEmpty(trafficDemandsNimo.getSourceNetwork())) {
            log.error("The source network is missing for demand collector. Skip processing the collector {}",
                    collectorDataView.getName());
            return;
        }
        log.debug("Migrating collector " + trafficDemandsNimo);

        collectorDataView.setType(CollectorTypes.TRAFFIC_DEMAND);
        SourceCollector sourceCollector = new SourceCollector();
        sourceCollector.setName(trafficDemandsNimo.getSourceNetwork());
        if (destinationNetworks.contains(sourceCollector.getName())) {
            sourceCollector.setType(CollectorTypes.DARE);
        }
        collectorDataView.setSourceCollector(sourceCollector);

        DemandCollectorView demandCollectorView = new DemandCollectorView();

        List<DemandStepView> demandSteps = new ArrayList<>();
        if (null != trafficDemandsNimo.getDemandMeshConfig()) {
            for (DemandStepView demandStepView : trafficDemandsNimo.getDemandMeshConfig().getDemandMeshSteps()) {
                if (demandStepView.getTool().getToolType().equals(CollectorTypes.DEMAND_MESH_CREATOR)) {
                    DmdMeshCreatorView dmdMeshCreatorView = demandStepView.getTool().getDmdMeshCreator();
                    demandStepView.setEnabled(dmdMeshCreatorView.getEnabled());

                    DemandMeshAdvancedView demandMeshAdvancedView = dmdMeshCreatorView.getDemandMeshAdvancedView();
                    Debug debug = new Debug();
                    debug.setVerbosity(demandMeshAdvancedView.getVerbosity());
                    demandMeshAdvancedView.setDebug(debug);
                } else if (demandStepView.getTool().getToolType().equals(CollectorTypes.DEMAND_FOR_LSPS)) {
                    DmdsForLspsView dmdsForLspsView = demandStepView.getTool().getDmdsForLsps();
                    demandStepView.setEnabled(dmdsForLspsView.getEnabled());

                    DemandForLspAdvancedView demandForLspAdvancedView = dmdsForLspsView.getDemandForLspAdvancedView();
                    Debug debug = new Debug();
                    debug.setVerbosity(demandForLspAdvancedView.getVerbosity());
                    demandForLspAdvancedView.setDebug(debug);
                } else if (demandStepView.getTool().getToolType().equals(CollectorTypes.DEMAND_FOR_P2MP_LSPS)) {
                    DmdsForP2mplspsView dmdsForP2mplsps = demandStepView.getTool().getDmdsForP2mplsps();
                    demandStepView.setEnabled(dmdsForP2mplsps.getEnabled());

                    DemandForP2mpAdvancedView demandForP2mpAdvancedView = dmdsForP2mplsps.getDemandForP2mpAdvancedView();
                    Debug debug = new Debug();
                    debug.setVerbosity(demandForP2mpAdvancedView.getVerbosity());
                    demandForP2mpAdvancedView.setDebug(debug);
                } else if (demandStepView.getTool().getToolType().equals(CollectorTypes.DEMAND_DEDUCTION)) {
                    DemandDeductionView demandDeduction = demandStepView.getTool().getDemandDeduction();
                    demandStepView.setEnabled(demandDeduction.getEnabled());

                    DemandDeductionAdvancedView advancedView = demandDeduction.getAdvancedView();
                    Debug debug = new Debug();
                    debug.setVerbosity(advancedView.getVerbosity());
                    advancedView.setDebug(debug);
                }
            }
            demandSteps.addAll(trafficDemandsNimo.getDemandMeshConfig().getDemandMeshSteps());
        }
        demandCollectorView.setDemandSteps(demandSteps);


        populateValidatedCollectorParams(collectorDataView, parentNetworkDataView, demandCollectorView);

        log.debug("After migration demand collector view " + demandCollectorView);

    }

    public void migrateBgpLs(Network network, CollectorDataView collectorDataView,
                             NetworkDataView parentNetworkDataView, AllConfigurations allConfigurations,
                             Map<String, List<NodeFilterData>> nodeFilterDataMap) {
        BgpLsCollectorView bgpLsCollectorView = network.getNimo().getTopoBgpLsXtcNimo();

        log.debug("Migrating bgpls collector with xtc-host {}, backup-xtc-host {} and params {}", bgpLsCollectorView.getXtcHost(),
                bgpLsCollectorView.getBackupXtcHost(), bgpLsCollectorView);

        collectorDataView.setType(CollectorTypes.TOPO_BGPLS_XTC);
        if (!associateAgentWithCollector(allConfigurations, collectorDataView, bgpLsCollectorView.getXtcHost())) {
            log.error("The primary SR PCE agent is missing for BGPLS collector. Skip processing the collector {}",
                    collectorDataView.getName());
            return;
        }

        if (!associateAgentWithCollector(allConfigurations, collectorDataView, bgpLsCollectorView.getBackupXtcHost())) {
            log.error("The secondary SR PCE agent is missing for BGPLS collector {}",
                    collectorDataView.getName());
        }

        setNodeProfile(bgpLsCollectorView.getNetworkAccess(),
                bgpLsCollectorView.getNodeFilter(), nodeFilterDataMap, parentNetworkDataView, allConfigurations);

        Optional.ofNullable(bgpLsCollectorView.getIgpProtocolStr()).ifPresent(value ->
                bgpLsCollectorView.setIgpProtocol(IgpProtocol.valueOf(value.toUpperCase(Locale.ROOT))));

        bgpLsCollectorView.setReactiveEnabled(bgpLsCollectorView.getReactiveNetwork().getEnable());

        for (NodesView.NodeSuffix nodeSuffix : bgpLsCollectorView.getAdvanced().getNodes().getRemoveNodeSuffixList()) {
            bgpLsCollectorView.getAdvanced().getNodes().getRemoveNodeSuffix().add(nodeSuffix.getSuffix());
        }

        Optional.ofNullable(bgpLsCollectorView.getAdvanced().getNodes().getNetRecorder()).ifPresent(value ->
                bgpLsCollectorView.getAdvanced().getNodes().getDebug().setNetRecorder(RecordMode.valueOf(value.toUpperCase(Locale.ROOT))));

        Optional.ofNullable(bgpLsCollectorView.getAdvanced().getNodes().getVerbosity()).ifPresent(value ->
                bgpLsCollectorView.getAdvanced().getNodes().getDebug().setVerbosity(value));

        Optional.ofNullable(bgpLsCollectorView.getAdvanced().getInterfaces().getIpGuessStr()).ifPresent(value ->
                bgpLsCollectorView.getAdvanced().getInterfaces().setIpGuessing(Interfaces.IpGuessing.valueOf(value.toUpperCase(Locale.ROOT))));

        Optional.ofNullable(bgpLsCollectorView.getAdvanced().getInterfaces().getLagPortMatchStr()).ifPresent(value ->
                bgpLsCollectorView.getAdvanced().getInterfaces().setLagPortMatch(Interfaces.LagPortMatch.valueOf(value.toUpperCase(Locale.ROOT))));

        Optional.ofNullable(bgpLsCollectorView.getAdvanced().getInterfaces().getNetRecorder()).ifPresent(value ->
                bgpLsCollectorView.getAdvanced().getInterfaces().getDebug().setNetRecorder(RecordMode.valueOf(value.toUpperCase(Locale.ROOT))));

        Optional.ofNullable(bgpLsCollectorView.getAdvanced().getInterfaces().getVerbosity()).ifPresent(value ->
                bgpLsCollectorView.getAdvanced().getInterfaces().getDebug().setVerbosity(value));

        populateValidatedCollectorParams(collectorDataView, parentNetworkDataView, bgpLsCollectorView, false);

        log.debug("After migration BgpLs view " + bgpLsCollectorView);
    }

    public void migratePcepLsp(Network network, CollectorDataView collectorDataView,
                               NetworkDataView parentNetworkDataView, List<String> destinationNetworks,
                               AllConfigurations allConfigurations) {
        PcepLspCollector pcepLspCollector = network.getNimo().getPcepLspCollector();
        if (StringUtil.isEmpty(pcepLspCollector.getSourceNetwork())) {
            log.error("The source network is missing for pcep lsp collector. Skip processing the collector {}",
                    collectorDataView.getName());
            return;
        }
        log.debug("Migrating collector " + pcepLspCollector);

        collectorDataView.setType(CollectorTypes.LSP_PCEP_XTC);
        SourceCollector sourceCollector = new SourceCollector();
        sourceCollector.setName(pcepLspCollector.getSourceNetwork());
        if (destinationNetworks.contains(sourceCollector.getName())) {
            sourceCollector.setType(CollectorTypes.DARE);
        }
        collectorDataView.setSourceCollector(sourceCollector);

        boolean found = false;

        for (PcepLspCollector.SrpceAgents agent : pcepLspCollector.getSrpceAgentsList()) {
            if (!associateAgentWithCollector(allConfigurations, collectorDataView, agent.getSrpceAgent())) {
                log.error("The SR PCE agent {} associated with pcep lsp collector could not be found {}",
                        agent.getSrpceAgent(), collectorDataView.getName());
            } else {
                found = true;
            }
        }

        if (!found) {
            log.error("A valid SR PCE agent is not associated with pcep lsp collector. Skip migrating the collector {}.",
                    collectorDataView.getName());
            return;
        }

        populateValidatedCollectorParams(collectorDataView, parentNetworkDataView, pcepLspCollector, false);
    }

    public void migrateNetflow(Network network, CollectorDataView collectorDataView,
                               NetworkDataView parentNetworkDataView, List<String> destinationNetworks,
                               AllConfigurations allConfigurations) {
        NetflowNimo netflowNimo = network.getNimo().getNetflowNimo();
        if (StringUtil.isEmpty(netflowNimo.getSourceNetwork())) {
            log.error("The source network is missing for netflow collector. Skip processing the collector {}",
                    collectorDataView.getName());
            return;
        }
        log.debug("Migrating collector " + netflowNimo);

        associateNetflowAgent(allConfigurations, collectorDataView);

        collectorDataView.setType(CollectorTypes.NETFLOW);
        SourceCollector sourceCollector = new SourceCollector();
        sourceCollector.setName(netflowNimo.getSourceNetwork());
        if (destinationNetworks.contains(sourceCollector.getName())) {
            sourceCollector.setType(CollectorTypes.DARE);
        }
        collectorDataView.setSourceCollector(sourceCollector);

        NetflowCollector netflowCollector = new NetflowCollector();
        BeanUtils.copyProperties(netflowNimo.getConfig().getCommon(), netflowCollector.getCommonConfigs());
        BeanUtils.copyProperties(netflowNimo.getConfig().getIasFlows(), netflowCollector.getIASConfigs());
        BeanUtils.copyProperties(netflowNimo.getConfig().getDemands(), netflowCollector.getDemandConfigs());

        Optional.ofNullable(netflowNimo.getConfig().getCommon().getAddressFamilyStr()).ifPresent(value -> {
            String[] temp = netflowNimo.getConfig().getCommon().getAddressFamilyStr().split(",");
            for (String addressFamily : temp) {
                netflowCollector.getCommonConfigs().getAddressFamily().add(
                        CommonConfigs.AddressFamily.valueOf(addressFamily));
            }
        });

        if (!StringUtil.isEmpty(netflowNimo.getConfig().getCommon().getExtraAggregationStr())) {
            String[] temp = netflowNimo.getConfig().getCommon().getExtraAggregationStr().split(",");
            for (String aggr : temp) {
                netflowCollector.getCommonConfigs().getExtraAggregation().add(NetflowAgent.ExtraAggregation.valueOf(
                        aggr.toUpperCase(Locale.ROOT)));
            }
        }

        if (!StringUtil.isEmpty(netflowNimo.getConfig().getCommon().getExtNodeTagString())) {
            netflowCollector.getCommonConfigs().getExtNodeTags().addAll(Arrays.asList(
                    netflowNimo.getConfig().getCommon().getExtNodeTagString().split(",")));
        }

        if (!StringUtil.isEmpty(netflowNimo.getConfig().getCommon().getSplitAsFlowsOnIngressStr())) {
            netflowCollector.getCommonConfigs().setSplitAsFlowsOnIngress(CommonConfigs.SplitASFlows.valueOf(
                    netflowNimo.getConfig().getCommon().getSplitAsFlowsOnIngressStr().toUpperCase(Locale.ROOT)));
        }

        if (!StringUtil.isEmpty(netflowNimo.getConfig().getCommon().getLogLevelStr())) {
            netflowCollector.getCommonConfigs().setLogLevel(CommonConfigs.LogLevel.valueOf
                    (netflowNimo.getConfig().getCommon().getLogLevelStr()));
        }

        log.debug("After migration Netflow collector " + netflowCollector);

        populateValidatedCollectorParams(collectorDataView, parentNetworkDataView, netflowCollector, false);
    }

    public void migratePc(Network network, CollectorDataView collectorDataView,
                          NetworkDataView parentNetworkDataView, List<String> destinationNetworks,
                          Map<String, ParseConfigCollectorView> pcCollectors) {

        CfgParseNimo cfgParseNimo = network.getNimo().getCfgParseNimo();
        String pcAgent = cfgParseNimo.getCfgParseAgent();
        ParseConfigCollectorView pcCollector = new ParseConfigCollectorView();

        if (!StringUtil.isEmpty(pcAgent) && pcCollectors.containsKey(pcAgent)) {
            log.debug("Associating the get config from parse config agent.");
            pcCollector.setGetConfig(pcCollectors.get(pcAgent).getGetConfig());
        }

        if (StringUtil.isEmpty(cfgParseNimo.getSourceNetwork())) {
            log.error("The source network is missing for parse config collector. Skip processing the collector {}",
                    collectorDataView.getName());
            return;
        }

        log.debug("Migrating collector " + cfgParseNimo);

        collectorDataView.setType(CollectorTypes.CONFIG_PARSE);
        SourceCollector sourceCollector = new SourceCollector();
        sourceCollector.setName(cfgParseNimo.getSourceNetwork());
        if (destinationNetworks.contains(sourceCollector.getName())) {
            sourceCollector.setType(CollectorTypes.DARE);
        }
        collectorDataView.setSourceCollector(sourceCollector);

        ParseConfigView parseConfigView = new ParseConfigView();
        BeanUtils.copyProperties(cfgParseNimo.getParseConfig(), parseConfigView);
        BeanUtils.copyProperties(cfgParseNimo.getParseConfig().getParseConfigAdvanced(), parseConfigView.getParseConfigAdvanced());

        Optional.ofNullable(cfgParseNimo.getParseConfig().getIgpProtocol()).ifPresent(value ->
                parseConfigView.setIgpProtocol(IgpProtocol.valueOf(value.toUpperCase(Locale.ROOT))));

        if (null != cfgParseNimo.getParseConfig().getIncludeObjects()) {
            for (String includeObj : cfgParseNimo.getParseConfig().getIncludeObjects()) {
                parseConfigView.getIncludeObjects().add(ParseConfig.IncludeObject.valueOf(includeObj.toUpperCase(Locale.ROOT)));
            }
        }

        Optional.ofNullable(cfgParseNimo.getParseConfig().getParseConfigAdvanced().getCircuitMatch()).ifPresent(value ->
                parseConfigView.getParseConfigAdvanced().setCircuitMatch(ParseConfigAdvanced.CircuitMatch.valueOf(value.toUpperCase(Locale.ROOT))));

        Optional.ofNullable(cfgParseNimo.getParseConfig().getParseConfigAdvanced().getLagPortMatch()).ifPresent(value ->
                parseConfigView.getParseConfigAdvanced().setLagPortMatch(Interfaces.LagPortMatch.valueOf(value.toUpperCase(Locale.ROOT))));

        Optional.ofNullable(cfgParseNimo.getParseConfig().getParseConfigAdvanced().getOspfProcessIds()).ifPresent(value -> {
            for (String id : value.split(",")) {
                parseConfigView.getParseConfigAdvanced().getOspfProcessIds().add(Integer.parseInt(id));
            }
        });

        Optional.ofNullable(cfgParseNimo.getParseConfig().getParseConfigAdvanced().getIsisInstanceIds()).ifPresent(value -> {
            for (String id : value.split(",")) {
                parseConfigView.getParseConfigAdvanced().getIsisProcessIds().add(Integer.parseInt(id));
            }
        });

        Optional.ofNullable(cfgParseNimo.getParseConfig().getParseConfigAdvanced().getVerbosity()).ifPresent(value ->
                parseConfigView.getParseConfigAdvanced().getDebug().setVerbosity(value));

        Optional.ofNullable(cfgParseNimo.getParseConfig().getParseConfigAdvanced().getTimeout()).ifPresent(value ->
                parseConfigView.getParseConfigAdvanced().setTimeout(value));

        pcCollector.setParseConfig(parseConfigView);
        log.debug("After migration " + pcCollector);

        populateValidatedCollectorParams(collectorDataView, parentNetworkDataView, pcCollector, false);
    }

    public void migrateExternalScript(Network network, CollectorDataView collectorDataView,
                          NetworkDataView parentNetworkDataView, List<String> destinationNetworks) {

        ExternalExecutableNimo executableNimo = network.getNimo().getExternalExecutableNimo();

        if (StringUtil.isEmpty(executableNimo.getSourceNetwork())) {
            log.info("The source network is missing for external script collector.",
                    collectorDataView.getName());
            return;
        }

        log.debug("Only the external executor name and source collector if any will be migrated." + executableNimo);

        collectorDataView.setType(CollectorTypes.EXTERNAL_SCRIPT);
        SourceCollector sourceCollector = new SourceCollector();
        sourceCollector.setName(executableNimo.getSourceNetwork());
        if (destinationNetworks.contains(sourceCollector.getName())) {
            sourceCollector.setType(CollectorTypes.DARE);
        }
        collectorDataView.setSourceCollector(sourceCollector);

        CustomCollector customCollector = new CustomCollector();
        customCollector.setExecutableScript("Please upload the script.");
        populateValidatedCollectorParams(collectorDataView, parentNetworkDataView, customCollector, false);
    }


    //Sets the node profile associated with collector to the parent Network
    private void setNodeProfile(String nodeProfileName,
                                String nodeFilter, Map<String, List<NodeFilterData>> nodeFilterDataMap,
                                NetworkDataView parentNetworkDataView, AllConfigurations allConfigurations) {
        if (!StringUtil.isEmpty(nodeProfileName)) {
            log.debug("Network access assocated with collector " + nodeProfileName);
            for (NodeProfileData nodeProfileData : allConfigurations.getNodeProfileDataList()) {
                if (nodeProfileData.getName().equals(nodeProfileName)) {
                    log.info("Setting the node profile {} for the network {} associated with topo igp nimo "
                            , nodeProfileName, parentNetworkDataView.getName());
                    AllNodeProfileData allNodeProfileData = new AllNodeProfileData();
                    allNodeProfileData.setName(nodeProfileData.getName());
                    allNodeProfileData.setId(nodeProfileData.getId());
                    parentNetworkDataView.setNodeProfileData(allNodeProfileData);

                    if (!StringUtil.isEmpty(nodeFilter) && nodeFilterDataMap.containsKey(nodeFilter)) {
                        log.debug("Associating the node filter {} with the node profile {} ", nodeFilter, nodeProfileData.getName());
                        nodeProfileData.getNodeFilters().addAll(nodeFilterDataMap.get(nodeFilter));
                        log.debug("Nodeprofile with node filter " + nodeProfileData);
                    }
                }
            }
        }
    }

    /*
    Sets the source network name for the multicast collector.
     */
    private void setMulticastSourceNetwork(String networkName, String sourceNetworkName, CollectorDataView collectorDataView,
                                           List<String> destinationNetworks, Map<String, ConsolidationType> collectorToConsolidationType,
                                           Map<String, CollectorTypes> collectorTypesMap) {

        if (null == collectorDataView.getConsolidationType() && collectorToConsolidationType.containsKey(networkName)) {
            collectorDataView.setConsolidationType(collectorToConsolidationType.get(networkName));
        }

        SourceCollector sourceCollector = new SourceCollector();
        sourceCollector.setName(sourceNetworkName);

        if (null == collectorDataView.getSourceCollector()) {
            if (destinationNetworks.contains(sourceNetworkName)) {
                sourceCollector.setType(CollectorTypes.DARE);
                collectorDataView.setSourceCollector(sourceCollector);
            }
            if (collectorTypesMap.containsKey(sourceNetworkName)
                    && collectorTypesMap.get(sourceNetworkName) != CollectorTypes.MULTICAST) {
                collectorDataView.setSourceCollector(sourceCollector);
            }
        }
    }

    //Sets the agent associated the name to the parent collector.
    private boolean associateAgentWithCollector(AllConfigurations allConfigurations, CollectorDataView collectorDataView, String agentName) {
        boolean foundAgent = false;
        if (!StringUtil.isEmpty(agentName)) {
            List<AgentData> filteredList = allConfigurations.getAgentDataList().stream().filter(nodeProfileData ->
                    nodeProfileData.getName().equals(agentName)).toList();
            log.debug("Filtered agent size " + filteredList.size());
            if (filteredList.size() > 0) {
                AllAgentData allAgentData = new AllAgentData();
                BeanUtils.copyProperties(filteredList.get(0), allAgentData);
                collectorDataView.getAgents().add(allAgentData);
                foundAgent = true;
            }
        }
        return foundAgent;
    }

    private void associateNetflowAgent(AllConfigurations allConfigurations, CollectorDataView collectorDataView) {
        log.debug("Associate the netflow agent to the collector.");
        AgentData agentData = new AgentData();
        agentData.setName(AgentTypes.NETFLOW_AGENT.name());
        agentData.setType(AgentTypes.NETFLOW_AGENT);

        NetflowAgent netflowAgent = new NetflowAgent();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            agentData.setParams(objectMapper.writeValueAsString(netflowAgent));
        } catch (JsonProcessingException e) {
            log.error("Error populating the default netflow agent parameters.");
        }

        allConfigurations.getAgentDataList().add(agentData);

        AllAgentData netflowAgentInfo = new AllAgentData();
        netflowAgentInfo.setName(AgentTypes.NETFLOW_AGENT.name());
        netflowAgentInfo.setType(AgentTypes.NETFLOW_AGENT);

        collectorDataView.getAgents().add(netflowAgentInfo);
    }
}
