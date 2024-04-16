package com.cisco.configService.migration;

import com.cisco.collectionService.utils.StringUtil;
import com.cisco.configService.enums.SnmpSecurityLevel;
import com.cisco.configService.migration.wae7xConfig.Config;
import com.cisco.configService.migration.wae7xConfig.netAccess.*;
import com.cisco.configService.migration.wae7xConfig.scheduler.Action;
import com.cisco.configService.migration.wae7xConfig.scheduler.Scheduler;
import com.cisco.configService.migration.wae7xConfig.scheduler.Task;
import com.cisco.configService.migration.wae7xConfig.scheduler.Trigger;
import com.cisco.configService.model.AllConfigurations;
import com.cisco.configService.model.composer.CollectorDataView;
import com.cisco.configService.model.parseConfig.ui.ParseConfigCollectorView;
import com.cisco.configService.model.preConfig.*;
import com.cisco.configService.model.scheduler.SchedulerConfigData;
import com.cisco.configService.model.scheduler.TaskConfigData;
import jakarta.xml.bind.JAXBContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.*;

@Service
@Slf4j
public class MigrationService {

    @Autowired
    CollectorMigrationService collectorMigrationService;

    @Autowired
    AgentMigrationService agentMigrationService;

    final static String SPACE = " " ;

    public AllConfigurations migrateConfigurations(MultipartFile file) {
        log.info("Migrating configurations from file " + file.getOriginalFilename());
        AllConfigurations allConfigurations = new AllConfigurations();
        Map<String, List<NodeFilterData>> nodeFilterMap = new HashMap<>();

        try (InputStream fileInputStream = new BufferedInputStream(file.getInputStream())) {
            JAXBContext context = JAXBContext.newInstance(Config.class);
            Config data = (Config) context.createUnmarshaller()
                    .unmarshal(fileInputStream);

            allConfigurations.setAuthGroupDataList(migrateAuthGroup
                    (data.getDevices().getAuthgroups().getGroup()));

            allConfigurations.setSnmpGroupDataList(migrateSnmpGroup(
                    data.getDevices().getAuthgroups().getSnmpGroup()));


            allConfigurations.setNodeProfileDataList(migrateNetworkAccess(
                    data.getWae().getNimos().getNetworkAccess()));

            nodeFilterMap = migrateNodeFilter(data.getWae().getNimos().getNodeFilter());

            allConfigurations.setAgentDataList(agentMigrationService.migrateSrpceAgents(
                    data.getWae().getAgents().getSrPceAgent()));

            Map<String, ParseConfigCollectorView> parseConfigCollectors = agentMigrationService.migrateParseConfigAgents(
                    data.getWae().getAgents().getCfgParseAgent());

            Map<String, TaskConfigData> collectorTasks = new HashMap<>();
            Map<String, String> collectorToNetworkMap = new HashMap<>();

            collectorMigrationService.migrateNetwork(data.getNetworks(), nodeFilterMap,
                            data.getWae().getComponents().getAggregators().getAggregator(),
                            allConfigurations, collectorToNetworkMap, collectorTasks, parseConfigCollectors);

            allConfigurations.getSchedulerConfigDataList().addAll
                    (migrateScheduler(data.getWae().getComponents().getScheduler(), collectorToNetworkMap,collectorTasks));

        } catch (Exception e) {
            log.error("Error migrating configurations", e);
            log.error("Errors ",e.getCause());

        }
        return allConfigurations;
    }

    public List<AuthGroupData> migrateAuthGroup(List<Group> authgroups) {
        log.info("Migrating Auth groups " + authgroups.size());
        List<AuthGroupData> authGroupDataList = new ArrayList<>();

        for (Group authGroup : authgroups) {
            log.debug("Migrating Auth group " + authGroup);
            if (Optional.ofNullable(authGroup.getDefaultMap()).isPresent()) {
                AuthGroupData authGroupData = new AuthGroupData();
                authGroupData.setName(authGroup.getName());
                authGroupData.setUsername(authGroup.getDefaultMap().getRemoteName());
                authGroupDataList.add(authGroupData);
            } else if(authGroup.getUmap().size() > 0){
                AuthGroupData authGroupData = new AuthGroupData();
                authGroupData.setName(authGroup.getName());
                authGroupData.setUsername(authGroup.getUmap().get(0).getRemoteName());
                authGroupDataList.add(authGroupData);
            }
        }

        return authGroupDataList;
    }

