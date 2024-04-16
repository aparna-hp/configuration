package com.cisco.configService.repository;

import com.cisco.configService.model.preConfig.NodeFilterData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Slf4j
public class NodeFilterCustomRepository {

    String DELETE_NODE_FILTERS = "DELETE FROM NODE_FILTER WHERE ID IN (:NODE_FILTER_IDS)";

    String DELETE_NODE_FILTER_REF = "DELETE FROM NODE_FILTER_REF WHERE NODE_FILTER_ID IN (:NODE_FILTER_IDS)";

    String GET_NODE_FILTER = "SELECT  ID ,TYPE ,CONDITION ,VALUE ,ENABLED " +
            "FROM " +
            "  NODE_FILTER " +
            " WHERE " +
            "  NODE_FILTER.ID IN ( " +
            "    SELECT " +
            "      NODE_FILTER_ID " +
            "    FROM " +
            "      NODE_FILTER_REF " +
            "    WHERE " +
            "      NODE_FILTER_REF.NODE_PROFILE = :NODE_PROFILE_ID" +
            "  )";

    @Autowired
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public List<NodeFilterData> getNodeFilterByNodeProfileId(Long nodeProfileId) {

        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("NODE_PROFILE_ID", nodeProfileId);

        return namedParameterJdbcTemplate.query(GET_NODE_FILTER,
                parameters,
                new BeanPropertyRowMapper<>(NodeFilterData.class));
    }

    public void deleteNodeFilterAndRef(List<Long> nodeFilterIds) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("NODE_FILTER_IDS", nodeFilterIds);

        log.debug("No. of rows deleted from node_list_ref table " +
                namedParameterJdbcTemplate.update(DELETE_NODE_FILTER_REF, parameters));

        log.debug("No. of rows deleted from node_list table " +
                namedParameterJdbcTemplate.update(DELETE_NODE_FILTERS, parameters));
    }
}
