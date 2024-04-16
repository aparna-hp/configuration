package com.cisco.configService.migration;

import com.cisco.configService.enums.AgentTypes;
import com.cisco.configService.enums.CollectorTypes;
import com.cisco.configService.enums.RecordMode;
import com.cisco.configService.migration.wae7xConfig.agents.CfgParse;
import com.cisco.configService.model.common.LoginConfig;
import com.cisco.configService.model.composer.CollectorDataView;
import com.cisco.configService.model.parseConfig.ui.GetConfigView;
import com.cisco.configService.model.parseConfig.ui.ParseConfigCollectorView;
import com.cisco.configService.model.preConfig.AgentData;
import com.cisco.configService.model.srPce.SrPceAgent;
import com.cisco.configService.service.UtilService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.*;

@Service
@Slf4j
public class AgentMigrationService {

    public List<AgentData> migrateSrpceAgents(List<SrPceAgent> srPceAgents) {
        List<AgentData> agentDataList = new ArrayList<>();
        for (SrPceAgent srPceAgent : srPceAgents) {
            try {
                AgentData agentData = new AgentData();
                agentData.setName(srPceAgent.getName());
                agentData.setType(AgentTypes.SR_PCE_AGENT);

                srPceAgent.setAuthenticationType(SrPceAgent.AuthenticationType.NONE);
                Optional.ofNullable(srPceAgent.getUseAuth()).ifPresent(value -> {
                    if (value) {
                        srPceAgent.setAuthenticationType(SrPceAgent.AuthenticationType.BASIC);
                    }
                });

                Optional.ofNullable(srPceAgent.getAdvanced().getPlaybackEventsDelay()).ifPresent(srPceAgent::setPlaybackEventsDelay);
                Optional.ofNullable(srPceAgent.getAdvanced().getEventsBufferTime()).ifPresent(srPceAgent::setEventsBufferTime);
                Optional.ofNullable(srPceAgent.getAdvanced().getPoolSize()).ifPresent(srPceAgent::setPoolSize);

                Optional.ofNullable(srPceAgent.getNetRecordStr()).ifPresent(value ->
                        srPceAgent.setNetRecorderMode(RecordMode.valueOf(value.toUpperCase(Locale.ROOT))));

                Optional.ofNullable(srPceAgent.getAdvanced().getTopologyCollectionStr()).ifPresent(value -> {
                    if (value.equalsIgnoreCase(SrPceAgent.CollectionType.COLLECTION_AND_SUBSCRIPTION.name().replaceAll("_", "-"))) {
                        srPceAgent.setTopologyCollection(SrPceAgent.CollectionType.COLLECTION_AND_SUBSCRIPTION);
                    } else if (value.equalsIgnoreCase(SrPceAgent.CollectionType.OFF.name())) {
                        srPceAgent.setTopologyCollection(SrPceAgent.CollectionType.OFF);
                    } else {
                        srPceAgent.setTopologyCollection(SrPceAgent.CollectionType.COLLECTION_ONLY);
                    }
                });

                Optional.ofNullable(srPceAgent.getAdvanced().getLspCollectionStr()).ifPresent(value -> {
                    if (value.equalsIgnoreCase(SrPceAgent.CollectionType.COLLECTION_AND_SUBSCRIPTION.name().replaceAll("_", "-"))) {
                        srPceAgent.setLspCollection(SrPceAgent.CollectionType.COLLECTION_AND_SUBSCRIPTION);
                    } else if (value.equalsIgnoreCase(SrPceAgent.CollectionType.OFF.name())) {
                        srPceAgent.setLspCollection(SrPceAgent.CollectionType.OFF);
                    } else {
                        srPceAgent.setLspCollection(SrPceAgent.CollectionType.COLLECTION_ONLY);
                    }
                });

                ObjectMapper objectMapper = new ObjectMapper();
                String params = objectMapper.writeValueAsString(srPceAgent);
                agentData.setParams(params);

                log.debug("Migrated Sr PCE agent " + agentData);
                agentDataList.add(agentData);
            } catch (JsonProcessingException e) {
                log.error("Error migrating the SR PCE agent " + srPceAgent.getName(), e);
            }
        }
        log.info("Number of Srpce agents successfully migrated {}", agentDataList.size());
        return agentDataList;
    }

    public Map<String, ParseConfigCollectorView> migrateParseConfigAgents(List<CfgParse> cfgParseList) {
        if (null == cfgParseList){
            log.debug("No parse config agents found.");
            return new HashMap<>();
        }
        Map<String,ParseConfigCollectorView> pcCollectors = new HashMap<>();
        for (CfgParse cfgParse : cfgParseList) {
            try {
                log.info("Migrating the parse config agent " + cfgParse);
                ParseConfigCollectorView parseConfigCollectorView = new ParseConfigCollectorView();
                GetConfigView getConfigView = new GetConfigView();

                if(null != cfgParse.getAdvanced()) {
                    LoginConfig loginConfig = new LoginConfig();
                    BeanUtils.copyProperties(cfgParse.getAdvanced(), loginConfig,
                            UtilService.getNullPropertyNames(cfgParse.getAdvanced()));
                    getConfigView.setLoginConfig(loginConfig);

                    Optional.ofNullable(cfgParse.getAdvanced().getTimeout()).ifPresent(getConfigView::setTimeout);
                    Optional.ofNullable(cfgParse.getAdvanced().getVerbosity()).ifPresent(verbosity ->
                            getConfigView.getDebug().setVerbosity(verbosity));
                    Optional.ofNullable(cfgParse.getAdvanced().getDebug().getLoginRecordMode()).ifPresent(recordMode ->
                            getConfigView.getDebug().setNetRecorder(RecordMode.valueOf(recordMode.toUpperCase(Locale.ROOT))));
                }
                getConfigView.setEnable(true);

                parseConfigCollectorView.setGetConfig(getConfigView);
                log.info("Parse config collector from agent after migration " + parseConfigCollectorView);
                pcCollectors.put(cfgParse.getName(), parseConfigCollectorView);
            }catch (Exception e) {
                log.error("Error migrating the parse config agent " + cfgParse.getName(), e);
            }
        }
        return pcCollectors;
    }
}
