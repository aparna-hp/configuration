package com.cisco.configService.repository;

import com.cisco.configService.entity.Agents;
import com.cisco.configService.enums.AgentTypes;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface AgentRepository extends CrudRepository<Agents, Long> {

    @Query("SELECT  ID, NAME, TYPE, UPDATE_DATE FROM AGENTS WHERE ID IN ( SELECT AGENT_ID FROM AGENT_REF WHERE COLLECTOR = :collectorId) ;")
    List<Agents> findAgentByCollectorId(Long collectorId);

    Optional<Agents> findAgentByName(String name);

    List<Agents> findAgentByType(AgentTypes type);
}