    public List<SnmpGroupData> migrateSnmpGroup(List<SnmpGroup> snmpGroups) {
        log.info("Migrating Snmp groups " + snmpGroups.size());

        List<SnmpGroupData> snmpGroupDataList = new ArrayList<>();
        for (SnmpGroup snmpGroup : snmpGroups) {
            log.debug("Migrating snmp group " + snmpGroup);
            if (Optional.ofNullable(snmpGroup.getDefaultMap()).isPresent()) {
                SnmpGroupData snmpGroupData = new SnmpGroupData();
                snmpGroupData.setName(snmpGroup.getName());
                Optional.ofNullable(snmpGroup.getDefaultMap().getCommunityName()).ifPresent(snmpGroupData::setRoCommunity);
                snmpGroupData.setSnmpType(SnmpGroupData.SnmpType.SNMPv2c);
                Usm usm = snmpGroup.getDefaultMap().getUsm();

                if (Optional.ofNullable(usm).isPresent()) {
                    if (!StringUtil.isEmpty(usm.getSecurityLevel())) {
                        snmpGroupData.setSnmpType(SnmpGroupData.SnmpType.SNMPv3);
                        if(usm.getSecurityLevel().equalsIgnoreCase("auth-priv")) {
                            snmpGroupData.setSecurityLevel(SnmpSecurityLevel.AUTH_PRIV);
                        }else if(usm.getSecurityLevel().equalsIgnoreCase("noauth-nopriv")) {
                            snmpGroupData.setSecurityLevel(SnmpSecurityLevel.NOAUTH_NOPRIV);
                        }else if(usm.getSecurityLevel().equalsIgnoreCase("noauth-priv")) {
                            snmpGroupData.setSecurityLevel(SnmpSecurityLevel.AUTH_NOPRIV);
                        }
                    }

                    Optional.ofNullable(usm.getRemoteName()).ifPresent(snmpGroupData::setUsername);
                    if (Optional.ofNullable(usm.getAuth().getMd5()).isPresent()) {
                        snmpGroupData.setAuthenticationProtocol(SnmpGroupData.AuthenticationProtocol.MD5);
                        snmpGroupData.setAuthenticationPassword(snmpGroup.getDefaultMap().getUsm().getAuth().getMd5().getRemotePassword());
                    } else if (Optional.ofNullable(usm.getAuth().getSha()).isPresent()) {
                        snmpGroupData.setAuthenticationProtocol(SnmpGroupData.AuthenticationProtocol.SHA);
                        snmpGroupData.setAuthenticationPassword(snmpGroup.getDefaultMap().getUsm().getAuth().getSha().getRemotePassword());
                    }
                    if (Optional.ofNullable(usm.getPriv().getAes()).isPresent()) {
                        snmpGroupData.setEncryptionProtocol(SnmpGroupData.EncryptionProtocol.AES);
                        snmpGroupData.setEncryptionPassword(snmpGroup.getDefaultMap().getUsm().getPriv().getAes().getRemotePassword());
                    } else if (Optional.ofNullable(usm.getPriv().getDes()).isPresent()) {
                        snmpGroupData.setEncryptionProtocol(SnmpGroupData.EncryptionProtocol.DES);
                        snmpGroupData.setEncryptionPassword(snmpGroup.getDefaultMap().getUsm().getPriv().getDes().getRemotePassword());
                    }
                }
                log.debug("Migrated Snmp group " + snmpGroupData);
                snmpGroupDataList.add(snmpGroupData);
            }
        }
        return snmpGroupDataList;
    }

    public List<NodeProfileData> migrateNetworkAccess(List<NetworkAccess> networkAccessList) {
        List<NodeProfileData> nodeProfileDataList = new ArrayList<>();
        log.debug("Migrating network access " + networkAccessList.size());
        for (NetworkAccess networkAccess : networkAccessList) {
            log.debug("Migrating Node access " + networkAccess);
            NodeProfileData nodeProfileData = new NodeProfileData();
            nodeProfileData.setName(networkAccess.getName());
            nodeProfileData.setDefaultSnmpGroup(networkAccess.getDefaultSnmpGroup());
            nodeProfileData.setDefaultAuthGroup(networkAccess.getDefaultAuthGroup());

            Set<NodeListData> nodeLists = new HashSet<>();
            for (NodeAccess nodeAccess : networkAccess.getNodeAccess()) {
                NodeListData nodeList = new NodeListData();
                nodeList.setNodeIp(nodeAccess.getIpAddress());
                nodeList.setNodeManagementIp(nodeAccess.getIpManage());
                nodeList.setAuthGroupName(nodeAccess.getAuthGroup());
                nodeList.setSnmpGroupName(nodeAccess.getSnmpGroup());
                nodeLists.add(nodeList);
            }
            nodeProfileData.setNodeLists(nodeLists);
            nodeProfileDataList.add(nodeProfileData);
        }
        return nodeProfileDataList;
    }

