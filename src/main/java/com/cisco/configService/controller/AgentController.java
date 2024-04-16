package com.cisco.configService.controller;

import com.cisco.configService.enums.AgentActionTypes;
import com.cisco.configService.enums.AgentTypes;
import com.cisco.configService.model.preConfig.AgentData;
import com.cisco.configService.model.preConfig.AllAgentData;
import com.cisco.configService.service.AgentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RestController
@CrossOrigin
@RequestMapping(path = {"/api/v1/networks/collectors/agents"}, produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class AgentController {

    @Autowired
    AgentService agentService;

    @Operation(summary = "Add a new agent")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Agent is created", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AgentData.class))}),
            @ApiResponse(responseCode = "500", description = "Error creating the agent.", content = @Content)})
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> addAgent(@RequestBody AgentData agentData){
        agentService.addAgent(agentData);
        return ResponseEntity.status(HttpStatus.CREATED).body(agentData);
    }

    @Operation(summary = "Get agent configuration value by its ID")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Found the agent", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AgentData.class))}),
            @ApiResponse(responseCode = "404", description = "Agent not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error getting the agent details.", content = @Content)})
    @GetMapping(path = "{id}")
    public ResponseEntity<Object> getAgent(@PathVariable(value = "id") Long id) {
        log.info("Get the agent by Id = " + id);
        final Optional<AgentData> optionalAgentData = agentService.getAgent(id);
        if (optionalAgentData.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(optionalAgentData.get());
    }

    @Operation(summary = "Get all agents status.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Agent names and type are found", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AllAgentData.class))}),
            @ApiResponse(responseCode = "500", description = "Error getting the agent status details.", content = @Content)})
    @GetMapping(path = "/status")
    public ResponseEntity<Object> getAllAgentStatus() {
        log.info("Get the all agents name and type");
        final List<AllAgentData> agentDataList = agentService.getAllAgentStatus();
        return ResponseEntity.ok(agentDataList);
    }

    @Operation(summary = "Get the agents name and type belonging to the agent type. If agent type is not provided, then all agents will be returned.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Agent names and type are found", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AllAgentData.class))}),
            @ApiResponse(responseCode = "500", description = "Error getting the agent details.", content = @Content)})
    @GetMapping
    public ResponseEntity<Object> getAgentInfo(@RequestParam(value = "type", required = false) AgentTypes type) {
        log.info("Get the all agents belonging to the type " + type);
        final List<AllAgentData> agentDataList = agentService.getAgentInfo(type);
        return ResponseEntity.ok(agentDataList);
    }

    @Operation(summary = "Update the Agent")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Updated the agent", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AgentData.class))}),
            @ApiResponse(responseCode = "404", description = "Agent not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error updating the Agent", content = @Content)})
    @PutMapping
    public ResponseEntity<Object> updateAgent(@RequestBody AgentData agentData) {
        log.info("Update the agent " + agentData);
        final Optional<Long> agentId = agentService.updateAgent(agentData);
        if (agentId.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(agentData);
    }

    @Operation(summary = "Delete an agent by its ID")
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Agent is deleted", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AgentData.class))}),
            @ApiResponse(responseCode = "404", description = "Agent not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error deleting the agent", content = @Content)})
    @DeleteMapping
    public ResponseEntity<Object> deleteAgent(@RequestParam(value = "id") Long id) {
        log.info("Delete the agent by Id = " + id);
        final Optional<Long> agentId = agentService.deleteAgent(id);
        if (agentId.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Invoke action like start, stop or restart on agent identified by its ID")
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "The agent action is been submitted.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error invoking the agent action", content = @Content)})
    @PutMapping(path = "/invokeAction")
    public ResponseEntity<Object> invokeAction(@RequestParam(value = "id") Long id,
                                               @RequestParam(value = "action") AgentActionTypes agentActionType) {
        log.info("Invoke action {}  on agent with Id = {} " , agentActionType, id);
        agentService.invokeAgent(id, agentActionType);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get status of the agent identified by its ID")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The agent status is successfully retrieved.", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AgentData.class))}),
            @ApiResponse(responseCode = "500", description = "Error getting the agent status", content = @Content),
            @ApiResponse(responseCode = "400", description = "Agent not found.", content = @Content)})
    @GetMapping(path = "/status/{id}")
    public ResponseEntity<Object> getStatus(@PathVariable(value = "id") Long id) {
        log.info("Gets status of agent with Id = {} " , id);
        Optional<AllAgentData> statusOptional = agentService.getStatus(id);
        if(statusOptional.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(statusOptional.get());
    }

    @Operation(summary = "Get agent dynamic form using type along with the configured value using ID")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Agent default parameters obtained successfully.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Agent Type not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error getting the agent default parameters.", content = @Content)})
    @GetMapping(path = "/defaultParameters")
    public ResponseEntity<Object> getAgentDefaultParams(@RequestParam(value = "type") AgentTypes type) {
        log.info("Get the agent default parameters for type {} " , type);
        String params = agentService.getDefaultConfigParams(type);
        if (params == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(params);
    }
}
