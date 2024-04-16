package com.cisco.configService.controller;


import com.cisco.configService.model.preConfig.NodeFilterData;
import com.cisco.configService.model.preConfig.NodeProfileData;
import com.cisco.configService.service.NodeFilterService;
import com.cisco.configService.service.NodeProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;


@RestController
@CrossOrigin
@RequestMapping(path = {"/api/v1/node-profiles/node-filters"}, produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class NodeFilterController {

    @Autowired
    NodeFilterService nodeFilterService;

    @Autowired
    NodeProfileService nodeProfileService;

    @Operation(summary = "Add node filter to a node profile")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Node filters are added to the profile", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = NodeFilterData.class)))}),
            @ApiResponse(responseCode = "500", description = "Error adding the node filters to the node profile.", content = @Content)})
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> addNodeFilter(@RequestParam Long nodeProfileId,
                                                        @RequestBody List<NodeFilterData> nodeFilterData) {
        log.info("Adding node lists to the  NodeProfile " + nodeProfileId);
        nodeFilterService.addUpdateNodeFilterToProfile(nodeProfileId, nodeFilterData, true);
        log.info("Update the network profile files belonging to  ID " + nodeProfileId);
        Optional<NodeProfileData> nodeProfileDataOptional = nodeProfileService.getNodeProfile(nodeProfileId);
        nodeProfileDataOptional.ifPresent(nodeProfileData -> nodeProfileService.populatePreConfigFiles(nodeProfileData));
        return ResponseEntity.status(HttpStatus.CREATED).body(nodeFilterData);
    }

    @Operation(summary = "Update node filters belonging to a node profile")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Node filters are updated to the profile", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = NodeFilterData.class)))}),
            @ApiResponse(responseCode = "500", description = "Error updating the node filters to the node profile.", content = @Content)})
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> updateNodeFilter(@RequestParam Long nodeProfileId,
                                                              @RequestBody List<NodeFilterData> nodeFilterData) {
        log.info("Updating node lists to the  NodeProfile " + nodeProfileId);
        nodeFilterService.addUpdateNodeFilterToProfile(nodeProfileId,nodeFilterData, false);
        log.info("Update the network profile files belonging to  ID " + nodeProfileId);
        Optional<NodeProfileData> nodeProfileDataOptional = nodeProfileService.getNodeProfile(nodeProfileId);
        nodeProfileDataOptional.ifPresent(nodeProfileData -> nodeProfileService.populatePreConfigFiles(nodeProfileData));
        return ResponseEntity.status(HttpStatus.OK).body(nodeFilterData);
    }


    @Operation(summary = "Get Node filters by node profile ID")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Found the Node filters", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = NodeFilterData.class)))}),
            @ApiResponse(responseCode = "500", description = "Error getting the node filters belonging to the node profile.", content = @Content)})
    @GetMapping
    public ResponseEntity<Object> getNodeFilter(@RequestParam(value = "nodeProfileId") Long nodeProfileId) {
        log.info("Get the node filter by Node Profile  Id = " + nodeProfileId);
        final List<NodeFilterData> nodeFilterData = nodeFilterService.getNodeFilter(nodeProfileId);
        return ResponseEntity.ok(nodeFilterData);
    }

    @Operation(summary = "Delete node filters by its ID")
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Node filters are deleted", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,  array = @ArraySchema(schema = @Schema(implementation = Long.class)))}),
            @ApiResponse(responseCode = "500", description = "Error adding the node filters to the node profile.", content = @Content)})
    @DeleteMapping
    public ResponseEntity<Object> deleteNodeFilter(@RequestBody List<Long> nodeFilterIds) {
        log.info("Delete the Node List by Id = " + Arrays.toString(nodeFilterIds.toArray()));
        nodeFilterService.deleteNodeFilterOfNodeProfile(nodeFilterIds);
        return ResponseEntity.noContent().build();
    }
}