    public Map<String, List<NodeFilterData>> migrateNodeFilter(List<NodeFilter> nodeFilterList) {
        Map<String, List<NodeFilterData>> nodeFilterDataMap = new HashMap<>();
        log.debug("Migrating node filter " + nodeFilterList.size());
        for (NodeFilter nodeFilter : nodeFilterList) {
            log.debug("Migrating Node filter " + nodeFilter);
            nodeFilterDataMap.put(nodeFilter.getName(),new ArrayList<>());

            NodeFilterData nodeFilterData = new NodeFilterData();
            if(nodeFilter.getNodeFilter().startsWith(NodeFilter.TYPE.INCLUDE.name())){
                nodeFilterData.setCondition(NodeFilterData.Condition.INCLUDE);
            } else {
                nodeFilterData.setCondition(NodeFilterData.Condition.EXCLUDE);
            }

            if(null != nodeFilter.getRegexFilter() && !nodeFilter.getRegexFilter().startsWith(NodeFilter.TYPE.IGNORE.name()) &&
                    !StringUtil.isEmpty(nodeFilter.getRegex())) {
                nodeFilterData.setType(NodeFilterData.Type.IP_REGEX);
               nodeFilterData.setValue(nodeFilter.getRegex());
               nodeFilterDataMap.put(nodeFilter.getName(), List.of(nodeFilterData));
                log.debug("Migrated Node filter data: " + nodeFilterData);
            }

            if(null != nodeFilter.getNodeFilterList() && !nodeFilter.getNodeFilter().startsWith(NodeFilter.TYPE.IGNORE.name()) &&
                    null != nodeFilter.getNodeFilterList() && nodeFilter.getNodeFilterList().size() > 0) {
                nodeFilterData.setType(NodeFilterData.Type.IP_INDIVIDUAL);

                List<NodeFilterData> nodeFilterDataList = new ArrayList<>();
                for(NodeFilter.NodeFilterList ipaddr : nodeFilter.getNodeFilterList()){
                    NodeFilterData nodeFilterDataIp = new NodeFilterData();
                    nodeFilterDataIp.setCondition(nodeFilterData.getCondition());
                    nodeFilterDataIp.setType(nodeFilterData.getType());
                    nodeFilterDataIp.setEnabled(true);
                    nodeFilterDataIp.setValue(ipaddr.getNode());

                    nodeFilterDataList.add(nodeFilterDataIp);
                }
                nodeFilterDataMap.put(nodeFilter.getName(), nodeFilterDataList);
                log.debug("Migrated Node filter data: " + nodeFilterDataMap);

            }
        }
        return nodeFilterDataMap;
    }

    public List<SchedulerConfigData> migrateScheduler(Scheduler scheduler, Map<String, String> collectorToNetworkMap,
                                                      Map<String, TaskConfigData> taskConfigDataMap){
        List<SchedulerConfigData> schedulerConfigDataList = new ArrayList<>();
        if(null == scheduler.getTask()){
            log.debug("The tasks under the scheduler is empty. ");
            return schedulerConfigDataList;
        }
        for(Task  task : scheduler.getTask()) {
            SchedulerConfigData schedulerConfigData = new SchedulerConfigData();
            schedulerConfigData.setName(task.getName());
            schedulerConfigData.setActive(false);
            Optional.ofNullable(task.getEnabled()).ifPresent(schedulerConfigData::setActive);

            for(Action action : task.getAction()) {
                String path = action.getRpc().getPath();
                if(!StringUtil.isEmpty(path)){
                    String search = "network{";
                   int networkIndex = path.indexOf(search) ;
                   String nimoName = path.substring(networkIndex+search.length(), path.indexOf("}"));
                   log.debug("Nimo name found in scheduler task " + nimoName);
                   ///wae:networks/network{igp}/
                    Optional.ofNullable(taskConfigDataMap.get(nimoName)).ifPresent(value -> schedulerConfigData.getTaskConfigDataList().add(value));
                    Optional.ofNullable(collectorToNetworkMap.get(nimoName)).ifPresent(schedulerConfigData::setNetworkName);
                }
            }
            if(StringUtil.isEmpty(schedulerConfigData.getNetworkName())) {
                log.error("There is no network associated with Scheduler {}. Skip migrating the scheduler.", schedulerConfigData);
                continue;
            }

            for(Trigger trigger : task.getTriggers().getTrigger()) {

                if(null != trigger.getCron()) {
                    Optional.ofNullable(trigger.getCron().getMinute()).ifPresentOrElse((value) -> log.debug("Minute value " + value),
                            () -> trigger.getCron().setMinute("*"));

                    Optional.ofNullable(trigger.getCron().getHour()).ifPresentOrElse((value) -> log.debug("Hour value " + value),
                            () -> trigger.getCron().setHour("*"));

                    Optional.ofNullable(trigger.getCron().getDayOfMonth()).ifPresentOrElse((value) -> log.debug("Day of Month value " + value),
                            () -> trigger.getCron().setDayOfMonth("*"));

                    Optional.ofNullable(trigger.getCron().getMonth()).ifPresentOrElse((value) -> log.debug("Month value " + value),
                            () -> trigger.getCron().setMonth("*"));

                    Optional.ofNullable(trigger.getCron().getDayOfWeek()).ifPresentOrElse((value) -> log.debug("Day of Week value " + value),
                            () -> trigger.getCron().setDayOfWeek("*"));

                    String cronExpression = "0 " +  trigger.getCron().getMinute() + SPACE + trigger.getCron().getHour() + SPACE + trigger.getCron().getDayOfMonth()
                            + SPACE + trigger.getCron().getMonth() + SPACE + trigger.getCron().getDayOfWeek();

                    log.debug("Cron expression " + cronExpression);
                    schedulerConfigData.setCronExpression(cronExpression);
                }
            }
            schedulerConfigDataList.add(schedulerConfigData);
        }
        return schedulerConfigDataList;
    }



}
