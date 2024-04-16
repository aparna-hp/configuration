package com.cisco.configService.service;

import com.cisco.configService.entity.NodeFilter;
import com.cisco.configService.entity.NodeProfile;
import com.cisco.configService.exception.CustomException;
import com.cisco.configService.model.preConfig.NodeFilterData;
import com.cisco.configService.repository.NodeFilterCustomRepository;
import com.cisco.configService.repository.NodeFilterRepository;
import com.cisco.configService.repository.NodeProfileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class NodeFilterService {

    @Autowired
    NodeProfileRepository nodeProfileRepository;

    @Autowired
    NodeFilterCustomRepository nodeFilterCustomRepository;

    @Autowired
    NodeFilterRepository nodeFilterRepository;

    public void addUpdateNodeFilterToProfile(Long nodeProfileId, List<NodeFilterData> nodeFilterDataList, Boolean isAdd) {

        Optional<NodeProfile> nodeProfileDataOptional = nodeProfileRepository.findById(nodeProfileId);
        if(nodeProfileDataOptional.isEmpty()) {
            log.error("The node profile with id {} doesn't exist. Failed to add node filter. ", nodeProfileId);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid Node profile specified." +
                    "The node profile doesn't exists.");
        }

        NodeProfile nodeProfile = nodeProfileDataOptional.get();

        saveNodeFilterEntity(nodeProfile, nodeFilterDataList, isAdd);

        //Save the nodeprofile with node filters.
        nodeProfileRepository.save(nodeProfile);
    }

    public void deleteNodeFilterOfNodeProfile(List<Long> nodeFilterIds) {
        nodeFilterCustomRepository.deleteNodeFilterAndRef(nodeFilterIds);
    }

    public List<NodeFilterData> getNodeFilter(Long nodeProfileId) {
        Optional<NodeProfile> nodeProfileDataOptional = nodeProfileRepository.findById(nodeProfileId);
        if(nodeProfileDataOptional.isEmpty()) {
            log.error("The node profile with id {} doesn't exist. ", nodeProfileId);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid Node profile specified." +
                    "The node profile doesn't exists.");
        }
        return nodeFilterCustomRepository.getNodeFilterByNodeProfileId(nodeProfileId);
    }

    public void saveNodeFilterEntity(NodeProfile nodeProfile, List<NodeFilterData> nodeFilterDataList,
                                     boolean isAdd){

        for(NodeFilterData nodeFilterData : nodeFilterDataList){
            // Populate the entity bean with attributes.
            if(isAdd) {
                // Treat the node list as a new value for POST workflow
                nodeFilterData.setId(null);
            }
            NodeFilter nodeFilter = new NodeFilter();
            BeanUtils.copyProperties(nodeFilterData, nodeFilter);

            //Save nodefilter and copy the ID created.
            nodeFilterRepository.save(nodeFilter);
            nodeFilterData.setId(nodeFilter.getId());

            //Add the nodefilter to Nodeprofile
            nodeProfile.addNodeFilter(nodeFilter);
        }

    }
}
