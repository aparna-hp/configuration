package com.cisco.configService.controller;


import com.cisco.configService.model.preConfig.*;
import com.cisco.configService.service.NodeListService;
import com.cisco.configService.service.NodeProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


@RestController
@CrossOrigin
@RequestMapping(path = {"/api/v1/node-profiles/node-lists"}, produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class NodeListController {

    @Autowired
    NodeListService nodeListService;

    @Autowired
    NodeProfileService nodeProfileService;

    @Operation(summary = "Add node lists to a node profile")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Node lists are added to the profile", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = NodeListResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Error adding the node lists to the node profile.", content = @Content),
            @ApiResponse(responseCode = "207", description = "Snmp Group or Auth group mismatch occurred or invalid ip address found for few of the node lists.", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = NodeListResponse.class))})})
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> addNodeListsToProfile(@RequestParam Long nodeProfileId,
                                                        @RequestBody List<NodeListData> nodeListData) {
        log.info("Adding node lists to the  NodeProfile " + nodeProfileId);
        NodeListResponse nodeListResponse = nodeListService.addUpdateNodeListsToProfile(nodeProfileId, nodeListData);
        log.info("Update the network profile files belonging to  ID " + nodeProfileId);
        Optional<NodeProfileData> nodeProfileDataOptional = nodeProfileService.getNodeProfile(nodeProfileId);
        nodeProfileDataOptional.ifPresent(nodeProfileData -> nodeProfileService.populatePreConfigFiles(nodeProfileData));
        HttpStatus httpStatus  = nodeListResponse.getStatus() ? HttpStatus.CREATED : HttpStatus.MULTI_STATUS;
        return ResponseEntity.status(httpStatus).body(nodeListResponse);
    }

    @Operation(summary = "Import node lists to a node profile using the CSV file")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Node lists are added to the profile", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = NodeListResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Error adding the node lists to the node profile.", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid CSV file format.", content = @Content),
            @ApiResponse(responseCode = "207", description = "Snmp Group or Auth group mismatch occurred or invalid ip address found for few of the node lists.", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = NodeListResponse.class))})})
    @PostMapping(path = "/import", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Object> importNodeListsToProfile(@RequestParam Long nodeProfileId,
                                                           @RequestPart(value = "csvFile") MultipartFile multipartFile) {
        log.info("Importing node lists to the  NodeProfile " + nodeProfileId);
        List<NodeListData> nodeLists = new ArrayList<>();
        NodeListResponse nodeListResponse = nodeListService.importNodeLists(nodeProfileId, multipartFile, nodeLists);
        log.info("Update the network profile files belonging to  ID " + nodeProfileId);
        Optional<NodeProfileData> nodeProfileDataOptional = nodeProfileService.getNodeProfile(nodeProfileId);
        nodeProfileDataOptional.ifPresent(nodeProfileData -> nodeProfileService.populatePreConfigFiles(nodeProfileData));
        HttpStatus httpStatus  = nodeListResponse.getStatus() ? HttpStatus.CREATED : HttpStatus.MULTI_STATUS;
        return ResponseEntity.status(httpStatus).body(nodeListResponse);
    }

    @Operation(summary = "Update node lists to a node profile")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Node lists are updated to the profile", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = NodeListResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Error updating the node lists to the node profile.", content = @Content),
            @ApiResponse(responseCode = "207", description = "Snmp Group or Auth group mismatch occurred or invalid ip address found for few of the node lists.", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = NodeListResponse.class))})})
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> updateNodeListsToProfile(@RequestParam Long nodeProfileId,
                                                              @RequestBody List<NodeListData> nodeListData) {
        log.info("Updating node lists to the  NodeProfile " + nodeProfileId);
        NodeListResponse nodeListResponse = nodeListService.addUpdateNodeListsToProfile(nodeProfileId,nodeListData);
        log.info("Update the network profile files belonging to  ID " + nodeProfileId);
        Optional<NodeProfileData> nodeProfileDataOptional = nodeProfileService.getNodeProfile(nodeProfileId);
        nodeProfileDataOptional.ifPresent(nodeProfileData -> nodeProfileService.populatePreConfigFiles(nodeProfileData));
        HttpStatus httpStatus  = nodeListResponse.getStatus() ? HttpStatus.OK : HttpStatus.MULTI_STATUS;
        return ResponseEntity.status(httpStatus).body(nodeListResponse);
    }


    @Operation(summary = "Get a Paginated Node List by node profile ID")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Found the Node Lists", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = NodeListDataPages.class))}),
            @ApiResponse(responseCode = "500", description = "Error getting the node lists belonging to the node profile.", content = @Content)})
    @PutMapping("/filteredList")
    public ResponseEntity<Object> getPaginatedSortedNodeList(@RequestParam(value = "nodeProfileId") Long nodeProfileId,
                                                             @RequestParam(value = "size", required = false) Integer size,
                                                             @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
                                                             @RequestParam(value = "sortColumn", required = false)  String sortColumn ,
                                                             @RequestParam(value = "isAscOrder", required = false) Boolean isAscOrder,
                                                             @RequestBody(required = false) List<FilterCriteria> filterCriteria) {
        log.info("Get the paginated,sorted and filtered node list by Node Profile  Id = " + nodeProfileId);
        NodeListDataPages nodeListDataPages = new NodeListDataPages();
        long count = nodeListService.getNodeListCount(nodeProfileId, filterCriteria);
        nodeListDataPages.setCount(count);
        if(count >0) {
            nodeListDataPages.setNodeLists(nodeListService.getPaginatedNodeList
                    (nodeProfileId, sortColumn, isAscOrder, size, pageNumber, filterCriteria));
        }
        return ResponseEntity.ok(nodeListDataPages);
    }

    @Operation(summary = "Get the count of Node List by node profile ID")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Node Lists count returned",
            content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FilterCriteria.class))}),
            @ApiResponse(responseCode = "500", description = "Error getting the node lists count belonging to the node profile.", content = @Content)})
    @PutMapping(path = "/count", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getNodeListCount(@RequestParam(value = "nodeProfileId") Long nodeProfileId,
                                                   @RequestBody(required = false) List<FilterCriteria> filterCriteria) {
        log.info("Get the count of node list by Node Profile  Id = " + nodeProfileId);
        final Long nodeListCount = nodeListService.getNodeListCount(nodeProfileId, filterCriteria);
        return ResponseEntity.ok(nodeListCount);
    }

    @Operation(summary = "Delete node lists by its ID")
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Node Lists are deleted", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,  array = @ArraySchema(schema = @Schema(implementation = Long.class)))}),
            @ApiResponse(responseCode = "500", description = "Error deleting the node lists", content = @Content)})
    @DeleteMapping
    public ResponseEntity<Object> deleteNodeLists(@RequestBody List<Long> nodeListIds) {
        log.info("Delete the Node List by Id = " + Arrays.toString(nodeListIds.toArray()));
        nodeListService.deleteNodeListsOfNodeProfile(nodeListIds);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete all node lists belonging to node profile.")
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "All Node Lists are deleted", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error deleting the node lists", content = @Content)})
    @DeleteMapping(path = "/node-profile")
    public ResponseEntity<Object> deleteAllNodeLists(@RequestParam(value = "nodeProfileId") Long nodeProfileId) {
        log.info("Delete all the Node List belonging to node profile Id = " + nodeProfileId);
        nodeListService.deleteAllNodeListsOfNodeProfile(nodeProfileId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Export all the node list belonging to the node profile.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Node list exported successfully. ",
                    content = {@Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(implementation = Resource.class))}),
            @ApiResponse(responseCode = "500", description = "Error exporting the node list.", content = @Content)})
    @GetMapping(path = "/export", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<Object> exportNodeList(@RequestParam(value = "nodeProfileId") Long nodeProfileId) {
        log.info("Exporting the node list belonging to the node profile ." + nodeProfileId);
        String configurations = nodeListService.getAllNodeListsOfProfile(nodeProfileId);
        if (configurations != null && !configurations.isEmpty()){
            Resource resource = new InputStreamResource(new ByteArrayInputStream(configurations.getBytes()));
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(MediaType.TEXT_PLAIN_VALUE))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"node_list.csv\"")
                    .body(resource);
        } else {
            return ResponseEntity.noContent().build();
        }
    }
}
