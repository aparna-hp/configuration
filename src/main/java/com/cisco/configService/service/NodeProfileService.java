package com.cisco.configService.service;

import com.cisco.configService.entity.*;
import com.cisco.configService.exception.CustomException;
import com.cisco.configService.model.preConfig.AllNodeProfileData;
import com.cisco.configService.model.preConfig.NodeFilterData;
import com.cisco.configService.model.preConfig.NodeListData;
import com.cisco.configService.model.preConfig.NodeProfileData;
import com.cisco.configService.repository.NodeFilterRepository;
import com.cisco.configService.repository.NodeListRepository;
import com.cisco.configService.repository.NodeProfileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
public class NodeProfileService {

    @Autowired
    NodeProfileRepository nodeProfileRepository;

    @Autowired
    AuthGroupService authGroupService;

    @Autowired
    SnmpGroupService snmpGroupService;

    @Autowired
    NodeListRepository nodeListRepository;

    @Autowired
    NodeListService nodeListService;

    @Autowired
    NodeFilterRepository nodeFilterRepository;

    @Autowired
    NodeFilterService nodeFilterService;

    @Autowired
    PreConfigFileGeneratorService preConfigFileGeneratorService;

    //Create a new node profile along with node lists and node filter
    public void addNodeProfile(NodeProfileData nodeProfileData, boolean... override) {
        NodeProfile nodeProfile = new NodeProfile();
        Optional<NodeProfile> nodeProfileOptional = nodeProfileRepository.findByName(nodeProfileData.getName());
        if(nodeProfileOptional.isPresent()) {
            if(override == null || override.length == 0) {
                throw new CustomException(HttpStatus.BAD_REQUEST, "Network profile with name " +
                        nodeProfileData.getName() + " already exists !");
            }else {
                //Use the existing ID associated with name if override is true
                log.info("Using the existing ID {} associated with the name {}", nodeProfileOptional.get().getId(),
                        nodeProfileData.getName());
                nodeProfileData.setId(nodeProfileOptional.get().getId());
                updateNodeProfile(nodeProfileData);
            }
        } else {
            //Ignore Id
            nodeProfileData.setId(null);
        }

        nodeProfileData.setUpdateDate(LocalDate.now());
        log.debug("Default auth group {} and default snmp group {} is associated with node profile {}", nodeProfileData.getDefaultAuthGroup(),
                nodeProfileData.getDefaultSnmpGroup(), nodeProfileData.getName());

        if(authGroupService.isAuthGroupExist(nodeProfileData.getDefaultAuthGroup()).isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "The default auth group does not exist. " +
                    nodeProfileData.getDefaultAuthGroup());
        }

