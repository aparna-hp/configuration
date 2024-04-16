package com.cisco.configService.entity;

import com.cisco.configService.enums.AgentTypes;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDate;

@Data
public class Agents {

    @Id
    Long id;

    private String name;

    private AgentTypes type;

    private String params;

    private LocalDate updateDate;
}
