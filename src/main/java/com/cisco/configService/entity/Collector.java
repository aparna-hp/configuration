package com.cisco.configService.entity;

import com.cisco.configService.enums.CollectorTypes;
import com.cisco.workflowmanager.ConsolidationType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class Collector {

    @Id
    Long id;

    @NotBlank
    private String name;

    private CollectorTypes type;

    private ConsolidationType consolidationType;

    private String params;

    private String SourceCollector;

    private Integer timeout;

    private Set<AgentRef> agents = new HashSet<>();

    private Set<ChildCollectorRef> childCollectors = new HashSet<>();

    public void addAgent(Long agentsId) {
        AgentRef agentRef = new AgentRef();
        agentRef.setAgentId(agentsId);
        this.agents.add(agentRef);
    }

    public void addChildCollector(Long collectorId) {
        ChildCollectorRef childCollectorRef = new ChildCollectorRef();
        childCollectorRef.setChildCollectorId(collectorId);
        this.childCollectors.add(childCollectorRef);
    }

    public Set<Long> getAgentIds(){
        return agents.stream().map(AgentRef::getAgentId).collect(Collectors.toSet());
    }

    public Set<Long> getChildCollectorIds(){
        return childCollectors.stream().map(ChildCollectorRef::getChildCollectorId).collect(Collectors.toSet());
    }

}
