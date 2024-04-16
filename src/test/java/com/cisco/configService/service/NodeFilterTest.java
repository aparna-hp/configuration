package com.cisco.configService.service;

import com.cisco.configService.model.preConfig.NodeFilterData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
public class NodeFilterTest {

    @Autowired
    NodeFilterService nodeFilterService;

    private static final Logger logger = LogManager.getLogger(NodeFilterTest.class);
    private static final Long nodeProfileId = 1L;

    @Test
    @DisplayName("Test Add Update Delete node filter and test Get for node filter")
    void testAddUpdateDeleteNodeList() {
        List<NodeFilterData> nodeFilterDataList = new ArrayList<>();

        for (int i = 1; i < 10; i++) {
            NodeFilterData nodeFilter = new NodeFilterData();
            nodeFilter.setCondition(NodeFilterData.Condition.INCLUDE);
            nodeFilter.setType(NodeFilterData.Type.IP_INDIVIDUAL);
            nodeFilter.setValue("Host_" + String.format("%04d", i));
            nodeFilter.setEnabled(true);
            nodeFilterDataList.add(nodeFilter);
        }

        nodeFilterService.addUpdateNodeFilterToProfile(nodeProfileId, nodeFilterDataList, true);

        nodeFilterDataList = nodeFilterService.getNodeFilter(nodeProfileId);
        logger.info("Node filter data size = " + nodeFilterDataList.size());
        Assertions.assertEquals(10, nodeFilterDataList.size());

        for (NodeFilterData nodeFilterData : nodeFilterDataList) {
            nodeFilterData.setType(NodeFilterData.Type.HOST_INDIVIDUAL);
        }

        nodeFilterService.addUpdateNodeFilterToProfile(nodeProfileId, nodeFilterDataList, false);

        nodeFilterDataList = nodeFilterService.getNodeFilter(nodeProfileId);
        logger.info("Node filter data size = " + nodeFilterDataList.size());
        Assertions.assertEquals(10, nodeFilterDataList.size());

        nodeFilterDataList.forEach(nodeFilterData -> {
            Assertions.assertNotNull(nodeFilterData.getId());
            Assertions.assertNotNull(nodeFilterData.getType());
            Assertions.assertNotNull(nodeFilterData.getValue());
            Assertions.assertNotNull(nodeFilterData.getCondition());
            Assertions.assertTrue(nodeFilterData.getEnabled());
        });

        final List<Long> deleteNodeFilterIds = nodeFilterDataList.stream()
                .map(NodeFilterData::getId).collect(Collectors.toList());

        Assertions.assertDoesNotThrow(() ->nodeFilterService.deleteNodeFilterOfNodeProfile(deleteNodeFilterIds));

    }


}
