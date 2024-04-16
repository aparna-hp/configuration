package com.cisco.configService.service;

import com.cisco.collectionService.utils.StringUtil;
import com.cisco.configService.entity.*;
import com.cisco.configService.exception.CustomException;
import com.cisco.configService.exception.ValidationService;
import com.cisco.configService.model.preConfig.FilterCriteria;
import com.cisco.configService.model.preConfig.NodeListData;
import com.cisco.configService.model.preConfig.NodeListResponse;
import com.cisco.configService.repository.NodeListCustomRepository;
import com.cisco.configService.repository.NodeListRepository;
import com.cisco.configService.repository.NodeProfileRepository;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

@Service
@Slf4j
public class NodeListService {

    @Autowired
    NodeProfileRepository nodeProfileRepository;

    @Autowired
    AuthGroupService authGroupService;

    @Autowired
    SnmpGroupService snmpGroupService;

    @Autowired
    NodeListRepository nodeListRepository;

    @Autowired
    NodeListCustomRepository nodeListCustomRepository;

    @Autowired
    ValidationService<NodeListData> validationService ;

    Map<String, String> columnNameToEntityMap = Map.of("nodeIP","node_ip",
            "nodeManagementIP","node_management_ip",
            "authGroupName","auth_group_name",
            "snmpGroupName","snmp_group_name");

    private static final String[] CSV_COLUMNS = { "Node IP Address",
            "SNMP Profile",
            "Authentication Profile",
            "Management IP"};

    private static final String DEFAULT_COLUMN = "node_ip";
    private static final Integer DEFALT_PAGE_SIZE = 100;
    private static final Integer DEFAULT_PAGE_NUM = 0;
    private static final String COMMA_DELIMITER = "," , NEW_LINE = System.lineSeparator();

    public NodeListResponse addUpdateNodeListsToProfile(Long nodeProfileId, List<NodeListData> nodeLists) {

        Optional<NodeProfile> nodeProfileDataOptional = nodeProfileRepository.findById(nodeProfileId);
        if(nodeProfileDataOptional.isEmpty()) {
            log.error("The node profile with id {} doesn't exist. Failed to add node lists. ", nodeProfileId);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid Node profile specified." +
                    "The node profile doesn't exists.");
        }

        NodeProfile nodeProfile = nodeProfileDataOptional.get();

        NodeListResponse nodeListResponse = saveNodeListEntity(nodeProfile, nodeLists);

