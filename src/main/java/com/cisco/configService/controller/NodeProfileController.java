package com.cisco.configService.controller;


import com.cisco.configService.model.preConfig.AllNodeProfileData;
import com.cisco.configService.model.preConfig.NodeProfileData;
import com.cisco.configService.service.NodeProfileService;
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
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
@CrossOrigin
@RequestMapping(path = {"/api/v1/node-profiles"}, produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class NodeProfileController {

    @Autowired
    NodeProfileService nodeProfileService;

    @Operation(summary = "Create a new node profile. Node List and node filter is optional at the time of creation.")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Node profile is created", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = NodeProfileData.class))}),
            @ApiResponse(responseCode = "500", description = "Error creating the node profile.", content = @Content)})
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createNodeProfile(@Valid @RequestBody NodeProfileData nodeProfile) {
            log.info("Saving NodeProfile " + nodeProfile.getName());
        nodeProfileService.addNodeProfile(nodeProfile);
        return ResponseEntity.status(HttpStatus.CREATED).body(nodeProfile);
    }


    @Operation(summary = "Get the given NodeProfile details including all its node lists and node filter by its ID")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Found the Node Profile", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = NodeProfileData.class))}),
            @ApiResponse(responseCode = "404", description = "Node Profile not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error getting the node profile.", content = @Content)})
    @GetMapping(path = "{id}")
    public ResponseEntity<Object> getNodeProfileById(@PathVariable(value = "id") Long id) {
        log.info("Get the NodeP rofile by Id = " + id);
        final Optional<NodeProfileData> nodeProfile = nodeProfileService.getNodeProfile(id);
        if (nodeProfile.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(nodeProfile.get());
    }

    @Operation(summary = "Get the basic NodeProfile details  by its ID. This does not include the node lists and node filter associated with node profile.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Found the Node Profile", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = NodeProfileData.class))}),
            @ApiResponse(responseCode = "404", description = "Node Profile not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error getting the node profile info.", content = @Content)})
    @GetMapping(path = "/info")
    public ResponseEntity<Object> getNodeProfileInfoById(@RequestParam(value = "id") Long id) {
        log.info("Get the NodeP rofile basic info by Id = " + id);
        final Optional<NodeProfileData> nodeProfile = nodeProfileService.getNodeProfileInfo(id);
        if (nodeProfile.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(nodeProfile.get());
    }

    @Operation(summary = "Get all Node Profile name and id for landing page")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Found the Node Profile", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AllNodeProfileData.class))}),
            @ApiResponse(responseCode = "500", description = "Error getting the node profile details.", content = @Content)})
    @GetMapping
    public ResponseEntity<Object> getAllNodeProfile() {
        log.info("Get the all Node Profiles");
        final Iterable<AllNodeProfileData> node = nodeProfileService.getAllNodeProfileData();
        return ResponseEntity.ok(node);
    }

    @Operation(summary = "Update the NodeProfile along with the node list and node filter.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Updated the Node Profile", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = NodeProfileData.class))}),
            @ApiResponse(responseCode = "404", description = "Node Profile not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error updating the Node Profile", content = @Content)})
    @PutMapping
    public ResponseEntity<Object> updateNodeProfile(@Valid @RequestBody NodeProfileData nodeProfile) {
        log.info("Update the Node Profile " + nodeProfile);
        final Optional<Long> node = nodeProfileService.updateNodeProfile(nodeProfile);
        if (node.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(nodeProfile);
    }

    @Operation(summary = "Update the NodeProfile along with node filter only. The node list is not updated.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Updated the Node Profile", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = NodeProfileData.class))}),
            @ApiResponse(responseCode = "404", description = "Node Profile not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error updating the Node Profile", content = @Content)})
    @PutMapping("/info-and-node-filter")
    public ResponseEntity<Object> updateNodeProfileInfoAndNodeFilter(@Valid @RequestBody NodeProfileData nodeProfile) {
        log.info("Update the Node Profile " + nodeProfile);
        final Optional<Long> node = nodeProfileService.updateNodeProfileNodeFilter(nodeProfile);
        if (node.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(nodeProfile);
    }

    @Operation(summary = "Delete a Node Profile by its ID. The associated node lists and node filter are also deleted.")
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Node Profile is deleted", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = NodeProfileData.class))}),
            @ApiResponse(responseCode = "404", description = "Node Profile not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error deleting the node profile", content = @Content)})
    @DeleteMapping
    public ResponseEntity<Object> deleteNodeProfile(@RequestParam(value = "id") Long id) {
        log.info("Delete the Node Profile by Id = " + id);
        final Optional<Long> output = nodeProfileService.deleteNodeProfile(id);
        if (output.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
