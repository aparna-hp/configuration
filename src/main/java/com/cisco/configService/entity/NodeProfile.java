package com.cisco.configService.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class NodeProfile {

    @Id
    Long id;

    private String name;

    private String defaultAuthGroup;

    private String defaultSnmpGroup;

    private Boolean useNodeListAsIncludeFilter;

    private LocalDate updateDate;

    private Set<NodeListRef> nodeLists = new HashSet<>();

    private Set<NodeFilterRef> nodeFilters = new HashSet<>();

    public void addNodeList(NodeList nodeList) {
        this.nodeLists.add(createNodeListRef(nodeList));
    }

    public List<Long> getNodeListIds() {
        return nodeLists.stream().map(NodeListRef::getNodeListId).collect(Collectors.toList());
    }

    public void addNodeFilter(NodeFilter nodeFilter) {
        this.nodeFilters.add(createNodeFilterRef(nodeFilter));
    }

    public List<Long> getNodeFilterIds() {
        return nodeFilters.stream().map(NodeFilterRef::getNodeFilterId).collect(Collectors.toList());
    }

    private NodeListRef createNodeListRef(NodeList nodeList) {
        NodeListRef nodeListRef = new NodeListRef();
        nodeListRef.setNodeListId(nodeList.getId());
        return nodeListRef;
    }

    private NodeFilterRef createNodeFilterRef(NodeFilter nodeFilter) {
        NodeFilterRef nodeFilterRef = new NodeFilterRef();
        nodeFilterRef.setNodeFilterId(nodeFilter.getId());
        return nodeFilterRef;
    }

    @Override
    public String toString() {
        return "NodeProfile{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", nodeLists=" + Arrays.toString(nodeLists.toArray()) +
                ", nodeFilters=" + Arrays.toString(nodeFilters.toArray())+
                '}';
    }
}
