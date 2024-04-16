package com.cisco.configService.repository;

import com.cisco.configService.entity.NodeProfile;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface NodeProfileRepository extends CrudRepository<NodeProfile, Long> {

    @Query("SELECT  ID ,NAME ,DEFAULT_AUTH_GROUP ,DEFAULT_SNMP_GROUP ,USE_NODE_LIST_AS_INCLUDE_FILTER ,UPDATE_DATE  " +
            "  FROM NODE_PROFILE WHERE ID IN " +
            "( SELECT NODE_PROFILE_ID FROM NODE_PROFILE_REF WHERE NETWORK = :networkId ) ;")
    Optional<NodeProfile> findNodeProfileByNetworkId(Long networkId);

    @Query("SELECT NETWORK FROM NODE_PROFILE_REF WHERE NODE_PROFILE_ID = :nodeProfileId ;")
    List<Long> findNetworkByNodeProfileId(Long nodeProfileId);

    Optional<NodeProfile> findByName(String name);

    List<NodeProfile> findByDefaultAuthGroup(String defaultAuthGroup );

    List<NodeProfile> findByDefaultSnmpGroup(String defaultSnmpGroup );
}
