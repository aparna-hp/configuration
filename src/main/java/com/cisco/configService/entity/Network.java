package com.cisco.configService.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class Network {

    @Id
    Long id;

    @NotBlank
    private String name;

    private boolean draft = false;

    private String draftConfig;

    private Set<CollectorRef> collectorRefs = new HashSet<>();

    private NodeProfileRef nodeProfileRef;

    public void addCollectorRef(Long collectorId) {
        CollectorRef collectorRef = new CollectorRef();
        collectorRef.setCollectorId(collectorId);
        this.collectorRefs.add(collectorRef);
    }

    public List<Long> getCollectorIds() {
        return collectorRefs.stream().map(CollectorRef::getCollectorId).collect(Collectors.toList());
    }

    public void addNodeProfileRef(Long nodeProfileId) {
        NodeProfileRef nodeProfileRef = new NodeProfileRef();
        nodeProfileRef.setNodeProfileId(nodeProfileId);
        this.nodeProfileRef = nodeProfileRef;
    }
}




