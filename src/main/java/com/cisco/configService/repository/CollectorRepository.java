package com.cisco.configService.repository;

import com.cisco.configService.entity.Collector;
import com.cisco.configService.enums.CollectorTypes;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface CollectorRepository extends CrudRepository<Collector, Long> {

    @Query("SELECT COLLECTOR FROM CHILD_COLLECTOR_REF WHERE CHILD_COLLECTOR_ID = :childCollectorId ;")
    Optional<Long> findParentCollector(Long childCollectorId);

    @Query("SELECT NETWORK FROM COLLECTOR_REF WHERE COLLECTOR_ID = :collectorId ;")
    Optional<Long> findNetworkByCollectorId(Long collectorId);

    @Query("SELECT ID FROM COLLECTOR WHERE TYPE LIKE :collectorType AND ID IN (SELECT COLLECTOR_ID FROM COLLECTOR_REF WHERE NETWORK = :networkId );")
    List<Long> findCollectorIdByNetworkIdAndType(Long networkId, CollectorTypes collectorType);

    List<Collector> findByName(String name);
}
