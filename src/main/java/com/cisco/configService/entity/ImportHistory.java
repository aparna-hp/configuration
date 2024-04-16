package com.cisco.configService.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class ImportHistory {

    public enum Status {
        IN_PROGRESS, SUCCESS, FAILED
    }

    public enum Type {
        CP, WAE
    }

    @Id
    Long id;

    private Type type;

    private  Status status;

    private String failureReport;

    Long startTime;

    Long endTime;

}
