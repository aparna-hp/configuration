package com.cisco.configService.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class CollectorCustomRepository {

    String DELETE_COLLECTOR_REF = "DELETE FROM COLLECTOR_REF WHERE COLLECTOR_ID IN (:COLLECTOR_ID)";

    String DELETE_AGENT_REF = "DELETE FROM AGENT_REF WHERE COLLECTOR IN (:COLLECTOR_ID)";

    String DELETE_CHILD_COLLECTOR_REF = "DELETE FROM CHILD_COLLECTOR_REF WHERE COLLECTOR IN (:COLLECTOR_ID)";

    String DELETE_COLLECTOR = "DELETE FROM COLLECTOR WHERE ID IN (:COLLECTOR_ID)";

    @Autowired
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public void deleteCollector(Long collectorId) {

        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("COLLECTOR_ID", collectorId);

        namedParameterJdbcTemplate.update(DELETE_COLLECTOR_REF,
                parameters);

        namedParameterJdbcTemplate.update(DELETE_CHILD_COLLECTOR_REF,
                parameters);

        namedParameterJdbcTemplate.update(DELETE_AGENT_REF,
                parameters);

        namedParameterJdbcTemplate.update(DELETE_COLLECTOR,
                parameters);
    }
}
