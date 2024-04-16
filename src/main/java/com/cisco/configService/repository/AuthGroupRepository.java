package com.cisco.configService.repository;

import com.cisco.configService.entity.AuthGroup;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface AuthGroupRepository extends CrudRepository<AuthGroup, Long> {

    List<AuthGroup> findByName(String name);

    @Query("SELECT DISTINCT NODE_PROFILE " +
            "FROM NODE_LIST_REF " +
            "WHERE NODE_LIST_ID IN " +
            "  (SELECT DISTINCT NODE_LIST " +
            "    FROM AUTH_GROUP_REF " +
            "    WHERE AUTH_GROUP_ID IN (:authGroupId)) ;")
    List<Long> findNodeProfileByAuthGroup(Long authGroupId);

}
