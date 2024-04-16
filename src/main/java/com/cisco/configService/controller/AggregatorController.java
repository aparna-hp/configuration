package com.cisco.configService.controller;


import com.cisco.configService.model.aggregator.Purge;
import com.cisco.configService.service.AggregatorService;
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
import java.util.Optional;


@RestController
@CrossOrigin
@RequestMapping(path = {"/api/v1/aggregator"}, produces = MediaType.TEXT_PLAIN_VALUE)
@Slf4j
public class AggregatorController {

    @Autowired
    AggregatorService aggregatorService;

    @Operation(summary = "Get the global aggregator properties details.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The global aggregator properties returned successfully", content = {@Content(mediaType = MediaType.TEXT_PLAIN_VALUE)}),
            @ApiResponse(responseCode = "500", description = "Error getting the global aggregator properties details", content = @Content)})
    @GetMapping
    public ResponseEntity<Object> getAggrConfig() {
        log.info("Get the global aggregator properties details.");
        Optional<String> aggrConfig = aggregatorService.getAggrConfig();
        if(aggrConfig.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(aggrConfig.get());
    }

    @Operation(summary = "Get the purge configuration details.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The purge configuration details returned successfully", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Purge.class))}),
            @ApiResponse(responseCode = "500", description = "Error getting the purge configuration details", content = @Content)})
    @GetMapping(path = "/purge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getPurgeConfig() {
        log.info("Get the purge configuration details.");
        return ResponseEntity.ok(aggregatorService.getPurgeConfig());
    }

    @Operation(summary = "Download the aggregator config.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The global aggregator properties downloaded successfully. ",
                    content = {@Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(implementation = Resource.class))}),
            @ApiResponse(responseCode = "500", description = "Error downloading the global aggregator properties file", content = @Content)})
    @GetMapping(path = "/download")
    public ResponseEntity<Object> downloadAggrConfig() {
        log.info("Download the global aggregator properties details.");
        InputStream downloadedFile =  aggregatorService.downloadAggrConfig();
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


    @Operation(summary = "Update the global aggregator properties details.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Updated the global aggregator properties", content = {@Content(mediaType = MediaType.TEXT_PLAIN_VALUE)}),
            @ApiResponse(responseCode = "500", description = "Error updating the global aggregator properties", content = @Content)})
    @PutMapping
    public ResponseEntity<Object> updateAggregatorConfig(@RequestBody String aggrConfig) {
        log.info("Update the global aggregator properties.");
        boolean success = aggregatorService.updateAggrConfig(aggrConfig);
        if (success) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.internalServerError().build();
    }

    @Operation(summary = "Update the purge configuration details.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Updated the purge configuration successfully.", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Purge.class))}),
            @ApiResponse(responseCode = "500", description = "Error updating the purge configuration", content = @Content)})
    @PutMapping(path = "/purge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> updatePurgerConfig(@RequestBody Purge purgeConfig) {
        log.info("Update the purge configurations.");
        boolean success = aggregatorService.updatePurgeConfig(purgeConfig);
        if (success) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.internalServerError().build();
    }

    @Operation(summary = "Reset the global aggregator properties details.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The global aggregator properties reset is successful", content = {@Content(mediaType = MediaType.TEXT_PLAIN_VALUE)}),
            @ApiResponse(responseCode = "500", description = "Error resetting the global aggregator properties", content = @Content)})
    @PutMapping(path = "/reset")
    public ResponseEntity<Object> resetAggregatorConfig() {
        log.info("Reset global aggregator properties.");
        boolean success = aggregatorService.resetAggrConfig();
        if (success) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.internalServerError().build();
    }

}
