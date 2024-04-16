package com.cisco.configService.repository;

import com.cisco.configService.entity.SnmpGroup;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface SnmpGroupRepository extends CrudRepository<SnmpGroup, Long> {

    List<SnmpGroup> findByName(String name);

    @Query("SELECT DISTINCT NODE_PROFILE " +
            "FROM NODE_LIST_REF " +
            "WHERE NODE_LIST_ID IN " +
            "  (SELECT DISTINCT NODE_LIST " +
            "    FROM SNMP_GROUP_REF " +
            "    WHERE SNMP_GROUP_ID IN (:snmpGroupId)) ;")
    List<Long> findNodeProfileBySnmpGroup(Long snmpGroupId);
}
