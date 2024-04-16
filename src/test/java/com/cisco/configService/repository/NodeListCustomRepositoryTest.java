package com.cisco.configService.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class NodeListCustomRepositoryTest {

    @Autowired
    NodeListCustomRepository nodeListCustomRepository;

    @Test
    public void testWhereClause(){
        String whereClause = nodeListCustomRepository.getWhereClause(new HashMap<>());
        Assertions.assertTrue(whereClause.isEmpty());

        whereClause = nodeListCustomRepository.getWhereClause(Map.of("node_ip", "1") );
        Assertions.assertFalse(whereClause.isEmpty());

        whereClause = nodeListCustomRepository.getWhereClause(Map.of("node_ip", "1",
                "management_ip", "1") );
        Assertions.assertFalse(whereClause.isEmpty());
    }
}
