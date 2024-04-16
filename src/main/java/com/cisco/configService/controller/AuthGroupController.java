package com.cisco.configService.controller;


import com.cisco.configService.model.preConfig.AuthGroupData;
import com.cisco.configService.model.preConfig.NodeProfileData;
import com.cisco.configService.service.AuthGroupService;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;


@RestController
@Validated
@CrossOrigin
@RequestMapping(path = {"/api/v1/auth-groups"}, produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class AuthGroupController {

    @Autowired
    AuthGroupService authGroupService;

    @Autowired
    NodeProfileService nodeProfileService;

    @Operation(summary = "Create a new auth group")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Authgroup is created", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AuthGroupData.class))}),
            @ApiResponse(responseCode = "500", description = "Error creating the auth group.", content = @Content)})
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createAuthGroup(@Valid @RequestBody AuthGroupData authGroup) {
            log.info("Saving auth group " + authGroup.getName());
            authGroupService.addAuthGroup(authGroup);
            return ResponseEntity.status(HttpStatus.CREATED).body(authGroup);
    }


    @Operation(summary = "Get a Auth group by its ID")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Found the auth group", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AuthGroupData.class))}),
            @ApiResponse(responseCode = "404", description = "Auth group not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error getting the auth group details.", content = @Content)})
    @GetMapping(path = "{id}")
    public ResponseEntity<Object> getAuthGroup(@PathVariable(value = "id") Long id) {
        log.info("Get the authgroup by Id = " + id);
        final Optional<AuthGroupData> authGroup = authGroupService.getAuthGroup(id);
        if (authGroup.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(authGroup.get());
    }

    @Operation(summary = "Get all Auth groups")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Found the auth groups", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AuthGroupData.class))}),
            @ApiResponse(responseCode = "500", description = "Error getting the auth group details.", content = @Content)})
    @GetMapping
    public ResponseEntity<Object> getAllAuthGroups() {
        log.info("Get the all auth groups");
        final Iterable<AuthGroupData> node = authGroupService.getAllAuthGroups();
        return ResponseEntity.ok(node);
    }

    @Operation(summary = "Update the Auth group")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Updated the auth group", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AuthGroupData.class))}),
            @ApiResponse(responseCode = "404", description = "Auth group not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error updating the auth group", content = @Content)})
    @PutMapping
    public ResponseEntity<Object> updateAuthGroup(@Valid @RequestBody AuthGroupData authGroup) {
        log.info("Update the auth group " + authGroup);
        final Optional<Long> node = authGroupService.updateAuthGroup(authGroup);
        if (node.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Long> nodeProfileList = authGroupService.getNodeProfileByAuthGroup(authGroup.getId());
        nodeProfileList.addAll(nodeProfileService.getNodeProfileByAuthGroup(authGroup.getName()));
        log.info("Update the node profile related files associated with auth group. " +
                "Associated node profile size " + nodeProfileList.size());
        log.debug("Node profile ids {} associated with auth group {} ", Arrays.toString(nodeProfileList.toArray()), authGroup);
        nodeProfileList.forEach(nodeProfile -> {
            log.info("Updating the node profile related files associated with id {} ", nodeProfile);
            Optional<NodeProfileData> nodeProfileDataOptional = nodeProfileService.getNodeProfile(nodeProfile);
            nodeProfileDataOptional.ifPresent(nodeProfileData -> nodeProfileService.populatePreConfigFiles(nodeProfileData));
        });
        return ResponseEntity.ok(authGroup);
    }

    @Operation(summary = "Delete a Auth group by its ID")
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Auth group is deleted", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AuthGroupData.class))}),
            @ApiResponse(responseCode = "404", description = "Auth group not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error deleting the auth group.", content = @Content)})
    @DeleteMapping
    public ResponseEntity<Object> deleteAuthGroup(@RequestParam(value = "id") Long id) {
        log.info("Delete the auth group by Id = " + id);
        final Optional<Long> output = authGroupService.deleteAuthGroup(id);
        if (output.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
