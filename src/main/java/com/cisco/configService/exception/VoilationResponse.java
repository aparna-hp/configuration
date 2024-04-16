package com.cisco.configService.exception;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class VoilationResponse {
    private List<Violation> violations = new ArrayList<>();
}
