package com.cisco.configService.controller;


import com.cisco.configService.model.preConfig.NodeProfileData;
import com.cisco.configService.model.preConfig.SnmpGroupData;
import com.cisco.configService.service.NodeProfileService;
import com.cisco.configService.service.SnmpGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;


@RestController
@Validated
@CrossOrigin
@RequestMapping(path = {"/api/v1/snmp-groups"}, produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class SnmpGroupController {

    @Autowired
    SnmpGroupService snmpGroupService;

    @Autowired
    NodeProfileService nodeProfileService;

    @Operation(summary = "Create a new snmp group")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Snmp group is created", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SnmpGroupData.class))}),
            @ApiResponse(responseCode = "500", description = "Error creating the snmp group.", content = @Content)})
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createSnmpGroup(@Valid @RequestBody SnmpGroupData snmpGroup) {
            log.info("Saving snmp group " + snmpGroup.getName());
            snmpGroupService.addSnmpGroup(snmpGroup);
            return ResponseEntity.status(HttpStatus.CREATED).body(snmpGroup);
    }


    @Operation(summary = "Get a snmp group by its ID")
    @ApiResponses(value = {@ApiResponse(responseCode = "200",  description = "Found the snmp group", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SnmpGroupData.class))}),
            @ApiResponse(responseCode = "404", description = "Snmp group not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error getting the snmp group.", content = @Content)})
    @GetMapping(path = "{id}")
    public ResponseEntity<Object> getSnmpGroup(@PathVariable(value = "id") Long id) {
        log.info("Get the snmp group by Id = " + id);
        final Optional<SnmpGroupData> snmpGroup = snmpGroupService.getSnmpGroup(id);
        if (snmpGroup.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(snmpGroup.get());
    }


    @CrossOrigin
    @Operation(summary = "Get all Snmp groups")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Found the Snmp groups", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SnmpGroupData.class))}),
            @ApiResponse(responseCode = "500", description = "Error getting the snmp groups.", content = @Content)})
    @GetMapping
    public ResponseEntity<Object> getAllSnmpGroups() {
        log.info("Get the all snmp groups");
        final Iterable<SnmpGroupData> node = snmpGroupService.getAllSnmpGroups();
        return ResponseEntity.ok(node);
    }

    @Operation(summary = "Update the Snmp group")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Updated the snmp group", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SnmpGroupData.class))}),
            @ApiResponse(responseCode = "404", description = "Snmp group not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error updating the Snmp group", content = @Content)})
    @PutMapping
    public ResponseEntity<Object> updateSnmpGroup(@Valid @RequestBody SnmpGroupData snmpGroup) {
        log.info("Update the snmp group " + snmpGroup);
        final Optional<Long> node = snmpGroupService.updateSnmpGroup(snmpGroup);
        if (node.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<Long> nodeProfileList = snmpGroupService.getNodeProfileBySnmpGroup(snmpGroup.getId());
        nodeProfileList.addAll(nodeProfileService.getNodeProfileBySnmpGroup(snmpGroup.getName()));

        log.info("Update the node profile related files associated with snmp group. " +
                "Associated node profile size " + nodeProfileList.size());
        log.debug("Node profile associated with snmp group " + Arrays.toString(nodeProfileList.toArray()));
        nodeProfileList.forEach(nodeProfile -> {
            log.info("Updating the node profile related files associated with id {} ", nodeProfile);
            Optional<NodeProfileData> nodeProfileDataOptional = nodeProfileService.getNodeProfile(nodeProfile);
            nodeProfileDataOptional.ifPresent(nodeProfileData -> nodeProfileService.populatePreConfigFiles(nodeProfileData));
        });
        return ResponseEntity.ok(snmpGroup);
    }

    @Operation(summary = "Delete a Snmp group by its ID")
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Snmp group is deleted", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SnmpGroupData.class))}),
            @ApiResponse(responseCode = "404", description = "Snmp group not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error deleting the snmp group.", content = @Content)})
    @DeleteMapping
    public ResponseEntity<Object> deleteSnmpGroup(@RequestParam(value = "id") Long id) {
        log.info("Delete Snmp Group Id = " + id);
        final Optional<Long> snmpGroupData = snmpGroupService.deleteSnmpGroup(id);
        if (snmpGroupData.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
