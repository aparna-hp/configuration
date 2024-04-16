package com.cisco.configService.repository;

import com.cisco.configService.entity.ImportHistory;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ImportHistoryRepository extends CrudRepository<ImportHistory, Long> {

    @Query("SELECT DISTINCT ID " +
            "FROM IMPORT_HISTORY " +
            "WHERE STATUS = 'IN_PROGRESS' ;")
    List<Long> getInProgressImports();
}
