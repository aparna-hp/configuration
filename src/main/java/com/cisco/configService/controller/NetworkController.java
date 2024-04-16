package com.cisco.configService.controller;


import com.cisco.configService.model.composer.NetworkApiResponse;
import com.cisco.configService.model.composer.NetworkDataInfo;
import com.cisco.configService.model.composer.NetworkDataView;
import com.cisco.configService.service.NetworkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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

import java.util.List;
import java.util.Optional;


@RestController
@Validated
@CrossOrigin
@RequestMapping(path = {"/api/v1/networks"}, produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class NetworkController {

    @Autowired
    NetworkService networkService;

    @Operation(summary = "Create a new network")
    @ApiResponses(value = {@ApiResponse(content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = NetworkApiResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Error creating the network.", content = @Content)})
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createNetwork(@Valid @RequestBody NetworkDataView networkData) {
            log.info("Saving network " + networkData.getName());
            NetworkApiResponse networkApiResponse = networkService.saveNetwork(networkData);
            return ResponseEntity.status(HttpStatus.CREATED).body(networkApiResponse);
    }

    @Operation(summary = "Get all the networks along with its status required for Dashboard landing page.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Found the status of the network", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = NetworkDataInfo.class)))}),
            @ApiResponse(responseCode = "500", description = "Error getting the network status", content = @Content)})
    @GetMapping(path = "/network-status")
    public ResponseEntity<Object> getNetworkStatus() {
        log.info("Get all the networks along with its status");
        final List<NetworkDataInfo> networkStatus = networkService.getNetworkStatus();
        return ResponseEntity.ok(networkStatus);
    }


    @Operation(summary = "Get a Network by its ID")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Found the network", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = NetworkDataView.class))}),
            @ApiResponse(responseCode = "404", description = "Network not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error getting the network.", content = @Content)})
    @GetMapping(path = "{id}")
    public ResponseEntity<Object> getNetwork(@PathVariable(value = "id") Long id) {
        log.info("Get the network by Id = " + id);
        final Optional<NetworkDataView> networkData = networkService.getNetwork(id);
        if (networkData.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(networkData.get());
    }


    @Operation(summary = "Get all Network name and ID.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Found the node", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = NetworkDataInfo.class))}),
            @ApiResponse(responseCode = "500", description = "Error getting the network details", content = @Content)})
    @GetMapping
    public ResponseEntity<Object> getAllNetworks() {
        log.info("Get the all networks");
        final List<NetworkDataInfo> allNetworkData = networkService.getAllNetworkData();
        return ResponseEntity.ok(allNetworkData);
    }

    @Operation(summary = "Update the Network")
    @ApiResponses(value = {@ApiResponse(content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = NetworkApiResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Error updating the network.", content = @Content)})
    @PutMapping
    public ResponseEntity<Object> updateNetwork(@Valid @RequestBody NetworkDataView networkData) {
        log.info("Update the network " + networkData);
        NetworkApiResponse networkApiResponse = networkService.updateNetwork(networkData);
        return ResponseEntity.ok(networkApiResponse);
    }

    @Operation(summary = "Delete a Network by its ID")
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Network is deleted", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = NetworkDataView.class))}),
            @ApiResponse(responseCode = "404", description = "Network not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error deleting the network", content = @Content)})
    @DeleteMapping
    public ResponseEntity<Object> deleteNetwork(@RequestParam(value = "id") Long id) {
        log.info("Delete network with Id = " + id);
        final Optional<Long> networkId = networkService.deleteNetwork(id);
        if (networkId.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
