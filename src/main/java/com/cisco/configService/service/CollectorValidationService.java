package com.cisco.configService.service;

import com.cisco.collectionService.utils.StringUtil;
import com.cisco.configService.entity.Agents;
import com.cisco.configService.entity.Collector;
import com.cisco.configService.enums.AgentTypes;
import com.cisco.configService.enums.CollectorTypes;
import com.cisco.configService.exception.CustomException;
import com.cisco.configService.exception.ValidationService;
import com.cisco.configService.model.ConfigParams;
import com.cisco.configService.model.common.IgpConfigs;
import com.cisco.configService.model.common.LoginConfig;
import com.cisco.configService.model.common.ui.IgpConfigsView;
import com.cisco.configService.model.composer.CollectorDataView;
import com.cisco.configService.model.composer.SourceCollector;
import com.cisco.configService.model.custom.CustomCollector;
import com.cisco.configService.model.demand.CopyDemands;
import com.cisco.configService.model.demand.ui.*;
import com.cisco.configService.model.inventory.ui.InventoryCollectorView;
import com.cisco.configService.model.layout.LayoutCollectorView;
import com.cisco.configService.model.lspSnmp.ui.LspSnmpCollectorView;
import com.cisco.configService.model.multicast.ui.*;
import com.cisco.configService.model.netflow.NetflowCollector;
import com.cisco.configService.model.parseConfig.ui.ParseConfigCollectorView;
import com.cisco.configService.model.pcepLsp.PcepLspCollector;
import com.cisco.configService.model.preConfig.AgentData;
import com.cisco.configService.model.preConfig.AllAgentData;
import com.cisco.configService.model.topoBgp.ui.BgpCollectorView;
import com.cisco.configService.model.topoBgpls.ui.BgpLsCollectorView;
import com.cisco.configService.model.topoIgp.ui.IgpCollectorView;
import com.cisco.configService.model.topoVpn.ui.VpnCollectorView;
import com.cisco.configService.model.trafficPoller.ui.TrafficCollectorView;
import com.cisco.configService.repository.CollectorRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class CollectorValidationService {

    @Autowired
    ValidationService<ConfigParams> validationService;

    @Autowired
    AgentService agentService;

    @Autowired
    CollectorRepository collectorRepository;

    @Autowired
    AggregatorService aggregatorService;

    @Autowired
    CryptoService cryptoService;

    public void validateCollectorParams(CollectorDataView collectorDataView, boolean... verifyName) {
        log.info("Validating the Collector {} of type {} ", collectorDataView.getName(), collectorDataView.getType());

        if (verifyName.length > 0 ) {
            List<Collector> collectorList =  collectorRepository.findByName(collectorDataView.getName()) ;
            for(Collector collector : collectorList) {
                log.debug("Collector with name " + collector);
            }
            if(collectorList.size() > 0) {
                throw new CustomException(HttpStatus.BAD_REQUEST, "Invalid Collector Name", "Network cannot be saved." +
                        "Collector with name " + collectorDataView.getName() + " already exists !");
            }
        }

        //Validate the collector configuration parameters
        final CollectorTypes collectorType = collectorDataView.getType();
        ConfigParams formBeans;
        log.debug("Validating the parameters " + collectorDataView.getParams());
        int timeout = 0;
        try {
            switch (collectorType) {
                case TOPO_IGP -> {
                    checkForNullParams(collectorDataView);
                    formBeans = new ObjectMapper().
                            readValue(collectorDataView.getParams(), IgpCollectorView.class);
                    IgpCollectorView igpCollectorView = (IgpCollectorView) formBeans;
                    if(null !=  igpCollectorView.getIgpConfigs()) {
                        for (IgpConfigsView igpConfigsView : igpCollectorView.getIgpConfigs()) {
                            encryptCredentials(igpConfigsView.getAdvanced().getLoginConfig());
                            timeout = Integer.max(timeout, igpConfigsView.getAdvanced().getTimeout());
                        }
                    }
                    timeout = igpCollectorView.getAdvanced().getNodes().getTimeout();
                    timeout = Integer.max(timeout, igpCollectorView.getAdvanced().getInterfaces().getTimeout());
                    collectorDataView.setTimeout(timeout);
                }
                case TOPO_BGPLS_XTC -> {
                    checkForNullParams(collectorDataView);
                    Map<String, Long> agentToIdMap = validateAgentsAndGetId(collectorDataView.getAgents());
                    formBeans = new ObjectMapper().
                            readValue(collectorDataView.getParams(), BgpLsCollectorView.class);
                    BgpLsCollectorView bgpLsCollectorView = (BgpLsCollectorView) formBeans;
                    //Populate the srpce primary and secondary agent IDs in case the agent names are present
                    //in the collector config.
                    log.debug("Associating the primary agent {} and backup agent {}",
                            bgpLsCollectorView.getXtcHost(), bgpLsCollectorView.getBackupXtcHost());
                    if (agentToIdMap.size() > 0) {
                        if (!StringUtil.isEmpty(bgpLsCollectorView.getXtcHost())
                                && agentToIdMap.containsKey(bgpLsCollectorView.getXtcHost())) {
                            bgpLsCollectorView.setPrimarySrPceAgent(agentToIdMap.get(bgpLsCollectorView.getXtcHost()));
                        }

                        if (!StringUtil.isEmpty(bgpLsCollectorView.getBackupXtcHost())
                                && agentToIdMap.containsKey(bgpLsCollectorView.getBackupXtcHost())) {
                            bgpLsCollectorView.setSecondarySrPceAgent(agentToIdMap.get(bgpLsCollectorView.getBackupXtcHost()));
                        }
                    }
                    if (null == bgpLsCollectorView.getPrimarySrPceAgent()) {
                        throw new CustomException(HttpStatus.BAD_REQUEST, "Invalid collector parameters specified. The Primary SR PCE Agent" +
                                " is not present for bgpls collector " + collectorDataView.getName());
                    }
                    timeout = bgpLsCollectorView.getAdvanced().getNodes().getTimeout();
                    timeout = Integer.max(timeout, bgpLsCollectorView.getAdvanced().getInterfaces().getTimeout());
                    collectorDataView.setTimeout(timeout);
                }
                case TOPO_BGP -> {
                    formBeans = (null == collectorDataView.getParams()) ? new BgpCollectorView() :
                            new ObjectMapper().readValue(collectorDataView.getParams(), BgpCollectorView.class);
                    BgpCollectorView bgpCollectorView = (BgpCollectorView) formBeans;
                    encryptCredentials(bgpCollectorView.getAdvanced().getLoginConfig());
                    timeout = bgpCollectorView.getAdvanced().getTimeout();
                    collectorDataView.setTimeout(timeout);
                }
                case LSP_PCEP_XTC -> {
                    validateAgentsAndGetId(collectorDataView.getAgents());
                    formBeans = (null == collectorDataView.getParams()) ? new PcepLspCollector() :
                            new ObjectMapper().readValue(collectorDataView.getParams(), PcepLspCollector.class);
                    PcepLspCollector pcepLspCollector = (PcepLspCollector) formBeans;
                    timeout = pcepLspCollector.getAdvanced().getConnectTimeout();
                    collectorDataView.setTimeout(timeout);
                }
                case TOPO_VPN -> {
                    formBeans = (null == collectorDataView.getParams()) ? new VpnCollectorView() :
                            new ObjectMapper().readValue(collectorDataView.getParams(), VpnCollectorView.class);
                    VpnCollectorView vpnCollectorView = (VpnCollectorView) formBeans;
                    timeout = vpnCollectorView.getAdvanced().getTimeout();
                    collectorDataView.setTimeout(timeout);
                }
                case LSP_SNMP -> {
                    formBeans = new ObjectMapper().
                            readValue(collectorDataView.getParams(), LspSnmpCollectorView.class);
                    LspSnmpCollectorView lspSnmpCollectorView = (LspSnmpCollectorView) formBeans;
                    timeout = lspSnmpCollectorView.getAdvanced().getTimeout();
                    collectorDataView.setTimeout(timeout);
                }
                case CONFIG_PARSE -> {
                    formBeans = (null == collectorDataView.getParams()) ? new ParseConfigCollectorView() :
                            new ObjectMapper().readValue(collectorDataView.getParams(), ParseConfigCollectorView.class);
                    ParseConfigCollectorView parseConfigCollectorView = (ParseConfigCollectorView) formBeans;
                    encryptCredentials(parseConfigCollectorView.getGetConfig().getLoginConfig());
                    ParseConfigCollectorView configCollectorView = (ParseConfigCollectorView) formBeans;
                    timeout = configCollectorView.getParseConfig().getParseConfigAdvanced().getTimeout();
                    timeout = Integer.max(timeout, configCollectorView.getGetConfig().getTimeout());
                    collectorDataView.setTimeout(timeout);
                }
                case TRAFFIC_POLL -> {
                    formBeans = (null == collectorDataView.getParams()) ? new TrafficCollectorView() :
                            new ObjectMapper().readValue(collectorDataView.getParams(), TrafficCollectorView.class);
                    TrafficCollectorView trafficCollectorView = (TrafficCollectorView) formBeans;
                    timeout = trafficCollectorView.getSnmpTrafficPoller().getTimeout();
                    collectorDataView.setTimeout(timeout);
                }
                case TRAFFIC_DEMAND -> formBeans = (null == collectorDataView.getParams() ? new DemandCollectorView() :
                        new ObjectMapper().readValue(collectorDataView.getParams(), DemandCollectorView.class));
                case INVENTORY -> {
                    formBeans = (null == collectorDataView.getParams() ? new InventoryCollectorView() :
                            new ObjectMapper().readValue(collectorDataView.getParams(), InventoryCollectorView.class));
                    InventoryCollectorView inventoryCollectorView = (InventoryCollectorView) formBeans;
                    timeout = inventoryCollectorView.getAdvanced().getActionTimeout();
                    collectorDataView.setTimeout(timeout);
                }
                case LOGIN_FIND_MULTICAST -> {
                    formBeans = (null == collectorDataView.getParams() ? new LoginFindMulticastCollectorView() :
                            new ObjectMapper().readValue(collectorDataView.getParams(), LoginFindMulticastCollectorView.class));
                    LoginFindMulticastCollectorView loginFindMulticastCollectorView = (LoginFindMulticastCollectorView) formBeans;
                    timeout = loginFindMulticastCollectorView.getTimeout();
                    collectorDataView.setTimeout(timeout);
                }
                case LOGIN_POLL_MULTICAST -> {
                    formBeans = (null == collectorDataView.getParams() ? new LoginPollMulticastCollectorView() :
                            new ObjectMapper().readValue(collectorDataView.getParams(), LoginPollMulticastCollectorView.class));
                    LoginPollMulticastCollectorView loginPollMulticastCollectorView = (LoginPollMulticastCollectorView) formBeans;
                    timeout = loginPollMulticastCollectorView.getTimeout();
                    collectorDataView.setTimeout(timeout);
                }
                case SNMP_FIND_MULTICAST -> {
                    formBeans = (null == collectorDataView.getParams() ? new SnmpFindMulticastCollectorView() :
                            new ObjectMapper().readValue(collectorDataView.getParams(), SnmpFindMulticastCollectorView.class));
                    SnmpFindMulticastCollectorView snmpFindMulticastCollectorView = (SnmpFindMulticastCollectorView) formBeans;
                    timeout = snmpFindMulticastCollectorView.getTimeout();
                    collectorDataView.setTimeout(timeout);
                }
                case SNMP_POLL_MULTICAST -> {
                    formBeans = (null == collectorDataView.getParams() ? new SnmpPollMulticastCollectorView() :
                            new ObjectMapper().readValue(collectorDataView.getParams(), SnmpPollMulticastCollectorView.class));
                    SnmpPollMulticastCollectorView snmpPollMulticastCollectorView = (SnmpPollMulticastCollectorView) formBeans;
                    timeout = snmpPollMulticastCollectorView.getTimeout();
                    collectorDataView.setTimeout(timeout);
                }
                case LAYOUT -> {
                    formBeans = (null == collectorDataView.getParams() ? new LayoutCollectorView() :
                            new ObjectMapper().readValue(collectorDataView.getParams(), LayoutCollectorView.class));
                    LayoutCollectorView layoutCollectorView = (LayoutCollectorView) formBeans;
                    Optional.ofNullable(layoutCollectorView.getAdvanced().getConnectTimeout()).ifPresent(collectorDataView::setTimeout);
                }
                case NETFLOW -> formBeans = (null == collectorDataView.getParams() ? new NetflowCollector() :
                        new ObjectMapper().readValue(collectorDataView.getParams(), NetflowCollector.class));
                case EXTERNAL_SCRIPT -> {
                    checkForNullParams(collectorDataView);
                    formBeans =
                            new ObjectMapper().readValue(collectorDataView.getParams(), CustomCollector.class);
                    aggregatorService.validateAggregatorProperties(((CustomCollector) formBeans).getAggregatorProperties());
                    CustomCollector customCollector = (CustomCollector) formBeans;
                    Optional.ofNullable(customCollector.getTimeout()).ifPresent(collectorDataView::setTimeout);
                }
                case DEMAND_MESH_CREATOR -> formBeans = (null == collectorDataView.getParams() ? new DmdMeshCreatorView() :
                        new ObjectMapper().readValue(collectorDataView.getParams(), DmdMeshCreatorView.class));
                case DEMAND_FOR_LSPS -> formBeans = (null == collectorDataView.getParams() ? new DmdsForLspsView() :
                        new ObjectMapper().readValue(collectorDataView.getParams(), DmdsForLspsView.class));
                case DEMAND_FOR_P2MP_LSPS -> formBeans = (null == collectorDataView.getParams() ? new DmdsForP2mplspsView() :
                        new ObjectMapper().readValue(collectorDataView.getParams(), DmdsForP2mplspsView.class));
                case DEMAND_DEDUCTION -> formBeans = (null == collectorDataView.getParams() ? new DemandDeductionView() :
                        new ObjectMapper().readValue(collectorDataView.getParams(), DemandDeductionView.class));
                case COPY_DEMANDS -> formBeans = (null == collectorDataView.getParams() ? new CopyDemands() :
                        new ObjectMapper().readValue(collectorDataView.getParams(), CopyDemands.class));
                case MULTICAST -> formBeans = (null == collectorDataView.getParams() ? new MulticastCollectorView() :
                        new ObjectMapper().readValue(collectorDataView.getParams(), MulticastCollectorView.class));
                default -> formBeans = new ObjectMapper()
                        .readValue(collectorDataView.getParams(), ConfigParams.class);
            }

            ObjectMapper Obj = new ObjectMapper();
            // Converting the Java object into a JSON string
            String collectorParamsAsStr = Obj.writeValueAsString(formBeans);
            log.info("Collector params with default value = " + collectorParamsAsStr);
            collectorDataView.setParams(collectorParamsAsStr);

        } catch (JsonProcessingException jsonMappingException) {
            log.error("Json Error parsing the Collector Parameters ",jsonMappingException);
            throw new CustomException(HttpStatus.BAD_REQUEST, "Invalid collector parameters specified for collector " + collectorDataView.getName(),
                    jsonMappingException.getMessage());
        }

        validationService.validateInput(formBeans);
    }

    private void checkForNullParams(CollectorDataView collectorDataView) {
        if (null == collectorDataView.getParams()) {
            throw new CustomException(HttpStatus.BAD_REQUEST,
                    "Collector parameters are empty. Collector " + collectorDataView.getName() + " cannot be saved !");
        }
    }

    /*
    The method checks if source collector id or (name & type)
    Name & type support is been added when id is not available in create flow.
     */
    public void checkForSourceCollector(CollectorDataView collectorDataView) {
        if (collectorDataView.getType().equals(CollectorTypes.TOPO_IGP) ||
                collectorDataView.getType().equals(CollectorTypes.TOPO_BGPLS_XTC)) {
            return;
        }

        if (collectorDataView.getSourceCollector() != null &&
                CollectorTypes.DARE.equals(collectorDataView.getSourceCollector().getType())) {
            log.info("The source collector is dare.");
            collectorDataView.getSourceCollector().setName(CollectorTypes.DARE.name());
            collectorDataView.getSourceCollector().setType(CollectorTypes.DARE);
            collectorDataView.getSourceCollector().setId(CollectorService.DARE_COLLECTOR_ID);
            log.info("The source collector is dare." + collectorDataView.getSourceCollector());
            return;
        }

        if (collectorDataView.getSourceCollector() == null || (collectorDataView.getSourceCollector().getId() == null
                && StringUtil.isEmpty(collectorDataView.getSourceCollector().getName()))) {
            if (collectorDataView.getType().equals(CollectorTypes.EXTERNAL_SCRIPT)) {
                log.info("External executable script is not having any source collector.");
                return;
            }

            throw new CustomException(HttpStatus.BAD_REQUEST,
                    "The source collector must be specified for collector " + collectorDataView.getName());
        }

        if (collectorDataView.getSourceCollector().getId() != null) {
            Optional<Collector> collector = collectorRepository.findById(collectorDataView.getSourceCollector().getId());
            if (collector.isEmpty()) {
                log.warn("No collector found associated with id {}.", collectorDataView.getSourceCollector().getId());
                if (collectorDataView.getSourceCollector().getName() != null) {
                    List<Collector> collectorByNameType = collectorRepository.findByName(collectorDataView.getSourceCollector().getName());
                    if (collectorByNameType.size() == 0) {
                        log.warn("No collector found associated with name or id {}.", collectorDataView.getSourceCollector());
                        throw new CustomException(HttpStatus.BAD_REQUEST, "Invalid Source Collector." +
                                "Please check the source collector name and type provided. The source collector specified " +
                                "does not exist for collector " + collectorDataView.getName());
                    }

                    if (collectorByNameType.size() > 1) {
                        log.warn("More than one collector found associated with name {}. Using the first collector."
                                , collectorDataView.getSourceCollector().getName());
                    } else {
                        log.info("The source collector with name {} is valid.", collectorDataView.getSourceCollector().getName());
                    }
                    collectorDataView.getSourceCollector().setId(collectorByNameType.get(0).getId());
                } else {
                    throw new CustomException(HttpStatus.BAD_REQUEST, "Invalid Source Collector." +
                            "Please check the source collector id, name and type provided. The source collector specified " +
                            "does not exist for collector " + collectorDataView.getName());
                }
            } else {
                log.info("The source collector with id {} is valid.", collectorDataView.getSourceCollector().getId());
            }
        }
    }

    /*
   The method checks if source collector id or (name & type) is specified for the copy demand tool source.
   Name & type support is been added when id is not available.
    */
    public void checkForCopyDemandSource(SourceCollector sourceCollector) {

        if(sourceCollector !=  null &&
                CollectorTypes.DARE.equals(sourceCollector.getType())) {
            log.info("The source collector is dare.");
            sourceCollector.setName(CollectorTypes.DARE.name());
            sourceCollector.setType(CollectorTypes.DARE);
            sourceCollector.setId(CollectorService.DARE_COLLECTOR_ID);
            return;
        }

        if (sourceCollector == null || (sourceCollector.getId() == null
                && StringUtil.isEmpty(sourceCollector.getName()) )) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Source collector must be specified for" +
                    " copy demands tool part of Traffic Demand collector.");
        }

        if (sourceCollector.getId() != null) {
            Optional<Collector> collector = collectorRepository.findById(sourceCollector.getId());
            if (collector.isEmpty()) {
                throw new CustomException(HttpStatus.BAD_REQUEST, "Invalid Source collector specified for " +
                        "copy demands tool part of Traffic Demand collector." );
            } else {
                log.info("The source collector for copy demands tool part of" +
                        " Traffic Demand collector with id {} is valid." , sourceCollector.getId());
                sourceCollector.setName(collector.get().getName());
                sourceCollector.setType(collector.get().getType());
                return;
            }
        }

        List<Collector> collectorByName = collectorRepository.findByName(sourceCollector.getName());
        if (collectorByName.size() == 0) {
            log.warn("No collector found associated with name {}."
                    ,sourceCollector.getName());
            throw new CustomException(HttpStatus.BAD_REQUEST, "Invalid Source collector specified for " +
                    "copy demands tool part of Traffic Demand collector.");
        }

        if (collectorByName.size() > 1) {
            log.warn("More than one collector found associated with name {} for copy demands tool part of Traffic Demand collector." +
                            " Using the first collector.", sourceCollector.getName());
        } else {
            log.info("The source collector with name {} for copy demands tool part of Traffic Demand collector is valid."
                    , sourceCollector.getName());
        }
        sourceCollector.setType(collectorByName.get(0).getType());
        sourceCollector.setId(collectorByName.get(0).getId());
    }

    private Map<String, Long> validateAgentsAndGetId(Set<AllAgentData> allAgentDataSet ) {
        boolean foundSrPceAgent = false;
        Map<String, Long> nameToIdMap = new HashMap<>();
        AgentData agentData = new AgentData();
        for (AllAgentData allAgentData : allAgentDataSet) {

            Optional<AgentData> agentDataOptional = Optional.empty();
            if(null != allAgentData.getId())   {
                agentDataOptional = agentService.getAgent(allAgentData.getId());
            }
            if (agentDataOptional.isEmpty()) {
                log.error("The agent with id {} doesn't exist. Get agent by name {}.",
                        allAgentData.getId(), allAgentData.getName());
                Optional<Agents> agentOptional = agentService.getAgentByName(allAgentData.getName());
                if(agentOptional.isEmpty()) {
                    throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid agent specified.",
                            "The agent with id " + allAgentData.getId() + " and name " + allAgentData.getName() + " doesn't exist");
                }
                log.debug("Found the agent {} associated with the collector.", agentOptional.get());

                BeanUtils.copyProperties(agentOptional.get(), agentData);
                allAgentData.setId(agentData.getId());
                nameToIdMap.put(agentData.getName(), agentData.getId());
                log.debug("Added map entry with name {} and id {} .", agentData.getName(), agentData.getId());

            } else {
                agentData = agentDataOptional.get();
            }

            if (!agentData.getType().equals(AgentTypes.SR_PCE_AGENT)) {
                log.error("The agent with type {} should not be associated with collector "
                        , agentData.getType());
                throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid agent Type"
                        + agentData.getType() + "is associated with Collector");
            }

            foundSrPceAgent =  true;
        }

        if (!foundSrPceAgent) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid primary srpce agent specified for the collector.");
        }

        return nameToIdMap;
    }

    /*
    This method is used to encrypt the Login config telnet password.
     */
    private void encryptCredentials(LoginConfig loginConfig) {
        log.debug("Encrypting the telnet password.");
        byte[] encrypted;
        if (null != loginConfig.getTelnetPassword()) {
            encrypted = cryptoService.aesEncrypt(loginConfig.getTelnetPassword());
            if (null != encrypted) {
                loginConfig.setEncodedTelnetPassword(encrypted);
                loginConfig.setTelnetPassword(null);
            }
        }
    }

}
