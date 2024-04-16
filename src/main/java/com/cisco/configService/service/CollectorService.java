package com.cisco.configService.service;

import com.cisco.configService.entity.Agents;
import com.cisco.configService.entity.Collector;
import com.cisco.configService.entity.NodeFilter;
import com.cisco.configService.entity.NodeProfile;
import com.cisco.configService.enums.CollectorTypes;
import com.cisco.configService.enums.IgpProtocol;
import com.cisco.configService.exception.CustomException;
import com.cisco.configService.model.ConfigParams;
import com.cisco.configService.model.common.ui.IgpConfigsView;
import com.cisco.configService.model.composer.SourceCollector;
import com.cisco.configService.model.composer.cli.CollectorData;
import com.cisco.configService.model.custom.CustomCollector;
import com.cisco.configService.model.demand.CopyDemands;
import com.cisco.configService.model.demand.ui.*;
import com.cisco.configService.model.inventory.ui.InventoryCollectorView;
import com.cisco.configService.model.layout.LayoutCollectorView;
import com.cisco.configService.model.lspSnmp.ui.LspSnmpCollectorView;
import com.cisco.configService.model.multicast.MulticastCollector;
import com.cisco.configService.model.multicast.ui.LoginFindMulticastCollectorView;
import com.cisco.configService.model.multicast.ui.LoginPollMulticastCollectorView;
import com.cisco.configService.model.multicast.ui.SnmpFindMulticastCollectorView;
import com.cisco.configService.model.multicast.ui.SnmpPollMulticastCollectorView;
import com.cisco.configService.model.netflow.NetflowCollector;
import com.cisco.configService.model.parseConfig.ui.ParseConfigCollectorView;
import com.cisco.configService.model.pcepLsp.PcepLspCollector;
import com.cisco.configService.model.preConfig.AllAgentData;
import com.cisco.configService.model.preConfig.AllNodeProfileData;
import com.cisco.configService.model.preConfig.NodeFilterData;
import com.cisco.configService.model.topoBgp.ui.BgpCollectorView;
import com.cisco.configService.model.topoBgpls.ui.BgpLsCollectorView;
import com.cisco.configService.model.topoIgp.ui.IgpCollectorView;
import com.cisco.configService.model.topoVpn.ui.VpnCollectorView;
import com.cisco.configService.model.trafficPoller.TrafficCollector;
import com.cisco.configService.repository.CollectorRepository;
import com.cisco.configService.repository.NodeFilterRepository;
import com.cisco.configService.repository.NodeProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class CollectorService {

    @Autowired
    CollectorRepository collectorRepository;

    @Autowired
    AgentService agentService;

    @Autowired
    NodeFilterRepository nodeFilterRepository;

    @Autowired
    NodeProfileRepository nodeProfileRepository;

    @Autowired
    BeanConverstionService beanConverstionService;

    public static final Long DARE_COLLECTOR_ID = 0L;

    public Optional<CollectorData> getCollector(Long id) {
        Optional<Collector> collectorOptional = collectorRepository.findById(id);
        if (collectorOptional.isEmpty()) {
            return Optional.empty();
        }

        Collector collector = collectorOptional.get();

        Optional<Long> parentCollectorIdOptional = collectorRepository.findParentCollector(collector.getId());
        Optional<Long> networkOptional;
        String planFileName = "";
        if(parentCollectorIdOptional.isPresent()) {
            Long parentCollectorId = parentCollectorIdOptional.get();
            log.info("The requested Collector has parent Collector." + parentCollectorId);
            Optional<Collector> parentCollectorOptional = collectorRepository.findById(parentCollectorId);
            if (parentCollectorOptional.isPresent()) {
                Collector parentCollector = parentCollectorOptional.get();
                planFileName = parentCollectorId + "_" + parentCollector.getType();
                //Populate source network as parent network if not available
                if(collector.getSourceCollector() == null) {
                    collector.setSourceCollector(parentCollector.getSourceCollector());
                }
            } else {
                log.error("There is no network associated with the collector.");
                throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR,
                         "Collector is not associated with any network.");
            }
            networkOptional = collectorRepository.findNetworkByCollectorId(parentCollectorId);


        } else {
            networkOptional = collectorRepository.findNetworkByCollectorId(id);
        }

        if (networkOptional.isEmpty()) {
            log.error("There is no network associated with the collector.");
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR,
                   "Collector is not associated with any network.");
        }

        Optional<NodeProfile> nodeProfileOptional = nodeProfileRepository.findNodeProfileByNetworkId(networkOptional.get());
        if (nodeProfileOptional.isEmpty()) {
            log.error("There is no node profile associated with the network.");
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Collector is not associated with any node profile.");
        }

        CollectorData collectorData = beanConverstionService.populateToolParameters(collector);
        collectorData.setPlanFileName(planFileName);

        AllNodeProfileData allNodeProfileData = new AllNodeProfileData();
        BeanUtils.copyProperties(nodeProfileOptional.get(), allNodeProfileData);
        collectorData.setNodeProfileData(allNodeProfileData);
        collectorData.setNetworkId(networkOptional.get());

        if(collector.getSourceCollector() != null) {
            SourceCollector sourceCollector = new SourceCollector();

            if(collector.getSourceCollector().equals(DARE_COLLECTOR_ID.toString())) {
                sourceCollector.setName(CollectorTypes.DARE.name());
                sourceCollector.setType(CollectorTypes.DARE);
                sourceCollector.setId(DARE_COLLECTOR_ID);
                collectorData.setSourceCollector(sourceCollector);
            } else {
                Optional<Collector> sourceCollectorOptional = collectorRepository.findById(
                        Long.parseLong(collector.getSourceCollector()));
                if (sourceCollectorOptional.isPresent()) {
                    sourceCollector.setId(sourceCollectorOptional.get().getId());
                    sourceCollector.setName(sourceCollectorOptional.get().getName());
                    sourceCollector.setType(sourceCollectorOptional.get().getType());
                    collectorData.setSourceCollector(sourceCollector);
                }
            }
        } else {
            List<NodeFilterData> nodeFilterDataList = new ArrayList<>();
            for(Long nodeFilterId: nodeProfileOptional.get().getNodeFilterIds()) {
                if (null == nodeFilterId) {
                    continue;
                }
                NodeFilterData nodeFilterData = new NodeFilterData();
                Optional<NodeFilter> nodeFilter = nodeFilterRepository.findById(nodeFilterId);

                if(nodeFilter.isEmpty()) {
                    continue;
                }

                BeanUtils.copyProperties(nodeFilter.get(), nodeFilterData);
                nodeFilterDataList.add(nodeFilterData);
            }
            collectorData.setNodeFilterDataList(nodeFilterDataList);
        }

        for(Agents agents : agentService.getAgentByCollectorId(collector.getId()) ){
            log.info("Getting Agent " + agents);
            AllAgentData allAgentData = new AllAgentData();
            BeanUtils.copyProperties(agents, allAgentData);
            collectorData.getAgents().add(allAgentData);
        }

        return Optional.of(collectorData);
    }

    public Iterable<Collector> getAllCollectors() {
        return collectorRepository.findAll();
    }

    public List<CollectorTypes> getAllCollectorTypes() {
        return Arrays.asList(CollectorTypes.values());
    }

    public String getDefaultConfigParams(CollectorTypes collectorType) {
        String parameterDetails = "";

        switch (collectorType) {
            case TOPO_BGP:
                parameterDetails = getJson(new BgpCollectorView());
                break;

            case LSP_SNMP:
                parameterDetails = getJson(new LspSnmpCollectorView());
                break;

            case TOPO_IGP:
                IgpCollectorView igpCollector = new IgpCollectorView();
                IgpConfigsView igpConfigs = new IgpConfigsView();
                igpConfigs.setIgpIndex(1);
                igpConfigs.setSeedRouter("x.x.x.x");
                igpConfigs.setIgpProtocol(IgpProtocol.ISIS.name());
                igpCollector.setIgpConfigs(List.of(igpConfigs));
                parameterDetails = getJson(igpCollector);
                break;
            case TOPO_BGPLS_XTC:
                parameterDetails = getJson(new BgpLsCollectorView());
                break;
            case LSP_PCEP_XTC:
                parameterDetails = getJson(new PcepLspCollector());
                break;
            case TOPO_VPN:
                parameterDetails = getJson(new VpnCollectorView());
                break;
            case CONFIG_PARSE:
                parameterDetails = getJson(new ParseConfigCollectorView());
                break;
            case TRAFFIC_POLL:
                parameterDetails = getJson(new TrafficCollector());
                break;
            case INVENTORY:
                parameterDetails = getJson(new InventoryCollectorView());
                break;
            case LAYOUT:
                parameterDetails = getJson(new LayoutCollectorView());
                break;
            case TRAFFIC_DEMAND:
                parameterDetails = getJson(new DemandCollectorView());
                break;
            case LOGIN_FIND_MULTICAST:
                parameterDetails = getJson(new LoginFindMulticastCollectorView());
                break;
            case LOGIN_POLL_MULTICAST:
                parameterDetails = getJson(new LoginPollMulticastCollectorView());
                break;
            case SNMP_FIND_MULTICAST:
                parameterDetails = getJson(new SnmpFindMulticastCollectorView());
                break;
            case SNMP_POLL_MULTICAST:
                parameterDetails = getJson(new SnmpPollMulticastCollectorView());
                break;
            case EXTERNAL_SCRIPT:
                parameterDetails = getJson(new CustomCollector());
                break;
            case DEMAND_MESH_CREATOR:
                parameterDetails = getJson(new DmdMeshCreatorView());
                break;
            case DEMAND_FOR_LSPS:
                parameterDetails = getJson(new DmdsForLspsView());
                break;
            case DEMAND_FOR_P2MP_LSPS:
                parameterDetails = getJson(new DmdsForP2mplspsView());
                break;
            case DEMAND_DEDUCTION:
                parameterDetails = getJson(new DemandDeductionView());
                break;
            case COPY_DEMANDS:
                parameterDetails = getJson(new CopyDemands());
                break;
            case MULTICAST:
                parameterDetails = getJson(new MulticastCollector());
                break;
            case NETFLOW:
                parameterDetails = getJson(new NetflowCollector());
                break;
        }
        return parameterDetails;
    }

    private String getJson(ConfigParams collectorParams) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String config =  mapper.writeValueAsString(collectorParams);
            log.debug("Collector Config = " + config);
            return config;
        } catch (Exception e) {
            throw new CustomException("Error forming the JSON string for collector params for collector");
        }
    }
}
