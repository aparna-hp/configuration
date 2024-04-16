package com.cisco.configService.controller;


import com.cisco.configService.service.NetworkAccessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;


@RestController
@CrossOrigin
@RequestMapping(path = {"/api/v1/network-access"}, produces = MediaType.TEXT_PLAIN_VALUE)
@Slf4j
public class NetworkAccessController {

    @Autowired
    NetworkAccessService networkAccessService;

    @Operation(summary = "Get the network access details.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The Network Access returned successfully", content = {@Content(mediaType = MediaType.TEXT_PLAIN_VALUE)}),
            @ApiResponse(responseCode = "500", description = "Error getting the Network Access details", content = @Content)})
    @GetMapping
    public ResponseEntity<Object> getNetworkAccess() {
        log.info("Get the Network access details.");
        String networkAccess = networkAccessService.getNetworkAccess();
        return ResponseEntity.ok(networkAccess);
    }

    @Operation(summary = "Download the network access.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Network access downloaded successfully. ",
                    content = {@Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(implementation = Resource.class))}),
            @ApiResponse(responseCode = "500", description = "Error downloading the network access file", content = @Content)})
    @GetMapping(path = "/download")
    public ResponseEntity<Object> downloadNetworkAccess() {
        log.info("Download the Network access details.");
        InputStream downloadedFile =  networkAccessService.downloadNetworkAccess();
        if (downloadedFile != null){
            Resource resource = new InputStreamResource(downloadedFile);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(MediaType.TEXT_PLAIN_VALUE))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"network-access.txt\"")
                    .body(resource);
        } else {
            return ResponseEntity.noContent().build();
        }
    }


    @Operation(summary = "Update the network access details.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Updated the Network Access", content = {@Content(mediaType = MediaType.TEXT_PLAIN_VALUE)}),
            @ApiResponse(responseCode = "500", description = "Error updating the Network Access", content = @Content)})
    @PutMapping
    public ResponseEntity<Object> updateNetworkAccess(@RequestBody String networkAccess) {
        log.info("Update the Network access for all node profiles.");
        boolean success = networkAccessService.updateNetworkAccess(networkAccess);
        if (success) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.internalServerError().build();
    }

    @Operation(summary = "Reset the network access details.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The Network Access reset is successful", content = {@Content(mediaType = MediaType.TEXT_PLAIN_VALUE)}),
            @ApiResponse(responseCode = "500", description = "Error resetting the Network Access", content = @Content)})
    @PutMapping(path = "/reset")
    public ResponseEntity<Object> resetNetworkAccess() {
        log.info("Reset the Network access.");
        boolean success = networkAccessService.resetNetworkAccess();
        if (success) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.internalServerError().build();
    }

}
