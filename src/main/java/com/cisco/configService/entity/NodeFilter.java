package com.cisco.configService.entity;

import com.cisco.configService.model.preConfig.NodeFilterData;
import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class NodeFilter {

    @Id
    private Long id;

    private NodeFilterData.Type type;

    private String value;

    private NodeFilterData.Condition condition;

    private Boolean enabled = Boolean.TRUE;
}
