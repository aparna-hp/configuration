package com.cisco.configService.controller;


import com.cisco.configService.enums.CollectorTypes;
import com.cisco.configService.service.FileGatewayService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;


@RestController
@CrossOrigin
@RequestMapping(path = {"/api/v1/file-gateway"}, produces = MediaType.TEXT_PLAIN_VALUE)
@Slf4j
public class FileGatewayController {

    @Autowired
    FileGatewayService fileGatewayService;

    @Operation(summary = "Upload a file in the shared file system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Upload a file from file system",
                    content = {@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "500", description = "Error uploading the file", content = @Content)})
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<Object> uploadFile(@RequestPart(value = "file") MultipartFile multipartFile,
                                             @RequestPart(value = "fileName", required = false) String fileName,
                                             @RequestParam(value = "filePath", required = false) String filePath) {

        String response = fileGatewayService.upload(multipartFile, filePath,fileName);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Download the plan file from the shared file system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Download a plan-file from storage. ",
                    content = {@Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = Resource.class))}),
            @ApiResponse(responseCode = "204", description = "The plan file is not found.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error downloading the plan file", content = @Content)})
    @GetMapping(path = "/plan-file", produces = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Resource> downloadPlanFile(@RequestParam(value = "networkId") Long networkId,
                                                   @RequestParam(value = "collectorId") Long collectorId,
                                                 @RequestParam(value = "collectorType") CollectorTypes collectorType) {
        InputStream downloadedFile =  fileGatewayService.downloadPlanFile(networkId, collectorId, collectorType.name());
        if (downloadedFile != null)
        {
            Resource resource = new InputStreamResource(downloadedFile);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(MediaType.MULTIPART_FORM_DATA_VALUE))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + collectorId + "_" +  collectorType.name() + ".db\"")
                    .body(resource);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @Operation(summary = "Download the file from the stored file system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Download a file from shared file system. ",
                    content = {@Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(implementation = Resource.class))}),
            @ApiResponse(responseCode = "204", description = "The file is not found in the shared file system.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error downloading the file", content = @Content)})
    @GetMapping
    public ResponseEntity<Resource> downloadFile(@RequestParam(value = "fileName") String fileName,
                                                 @RequestParam(value = "filePath", required = false) String filePath) {
        InputStream downloadedFile =  fileGatewayService.downloadFile(filePath,fileName);
        if (downloadedFile != null)
        {
            Resource resource = new InputStreamResource(downloadedFile);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(MediaType.TEXT_PLAIN_VALUE))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + fileName + "\"")
                    .body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Delete a file from the shared file system." )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "File delete is successful. ",
                    content = {@Content(mediaType = MediaType.TEXT_PLAIN_VALUE)}),
            @ApiResponse(responseCode = "500", description = "Error deleting the file", content = @Content)})
    @DeleteMapping
    public ResponseEntity<Object> deleteFile(@RequestParam(value = "fileName") String fileName,
                                                 @RequestParam(value = "filePath", required = false) String filePath) {

        if (fileGatewayService.delete(filePath,fileName)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.internalServerError().build();

    }

    @Operation(summary = "Get the final aggregated plan file path given the network name." +
            " If collector type or collector name is specified, the corresponding collector plan file path will be returned.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Plan file path returned successfully", content = @Content),
            @ApiResponse(responseCode = "404", description = "Plan file not present", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error fetching the plan file path ", content = @Content)})
    @GetMapping(path = "/plan-file-path", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getPlanFilePath (@RequestParam String networkName,
                                                   @RequestParam(required = false) CollectorTypes collectorType,
                                                   @RequestParam(required = false) String collectorName) {

        log.info("Get the plan file path for the network {} with optional parameters collector type {} and collector name {} "
                , networkName, collectorType, collectorName);
        List<String> planFilePaths = fileGatewayService.getPlanFilePath(networkName, collectorType, collectorName);
        return ResponseEntity.ok(planFilePaths);
    }

}
