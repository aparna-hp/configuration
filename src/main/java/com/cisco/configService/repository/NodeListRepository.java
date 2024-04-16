package com.cisco.configService.repository;

import com.cisco.configService.entity.NodeList;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface NodeListRepository extends CrudRepository<NodeList, Long> {

    @Query("SELECT  ID, NODE_IP " +
            "  FROM NODE_LIST WHERE NODE_IP = :nodeIp AND ID IN " +
            "( SELECT NODE_LIST_ID FROM NODE_LIST_REF WHERE NODE_PROFILE = :nodeProfileId ) ;")
    Optional<NodeList> findByNodeIpForNodeProfile(Long nodeProfileId, String nodeIp);
}