        if(snmpGroupService.isSnmpGroupExists(nodeProfileData.getDefaultSnmpGroup()).isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "The default snmp group does not exist. " +
                    nodeProfileData.getDefaultAuthGroup());
        }

        //Copy the form bean to the entity bean
        BeanUtils.copyProperties(nodeProfileData, nodeProfile);

        nodeListService.saveNodeListEntity(nodeProfile,
                new ArrayList<>(nodeProfileData.getNodeLists()));

        nodeFilterService.saveNodeFilterEntity(nodeProfile,
                new ArrayList<>(nodeProfileData.getNodeFilters()), true);

        nodeProfileRepository.save(nodeProfile);
        //Copy the Id generated after the insert into the form bean.
        nodeProfileData.setId(nodeProfile.getId());

        //Save the minio path of the network access related files.
        populatePreConfigFiles(nodeProfileData);

    }

    //Get the node profile details along with all the node lists and node filter.
    public Optional<NodeProfileData> getNodeProfile(Long id) {
        log.info("Get the node profile by ID " + id);
        Optional<NodeProfile> optionalNodeProfile = nodeProfileRepository.findById(id);
        if (optionalNodeProfile.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(getNodeProfileData(optionalNodeProfile.get()));
    }


    //Get only the node profile details without node lists and node filters given the ID.
    public Optional<NodeProfileData> getNodeProfileInfo(Long id) {
        log.info("Get the node profile by ID " + id);
        Optional<NodeProfile> optionalNodeProfile = nodeProfileRepository.findById(id);
        if (optionalNodeProfile.isEmpty()) {
            return Optional.empty();
        }

        NodeProfileData nodeProfileData = new NodeProfileData();
        BeanUtils.copyProperties(optionalNodeProfile.get(), nodeProfileData);

        return Optional.of(nodeProfileData);
    }

    //Get the node profile details without node lists and node filters given the name.
    public Optional<NodeProfileData> getNodeProfileByName(String name) {
        log.info("Get the node profile by name " + name);
        Optional<NodeProfile> optionalNodeProfile = nodeProfileRepository.findByName(name);
        if (optionalNodeProfile.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(getNodeProfileData(optionalNodeProfile.get()));
    }

    //Populate the Node profile data from entity bean.
    public NodeProfileData getNodeProfileData(NodeProfile nodeProfile) {

        NodeProfileData nodeProfileData = new NodeProfileData();

        BeanUtils.copyProperties(nodeProfile, nodeProfileData);

        Map<Long, String> authGroupIdtoName = new HashMap<>();
        Map<Long, String> snmpGroupIdtoName = new HashMap<>();

        for (NodeListRef nodeListRef : nodeProfile.getNodeLists()) {
            Long nodeListId = nodeListRef.getNodeListId();
            log.debug("Copying the NodeList with Id " + nodeListId);
            if (null == nodeListId) {
                continue;
            }
            Optional<NodeList> nodeList = nodeListRepository.findById(nodeListRef.getNodeListId());
            if (nodeList.isEmpty()) {
                continue;
            }
            NodeListData nodeListData = populateNodeListData(nodeList.get(), authGroupIdtoName, snmpGroupIdtoName);
            nodeProfileData.getNodeLists().add(nodeListData);
        }

        for (Long nodeFilterId : nodeProfile.getNodeFilterIds()) {
            if (null == nodeFilterId) {
                continue;
            }
            NodeFilterData nodeFilterData = new NodeFilterData();
            Optional<NodeFilter> nodeFilter = nodeFilterRepository.findById(nodeFilterId);

            if (nodeFilter.isEmpty()) {
                continue;
            }

            BeanUtils.copyProperties(nodeFilter.get(), nodeFilterData);
            nodeProfileData.getNodeFilters().add(nodeFilterData);
        }

        return nodeProfileData;
    }

    //Get all the node profile IDs and names.
    public List<AllNodeProfileData> getAllNodeProfileData() {
        Iterable<NodeProfile> nodeProfiles = nodeProfileRepository.findAll();
        List<AllNodeProfileData> nodeProfileDataList = new ArrayList<>();

        for (NodeProfile nodeProfile : nodeProfiles) {
            AllNodeProfileData nodeProfileData = new AllNodeProfileData();
            BeanUtils.copyProperties(nodeProfile, nodeProfileData);
            nodeProfileDataList.add(nodeProfileData);
        }
        return nodeProfileDataList;
    }

    //Get all the node profile details along with the node lists and node filters.
    public List<NodeProfileData> getAllNodeProfile() {
        Iterable<NodeProfile> nodeProfiles = nodeProfileRepository.findAll();
        List<NodeProfileData> nodeProfileDataList = new ArrayList<>();

        for (NodeProfile nodeProfile : nodeProfiles) {
            NodeProfileData nodeProfileData = getNodeProfileData(nodeProfile);
            nodeProfileDataList.add(nodeProfileData);
        }
        log.info("No. of node profiles " + nodeProfileDataList.size());
        return nodeProfileDataList;
    }

    //Update the node profile along with its node lists and node filters.
    public Optional<Long> updateNodeProfile(NodeProfileData nodeProfileData) {
        log.info("Update invoked with nodeProfileData " + nodeProfileData);
        Optional<NodeProfile> optionalNodeProfile = nodeProfileRepository.findById(nodeProfileData.getId());
        if (optionalNodeProfile.isEmpty()) {
            return Optional.empty();
        }
        nodeProfileData.setUpdateDate(LocalDate.now());

        NodeProfile nodeProfile = optionalNodeProfile.get();
        //Name cannot be updated.
        nodeProfileData.setName(nodeProfile.getName());

        if(authGroupService.isAuthGroupExist(nodeProfileData.getDefaultAuthGroup()).isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "The default auth group does not exist. " +
                    nodeProfileData.getDefaultAuthGroup());
        }

        if(snmpGroupService.isSnmpGroupExists(nodeProfileData.getDefaultSnmpGroup()).isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "The default snmp group does not exist. " +
                    nodeProfileData.getDefaultAuthGroup());
        }

        BeanUtils.copyProperties(nodeProfileData, nodeProfile);

        log.info("NodeList size " + nodeProfile.getNodeLists().size());
        log.info("Updated NodeListData size " + nodeProfileData.getNodeLists().size());

        List<Long> prevNodeListIds = nodeProfile.getNodeListIds();
        nodeProfile.getNodeLists().clear();

        nodeListService.saveNodeListEntity(nodeProfile, new ArrayList<>(nodeProfileData.getNodeLists()));

        List<Long> prevNodeFilterIds = nodeProfile.getNodeFilterIds();
        nodeProfile.getNodeFilters().clear();

        nodeFilterService.saveNodeFilterEntity(nodeProfile, new ArrayList<>(nodeProfileData.getNodeFilters()),
                false);

        nodeProfileRepository.save(nodeProfile);
        log.info("Node profile updated data = " + nodeProfile);

        List<Long> currNodeListIds = nodeProfile.getNodeListIds();
        log.info("Prev NodeList Id size : " + prevNodeListIds.size());
        log.info("Curr NodeList Id size : " + currNodeListIds.size());
        prevNodeListIds.removeAll(currNodeListIds);

        prevNodeListIds.forEach(nodeList -> {
            log.info("Delete the node List" + nodeList);
            nodeListRepository.deleteById(nodeList);
        });

        List<Long> currNodeFilterIds = nodeProfile.getNodeFilterIds();
        log.info("Prev NodeFilter Id size : " + prevNodeFilterIds.size());
        log.info("Curr NodeFilter Id size : " + currNodeFilterIds.size());
        prevNodeFilterIds.removeAll(currNodeFilterIds);

        prevNodeFilterIds.forEach(nodeFilter -> {
            log.info("Delete the node List" + nodeFilter);
            nodeFilterRepository.deleteById(nodeFilter);
        });

        //Save the node profile details.
        populatePreConfigFiles(nodeProfileData);

        return Optional.of(nodeProfileData.getId());
    }

    //Update the node profile basic details and node filter only. Do not update node lists.
    public Optional<Long> updateNodeProfileNodeFilter(NodeProfileData nodeProfileData) {
        log.info("Update invoked with nodeProfileData and its node filter " + nodeProfileData);
        Optional<NodeProfile> optionalNodeProfile = nodeProfileRepository.findById(nodeProfileData.getId());
        if (optionalNodeProfile.isEmpty()) {
            return Optional.empty();
        }
        nodeProfileData.setUpdateDate(LocalDate.now());

        NodeProfile nodeProfile = optionalNodeProfile.get();
        //Name cannot be updated.
        nodeProfileData.setName(nodeProfile.getName());

        BeanUtils.copyProperties(nodeProfileData, nodeProfile);

        List<Long> prevNodeFilterIds = nodeProfile.getNodeFilterIds();
        nodeProfile.getNodeFilters().clear();

        nodeFilterService.saveNodeFilterEntity(nodeProfile, new ArrayList<>(nodeProfileData.getNodeFilters()),
                false);

        nodeProfileRepository.save(nodeProfile);
        log.info("Node profile updated data = " + nodeProfile);

        List<Long> currNodeFilterIds = nodeProfile.getNodeFilterIds();
        log.info("Prev NodeFilter Id size : " + prevNodeFilterIds.size());
        log.info("Curr NodeFilter Id size : " + currNodeFilterIds.size());
        prevNodeFilterIds.removeAll(currNodeFilterIds);

        prevNodeFilterIds.forEach(nodeFilter -> {
            log.info("Delete the node List" + nodeFilter);
            nodeFilterRepository.deleteById(nodeFilter);
        });

        //Save the node profile details.
        populatePreConfigFiles(getNodeProfileData(nodeProfile));

        return Optional.of(nodeProfileData.getId());
    }


    //Get the nodeprofile associated with the network.
    public Optional<NodeProfile> getNodeProfileByNetworkId(Long networkId) {
        return nodeProfileRepository.findNodeProfileByNetworkId(networkId);
    }

    //Delete the node profile associated with the ID
    public Optional<Long> deleteNodeProfile(Long id) {
        log.info("Verify if the node profile with id {} is referenced by any network ", id);
        List<Long> nodeProfileRefs = nodeProfileRepository.findNetworkByNodeProfileId(id);

        if (null != nodeProfileRefs && nodeProfileRefs.size() > 0) {
            log.info("The node profile is associated with network ids {} ",
                    Arrays.toString(nodeProfileRefs.toArray()));
            throw new CustomException(HttpStatus.CONFLICT,
                    "Cannot delete the node profile since it is associated with the network.");
        }

        Optional<NodeProfile> optionalNodeProfile = nodeProfileRepository.findById(id);
        if (optionalNodeProfile.isEmpty()) {
            return Optional.empty();
        }

        List<Long> nodeListIds = optionalNodeProfile.get().getNodeListIds();
        List<Long> nodeFilterIds = optionalNodeProfile.get().getNodeFilterIds();

        nodeProfileRepository.delete(optionalNodeProfile.get());

        if (nodeListIds.size() > 0) {
            log.info("Deleting the node lists associated with the node profile ID");
            nodeListService.deleteNodeListsOfNodeProfile(nodeListIds);
        }

        if (nodeFilterIds.size() > 0) {
            log.info("Deleting the node filters associated with the node profile ID");
            nodeFilterService.deleteNodeFilterOfNodeProfile(nodeFilterIds);
        }

        return Optional.of(id);
    }

    /*
    This method returns the node profile associated with the given default auth group name.
     */
    public List<Long> getNodeProfileByAuthGroup(String authGroup){
        List<Long> nodeProfileIdList = new ArrayList<>();
        List<NodeProfile> nodeProfileList = nodeProfileRepository.findByDefaultAuthGroup(authGroup);
        if(null != nodeProfileList){
            for(NodeProfile nodeProfile : nodeProfileList){
                nodeProfileIdList.add(nodeProfile.getId());
            }
        }
        log.debug("Node profile associated with default auth group {} are {}" ,
                authGroup, Arrays.toString(nodeProfileIdList.toArray()));
        return nodeProfileIdList;
    }

    /*
    This method returns the node profile associated with the given default snmp group name.
     */
    public List<Long> getNodeProfileBySnmpGroup(String snmpGroup){
        List<Long> nodeProfileIdList = new ArrayList<>();
        List<NodeProfile> nodeProfileList = nodeProfileRepository.findByDefaultSnmpGroup(snmpGroup);
        if(null != nodeProfileList){
            for(NodeProfile nodeProfile : nodeProfileList){
                nodeProfileIdList.add(nodeProfile.getId());
            }
        }
        log.debug("Node profile associated with default snmp group {} are {}" ,
                snmpGroup, Arrays.toString(nodeProfileIdList.toArray()));
        return nodeProfileIdList;
    }

    private NodeListData populateNodeListData(NodeList nodeList, Map<Long, String> authGroupIdtoName,
                                              Map<Long, String> snmpGroupIdtoName) {
        NodeListData nodeListData = new NodeListData();
        log.info("Node id, ip " + nodeList.getId() + " " + nodeList.getNodeIp());
        BeanUtils.copyProperties(nodeList, nodeListData);

        if (nodeList.getAuthGroup() != null) {
            if (authGroupIdtoName.containsKey(nodeList.getAuthGroup().getAuthGroupId())) {
                nodeListData.setAuthGroupName(authGroupIdtoName.get(nodeList.getAuthGroup().getAuthGroupId()));
            } else {
                Optional<String> authGroupOptional = authGroupService.getAuthGroupNameById(nodeList.getAuthGroup().getAuthGroupId());
                if (authGroupOptional.isEmpty()) {
                    nodeListData.setAuthGroupMismatch(true);
                    authGroupIdtoName.put(nodeList.getAuthGroup().getAuthGroupId(), null);
                } else {
                    nodeListData.setAuthGroupName(authGroupOptional.get());
                    authGroupIdtoName.put(nodeList.getAuthGroup().getAuthGroupId(), authGroupOptional.get());
                }
            }
        } else {
            nodeListData.setAuthGroupMismatch(true);
        }

        if (null != nodeList.getSnmpGroup()) {
            if (snmpGroupIdtoName.containsKey(nodeList.getSnmpGroup().getSnmpGroupId())) {
                nodeListData.setSnmpGroupName(snmpGroupIdtoName.get(nodeList.getSnmpGroup().getSnmpGroupId()));
            } else {
                //Determine if snmp group is already added or not
                Optional<String> snmpGroupNameOptional = snmpGroupService.getSnmpGroupNameById(nodeList.getSnmpGroup().getSnmpGroupId());
                if (snmpGroupNameOptional.isEmpty()) {
                    nodeListData.setSnmpGroupMismatch(true);
                    snmpGroupIdtoName.put(nodeList.getSnmpGroup().getSnmpGroupId(), null);
                } else {
                    nodeListData.setSnmpGroupName(snmpGroupNameOptional.get());
                    snmpGroupIdtoName.put(nodeList.getSnmpGroup().getSnmpGroupId(), snmpGroupNameOptional.get());
                }
            }
        } else {
            nodeListData.setSnmpGroupMismatch(true);
        }
        return nodeListData;
    }

    //Publish the node profile changes to the object storage.
    public void populatePreConfigFiles(NodeProfileData nodeProfileData) {
        //Save the network access related files to minio.
        preConfigFileGeneratorService.generateManageIpFile(nodeProfileData);
        preConfigFileGeneratorService.generateAuthFile(nodeProfileData);
        preConfigFileGeneratorService.generateNetAccess(nodeProfileData.getId());
    }

}
