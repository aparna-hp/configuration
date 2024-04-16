package com.cisco.configService.repository;

import com.cisco.configService.model.preConfig.NodeListData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class NodeListCustomRepository {

    String DELETE_NODE_LISTS = "DELETE FROM NODE_LIST WHERE ID IN (:NODE_LIST_IDS)";

    String DELETE_NODE_LIST_REF = "DELETE FROM NODE_LIST_REF WHERE NODE_LIST_ID IN (:NODE_LIST_IDS)";

    String DELETE_AUTH_GROUP_REF = "DELETE FROM AUTH_GROUP_REF WHERE NODE_LIST IN (:NODE_LIST_IDS)";

    String DELETE_SNMP_GROUP_REF = "DELETE FROM SNMP_GROUP_REF WHERE NODE_LIST IN (:NODE_LIST_IDS)";

    String GET_NODE_LIST = "SELECT " +
            "  NODE_LIST.ID, " +
            "  NODE_LIST.NODE_IP, " +
            "  NODE_LIST.NODE_MANAGEMENT_IP, " +
            "  AUTH_GROUP.NAME AS AUTH_GROUP_NAME, " +
            "  SNMP_GROUP.NAME AS SNMP_GROUP_NAME " +
            "FROM " +
            "  NODE_LIST " +
            "  LEFT OUTER JOIN AUTH_GROUP_REF ON AUTH_GROUP_REF.NODE_LIST = NODE_LIST.ID " +
            "  LEFT OUTER JOIN SNMP_GROUP_REF ON SNMP_GROUP_REF.NODE_LIST = NODE_LIST.ID " +
            "  LEFT OUTER JOIN SNMP_GROUP ON SNMP_GROUP.ID = SNMP_GROUP_REF.SNMP_GROUP_ID " +
            "  LEFT OUTER JOIN AUTH_GROUP ON AUTH_GROUP.ID = AUTH_GROUP_REF.AUTH_GROUP_ID " +
            "WHERE " +
            "  NODE_LIST.ID IN ( " +
            "    SELECT " +
            "      NODE_LIST_ID " +
            "    FROM " +
            "      NODE_LIST_REF " +
            "    WHERE " +
            "      NODE_LIST_REF.NODE_PROFILE = :NODE_PROFILE_ID" +
            "  )";

    String SORT_PAGINATION = " @WHERE_CLAUSE ORDER By @SORT_COLUMN @SORT_ORDER LIMIT @SIZE OFFSET @PAGE_NUM";
    String COUNT_QUERY = "SELECT COUNT(1) FROM ( ";
    String QUERY_ALIAS = " AS NODE_LIST_QUERY ";
    String CLOSING_BRACKET = ")";
    String OUTER_QUERY = "SELECT * FROM ( ";
    String LIKE_CLAUSE = " LIKE ";
    String WHERE_CLAUSE = " WHERE ";


    @Autowired
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public List<NodeListData> getNodeListByNodeProfileId(Long nodeProfileId, final String sortColumn, Boolean isAscOrder,
                                                         Integer size, Integer pageNum, Map<String, String> filterMap) {

        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("NODE_PROFILE_ID", nodeProfileId);


        String query = OUTER_QUERY + GET_NODE_LIST + CLOSING_BRACKET + QUERY_ALIAS + SORT_PAGINATION
                .replace("@WHERE_CLAUSE", getWhereClause(filterMap))
                .replace("@SORT_COLUMN", sortColumn)
                .replace("@SORT_ORDER", isAscOrder ? "ASC" : "DESC")
                .replace("@SIZE", size.toString())
                .replace("@PAGE_NUM", pageNum.toString());

        return namedParameterJdbcTemplate.query(query,
                parameters,
                new BeanPropertyRowMapper<>(NodeListData.class));
    }

    public Long getNodeListCountByNodeProfileId(Long nodeProfileId, Map<String, String> filterMap) {

        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("NODE_PROFILE_ID", nodeProfileId);

        String query = COUNT_QUERY + GET_NODE_LIST + CLOSING_BRACKET + QUERY_ALIAS + getWhereClause(filterMap);
        log.debug("Count query " + query);
        return namedParameterJdbcTemplate.queryForObject(query,
                parameters, Long.class);
    }

    public void deleteNodeListAndRef(List<Long> nodeListIds) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("NODE_LIST_IDS", nodeListIds);

        log.debug("No. of rows deleted from node_list_ref table " +
                namedParameterJdbcTemplate.update(DELETE_NODE_LIST_REF, parameters));

        log.debug("No. of rows deleted from Auth_Group_Ref table " +
                namedParameterJdbcTemplate.update(DELETE_AUTH_GROUP_REF, parameters));

        log.debug("No. of rows deleted from Snmp Group ref table " +
                namedParameterJdbcTemplate.update(DELETE_SNMP_GROUP_REF, parameters));

        log.debug("No. of rows deleted from node_list table " +
                namedParameterJdbcTemplate.update(DELETE_NODE_LISTS, parameters));
    }

    public String getWhereClause(Map<String, String> filterMap) {
        StringBuilder whereClause = new StringBuilder();
        String AND = " and ";
        if (filterMap.size() > 0) {
            whereClause.append(WHERE_CLAUSE);
        }
        filterMap.forEach((column, value) -> {
            whereClause.append(column).append(LIKE_CLAUSE).append(value);
            whereClause.append(AND);
        } );

        String whereClauseStr = whereClause.toString();
        if(whereClauseStr.endsWith(AND)) {
            whereClauseStr = whereClauseStr.substring(0, whereClause.lastIndexOf(AND));
        }
        log.debug("Result " + whereClauseStr);
        return whereClauseStr;
    }
}