        nodeProfileRepository.save(nodeProfile);
        return nodeListResponse;
    }

    public NodeListResponse importNodeLists(Long nodeProfileId, MultipartFile file, List<NodeListData> nodeLists ) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            if ((line = br.readLine()) != null) {
                log.debug("Validating the column names and order " + line);
                String[] columns = line.split(COMMA_DELIMITER);
                if (columns.length != 4 ||
                        !columns[0].contains(CSV_COLUMNS[0]) ||
                        !columns[1].contains(CSV_COLUMNS[1]) ||
                        !columns[2].contains(CSV_COLUMNS[2]) ||
                        !columns[3].contains(CSV_COLUMNS[3])) {
                    log.error("Invalid CSV file format with columns " + line);
                    throw new CustomException(HttpStatus.BAD_REQUEST, "Invalid CSV file format." +
                            "The CSV file file must have the first line with 4 columns Node IP Address, SNMP Profile, Authentication Profile and Management IP");
                }
            }

            while ((line = br.readLine()) != null) {
                log.debug("Processing the node list " + line);
                String[] values = line.split(COMMA_DELIMITER);
                NodeListData nodeListData = new NodeListData();
                //Removing the quotes associated with the data stored in csv.
                nodeListData.setNodeIp(values[0].replaceAll("\"", ""));
                nodeListData.setSnmpGroupName(values[1].replaceAll("\"", ""));
                nodeListData.setAuthGroupName(values[2].replaceAll("\"", ""));
                nodeListData.setNodeManagementIp(values[3].replaceAll("\"", ""));
                nodeLists.add(nodeListData);
            }
        } catch (CustomException cx){
           throw cx;
        }  catch(Exception e) {
            log.error("Error importing the CSV file ", e);
        }

        //Add the node lists to the node profile.
        return addUpdateNodeListsToProfile(nodeProfileId, nodeLists);
    }


    public void deleteNodeListsOfNodeProfile(List<Long> nodeListIds) {
        nodeListCustomRepository.deleteNodeListAndRef(nodeListIds);
    }

    public void deleteAllNodeListsOfNodeProfile(Long nodeProfileId) {
        Optional<NodeProfile> nodeProfileDataOptional = nodeProfileRepository.findById(nodeProfileId);
        if(nodeProfileDataOptional.isEmpty()) {
            log.error("The node profile with id {} doesn't exist. ", nodeProfileId);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid Node profile specified." +
                    "The node profile doesn't exists.");
        }

        nodeListCustomRepository.deleteNodeListAndRef(nodeProfileDataOptional.get().getNodeListIds());
    }

    public long getNodeListCount(Long nodeProfileId, List<FilterCriteria> filterMap ) {
        Optional<NodeProfile> nodeProfileDataOptional = nodeProfileRepository.findById(nodeProfileId);
        if(nodeProfileDataOptional.isEmpty()) {
            log.error("The node profile with id {} doesn't exist. ", nodeProfileId);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid Node profile specified." +
                    "The node profile doesn't exists.");
        }
        long count =  nodeListCustomRepository.getNodeListCountByNodeProfileId(nodeProfileId,
                getColumnFilterMap(filterMap));
        log.info("Count of node list " + count);
        return count;
    }

    public List<NodeListData> getPaginatedNodeList(Long nodeProfileId,  String sortColumn, Boolean isAscOrder,
                                                   Integer size, Integer pageNum, List<FilterCriteria> filterCriteria) {
        Optional<NodeProfile> nodeProfileDataOptional = nodeProfileRepository.findById(nodeProfileId);
        if(nodeProfileDataOptional.isEmpty()) {
            log.error("The node profile with id {} doesn't exist ", nodeProfileId);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid Node profile specified." +
                    "The node profile doesn't exists.");
        }

        size = size == null ? DEFALT_PAGE_SIZE : size;
        pageNum = pageNum == null || pageNum < 1 ? DEFAULT_PAGE_NUM : (pageNum -1 ) * size ;
        sortColumn = sortColumn == null || null == columnNameToEntityMap.get(sortColumn)?
                DEFAULT_COLUMN : columnNameToEntityMap.get(sortColumn);
        isAscOrder = isAscOrder == null || isAscOrder;

       return nodeListCustomRepository.getNodeListByNodeProfileId(
                nodeProfileId, sortColumn, isAscOrder, size, pageNum, getColumnFilterMap(filterCriteria));
    }

    public NodeListResponse saveNodeListEntity(NodeProfile nodeProfile, List<NodeListData> nodeLists ) {
        NodeListResponse nodeListResponse = new NodeListResponse();
        long invalid = 0, valid =0, authMismatch = 0, snmpMismatch = 0;

        log.info("Associate nodelists to the node profile ");
        for(NodeListData nodeListData : nodeLists){
            // Populate the entity bean with attributes.
            try {
                validationService.validateInput(nodeListData);
            } catch (ConstraintViolationException constraintViolationException) {
                log.error("Invalid node list. Ignore. " + nodeListData);
                invalid++;
                continue;
            }

            NodeList nodeList = new NodeList();
            BeanUtils.copyProperties(nodeListData, nodeList);

            /*
            Verify if node ip already exists. If yes override the same entry to avoid duplicates.
             */
            Optional<NodeList> nodeListOptional = nodeListRepository.findByNodeIpForNodeProfile(nodeProfile.getId(),nodeList.getNodeIp());
            if(nodeListOptional.isPresent()) {
                log.debug("The node list with node ip {} already exists with id {} under node profile {}",
                        nodeList.getNodeIp(), nodeListOptional.get(), nodeProfile.getId());
                nodeList.setId(nodeListOptional.get().getId());
            }
            else {
                log.debug("The node ip doesnt exist.");
                nodeList.setId(null);
            }


            //Populate the Auth group reference.
            Optional<AuthGroup> authGroup = authGroupService.isAuthGroupExist(nodeListData.getAuthGroupName());
            if(authGroup.isEmpty()) {
                log.debug("Auth group does not exists");
                nodeListData.setAuthGroupMismatch(true);
                authMismatch++;
            } else {
                log.debug("Auth group found with id " + authGroup.get().getId());
                nodeList.addAuthGroupRef(authGroup.get());
                nodeListData.setAuthGroupMismatch(false);
            }

            //Determine if snmp group is already added or not
            Optional<SnmpGroup> snmpGroup = snmpGroupService.isSnmpGroupExists(nodeListData.getSnmpGroupName());
            if(snmpGroup.isEmpty()) {
                log.debug("Snmp group does not exist");
                nodeListData.setSnmpGroupMismatch(true);
                snmpMismatch++;
            } else {
                log.debug("Snmp group exist " + snmpGroup.get().getId());
                nodeList.addSnmpGroupRef(snmpGroup.get());
                nodeListData.setSnmpGroupMismatch(false);
            }

            //Save NodeList and set the Id to the NodeListData
            try {
                nodeListRepository.save(nodeList);
                nodeListData.setId(nodeList.getId());

                //Add the nodelist to nodeprofile.
                nodeProfile.addNodeList(nodeList);
                if(!nodeListData.isSnmpGroupMismatch() && !nodeListData.isAuthGroupMismatch()) {
                    valid++;
                }
            } catch (Exception e){
                log.error("Error saving the node list details " + nodeListData, e);
            }
        }
        nodeListResponse.setValidNodeListCount(valid);
        nodeListResponse.setInvalidNodeListCount(invalid);
        nodeListResponse.setSnmpMismatchNodListCount(snmpMismatch);
        nodeListResponse.setAuthMismatchNodListCount(authMismatch);

        log.debug("Node List response " + nodeListResponse);
        return nodeListResponse;
    }

    /*
    This method returns all the node lists belonging to the node profile in CSV format.
     */
    public String getAllNodeListsOfProfile(Long nodeProfileId){
        StringBuilder nodeLists = new StringBuilder(String.join(",", CSV_COLUMNS));
        nodeLists.append(NEW_LINE);

        Optional<NodeProfile> nodeProfileDataOptional = nodeProfileRepository.findById(nodeProfileId);
        if(nodeProfileDataOptional.isEmpty()) {
            log.error("The node profile with id {} doesn't exist.", nodeProfileId);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid Node profile specified." +
                    "The node profile doesn't exists.");
        }

        NodeProfile nodeProfile = nodeProfileDataOptional.get();

        for (NodeListRef nodeListRef : nodeProfile.getNodeLists()) {
            Long nodeListId = nodeListRef.getNodeListId();
            log.debug("Exporting the NodeList with Id " + nodeListId);
            if (null == nodeListId) {
                continue;
            }
            Optional<NodeList> nodeListOptional = nodeListRepository.findById(nodeListRef.getNodeListId());
            if (nodeListOptional.isPresent()) {
                String authGroup = "", snmpGroup = "";
                NodeList nodeList = nodeListOptional.get();
                if(null != nodeList.getAuthGroup()) {
                    Optional<String> authGroupOptional = authGroupService.getAuthGroupNameById(nodeList.getAuthGroup().getAuthGroupId());
                    if (authGroupOptional.isPresent()) {
                        authGroup = authGroupOptional.get();
                    }
                }

                if(null != nodeList.getSnmpGroup()) {
                    Optional<String> snmpGroupNameOptional = snmpGroupService.getSnmpGroupNameById(nodeList.getSnmpGroup().getSnmpGroupId());
                    if (snmpGroupNameOptional.isPresent()) {
                        snmpGroup = snmpGroupNameOptional.get();
                    }
                }

                nodeLists.append(nodeList.getNodeIp())
                        .append(COMMA_DELIMITER)
                        .append(snmpGroup)
                        .append(COMMA_DELIMITER)
                        .append(authGroup)
                        .append(COMMA_DELIMITER)
                        .append(nodeList.getNodeManagementIp())
                        .append(NEW_LINE);
            }
        }
        return nodeLists.toString();
    }

    private Map<String, String> getColumnFilterMap(List<FilterCriteria> filterCriteriaList){
        Map<String, String> columnFilterMap = new HashMap<>();
        if (null != filterCriteriaList) {
            filterCriteriaList.forEach((filterCriteria) -> {
                if (!StringUtil.isEmpty(filterCriteria.getColumnValue())) {
                    String column = (null == filterCriteria.getColumnName() ||
                            null == columnNameToEntityMap.get(filterCriteria.getColumnName())) ?
                            DEFAULT_COLUMN : columnNameToEntityMap.get(filterCriteria.getColumnName());
                    String value = "'%" + filterCriteria.getColumnValue() + "%'";
                    log.debug("Using the filter {} for column {}", value, column );
                    columnFilterMap.put(column, value);
                } else {
                    log.debug("Not using filter column since the filter value is empty.");

                }
            });
        }
        return columnFilterMap;
    }
}
