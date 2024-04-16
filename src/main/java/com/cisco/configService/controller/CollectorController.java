package com.cisco.configService.controller;

import com.cisco.configService.enums.CollectorTypes;
import com.cisco.configService.model.composer.CollectorDataView;
import com.cisco.configService.model.composer.cli.CollectorData;
import com.cisco.configService.service.CollectorService;
import com.cisco.configService.service.CollectorValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
@CrossOrigin
@RequestMapping(path = {"/api/v1/networks/collectors"}, produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class CollectorController {

    @Autowired
    CollectorService collectorService;

    @Autowired
    CollectorValidationService collectorValidationService;

    @Operation(summary = "Get a collector by its ID")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Found the collector", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = com.cisco.configService.model.composer.cli.CollectorData.class))}),
            @ApiResponse(responseCode = "404", description = "Collector not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error getting the collector details.", content = @Content)})
    @GetMapping(path = "{id}")
    public ResponseEntity<Object> getCollector(@PathVariable(value = "id") Long id) {
        log.info("Get the collector by Id = " + id);
        final Optional<CollectorData> node = collectorService.getCollector(id);
        if (node.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(node.get());
    }

    @Operation(summary = "Get all collector types")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Return all Collector Types", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CollectorTypes.class))}),
            @ApiResponse(responseCode = "404", description = "Collector types not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error getting the collector types", content = @Content)})
    @GetMapping(path = "/type")
    public ResponseEntity<Object> getAllCollectorTypes() {
        log.info("Get the all collector types");
        return ResponseEntity.ok(collectorService.getAllCollectorTypes());
    }

    @Operation(summary = "Get the default collector configuration parameters")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Return the default collector parameters.", content = {@Content(mediaType = MediaType.TEXT_PLAIN_VALUE, schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "500", description = "Error getting the collector parameters", content = @Content)})
    @GetMapping(path = "/default-parameters", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<Object> getDefaultCollectorParameters(@RequestParam CollectorTypes collectorType) {
        log.info("Get the default collector configuration parameter for type " + collectorType.name());
        String params = collectorService.getDefaultConfigParams(collectorType);
        log.info("Collector default parameters : " + params);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(MediaType.TEXT_PLAIN_VALUE))
                .body(params);
    }

    @Operation(summary = "Validate the collector parameter")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Collector Parameters are valid", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CollectorData.class))}),
            @ApiResponse(responseCode = "500", description = "Invalid collector parameters", content = @Content)})
    @PutMapping(path = "/validate-collector")
    public ResponseEntity<Object> validateCollector(@Valid @RequestBody CollectorDataView collectorDataView) {
        log.info("Validating collector {} " , collectorDataView);
        collectorValidationService.validateCollectorParams(collectorDataView,true);
        return ResponseEntity.ok(collectorDataView);
    }

}
