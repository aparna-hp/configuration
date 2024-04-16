package com.cisco.configService.controller;


import com.cisco.collectionService.utils.StringUtil;
import com.cisco.configService.entity.ImportHistory;
import com.cisco.configService.model.AllConfigurations;
import com.cisco.configService.model.common.ResponseMessage;
import com.cisco.configService.service.ConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;


@RestController
@Validated
@CrossOrigin
@RequestMapping(path = {"/api/v1/configurations"}, produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class ConfigurationController {

    @Autowired
    ConfigurationService configurationService;

    @Operation(summary = "Import the collector configuration file.")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Configurations imported successfully", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ImportHistory.class))}),
            @ApiResponse(responseCode = "500", description = "Error importing the configurations.", content = @Content)})
    @PostMapping(path = "/import", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Object> importConfig(@Valid @RequestBody MultipartFile configFile,
                                               @RequestParam(value = "override", required = false) boolean override,
                                               @RequestParam(value = "importType", required = false) ImportHistory.Type importType) {
        log.info("Importing Configuration {} with override flag {} and importType {}" , configFile, override, importType);
        ImportHistory importHistory = configurationService.importConfigurations(configFile, importType, override);
        return ResponseEntity.status(HttpStatus.CREATED).body(importHistory);
    }

    @Operation(summary = "Import the CP configurations in JSON format")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Configurations imported successfully", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AllConfigurations.class))}),
            @ApiResponse(responseCode = "500", description = "Error importing the configurations.", content = @Content),
            @ApiResponse(responseCode = "207", description = "Few configurations could not be imported. Please verify the error report.", content = @Content)})
    @PostMapping(path = "/import/cp",  consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> importCPConfigJson(@Valid @RequestBody AllConfigurations configurations,
                                               @RequestParam(value = "override", required = false) boolean override) {
        log.info("Importing Configuration " + configurations);
        String report = configurationService.importCPConfigurations(configurations, override);
        HttpStatus httpStatus = StringUtil.isEmpty(report) ? HttpStatus.CREATED : HttpStatus.MULTI_STATUS ;
        return ResponseEntity.status(httpStatus).body(report);
    }

    @Operation(summary = "Import the CP collector configuration file")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Configurations imported successfully", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error importing the configurations.", content = @Content),
            @ApiResponse(responseCode = "207", description = "Few configurations could not be imported. Please verify the error report.", content = @Content)})
    @PostMapping(path = "/import/cp/file",  consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Object> importCpConfigFile(@Valid @RequestBody MultipartFile configFile,
                                               @RequestParam(value = "override", required = false) boolean override) {
        log.info("Importing Configuration " + configFile);
        String report = configurationService.importCPConfigurations(configFile, override);
        HttpStatus httpStatus = StringUtil.isEmpty(report) ? HttpStatus.CREATED: HttpStatus.MULTI_STATUS ;
        return ResponseEntity.status(httpStatus).body(report);
    }

    @Operation(summary = "Export the configurations")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Configurations exported successfully",
            content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AllConfigurations.class))}),
            @ApiResponse(responseCode = "500", description = "Error exporting the configurations.", content = @Content)})
    @GetMapping
    public ResponseEntity<Object> exportConfig() {
        log.info("Exporting Configurations");
        String configurations = configurationService.exportConfig();
        return ResponseEntity.ok(configurations);
    }

    @Operation(summary = "Get import history.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Configuration import history returned successfully",
            content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ImportHistory.class))}),
            @ApiResponse(responseCode = "500", description = "Error getting the configuration history.", content = @Content)})
    @GetMapping(path = "/history")
    public ResponseEntity<Object> getHistory() {
        log.info("Get the import configuration history.");
        List<ImportHistory> history = configurationService.getHistory();
        return ResponseEntity.ok(history);
    }

    @Operation(summary = "Download the configurations.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configurations downloaded successfully. ",
                    content = {@Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(implementation = Resource.class))}),
            @ApiResponse(responseCode = "500", description = "Error downloading the network access file", content = @Content)})
    @GetMapping(path = "/download", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<Object> downloadNetworkAccess() {
        log.info("Download the configuration details.");
        String configurations =  configurationService.exportConfig();
        if (configurations != null && !configurations.isEmpty()){
            Resource resource = new InputStreamResource(new ByteArrayInputStream(configurations.getBytes()));
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(MediaType.TEXT_PLAIN_VALUE))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"configurations.txt\"")
                    .body(resource);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @Operation(summary = "Clear the history. If ID is not mentioned, then all the entries are deleted.")
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Import history is/are deleted", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error deleting the history.", content = @Content)})
    @DeleteMapping(path = "/import-history")
    public ResponseEntity<Object> deleteHistory(@RequestParam(value = "id", required = false) Long id) {
        log.info("Delete import history");
        configurationService.deleteImportHistory(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Clear the collector configurations")
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "All configurations are deleted", content =
            {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResponseMessage.class))}),
            @ApiResponse(responseCode = "500", description = "Error deleting the configurations.", content = @Content)})
    @DeleteMapping
    public ResponseEntity<Object> deleteConfig() {
        log.info("Delete configurations");
        configurationService.clearConfigurations();
      /*  ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setStatus(true);
        responseMessage.setMessage("Configuration deleted Successfully.");*/
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Import the configurations file from WAE")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Below Configurations are imported successfully : " +
            "Auth groups,SNMP groups,network access,node filter configurations, sr pce agent and parse config agent configurations, " +
            "network configurations part of the composer workflow, schedulers configurations and the Archive configurations",
            content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResponseMessage.class))}),
            @ApiResponse(responseCode = "500", description = "Error importing the configurations.", content = @Content),
            @ApiResponse(responseCode = "207", description = "Few configurations could not be imported. Please verify the error report.", content = @Content)})
    @PostMapping(path = "/import/wae" , consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> importFromWae(@RequestPart(value = "file") MultipartFile multipartFile,
                                                @RequestParam(value="override", required = false) boolean override) {
        log.info("Importing Configuration from " + multipartFile.getOriginalFilename());
        String report = configurationService.migrateConfigurations(multipartFile, override);
        log.debug("Migration error report " + report);
        HttpStatus httpStatus = StringUtil.isEmpty(report) ? HttpStatus.CREATED : HttpStatus.MULTI_STATUS ;
        return ResponseEntity.status(httpStatus).body(" Please note that the credentials are not imported and needs to be re-configured." + report);
    }
}
