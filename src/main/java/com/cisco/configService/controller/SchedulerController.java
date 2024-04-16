package com.cisco.configService.controller;


import com.cisco.configService.enums.WorkFlowManagerActions;
import com.cisco.configService.model.composer.ApiResponseDetail;
import com.cisco.configService.model.scheduler.SchedulerConfigData;
import com.cisco.configService.service.SchedulerService;
import com.cisco.workflowmanager.*;
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
@RequestMapping(path = {"/api/v1/schedulers"}, produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class SchedulerController {

    @Autowired
    SchedulerService schedulerService;

    @Operation(summary = "Create a new scheduler")
    @ApiResponses(value = {@ApiResponse(content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = ApiResponseDetail.class))}),
            @ApiResponse(responseCode = "500", description = "Error creating the schedulers.", content = @Content)
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createScheduler(@Valid @RequestBody SchedulerConfigData schedulerConfigDataList) {
            List<ApiResponseDetail> apiResponseDetails = schedulerService.addUpdateScheduler(List.of(schedulerConfigDataList), false);
            return ResponseEntity.status(HttpStatus.CREATED).body(apiResponseDetails.get(0));
    }

    @Operation(summary = "Update the scheduler config")
    @ApiResponses(value = {@ApiResponse(content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = ApiResponseDetail.class))}),
            @ApiResponse(responseCode = "500", description = "Error updating the schedulers.", content = @Content)
    })
    @PutMapping
    public ResponseEntity<Object> updateScheduler(@Valid @RequestBody SchedulerConfigData schedulerConfigDataList) {
        List<ApiResponseDetail> apiResponseDetails = schedulerService.addUpdateScheduler(List.of(schedulerConfigDataList), true);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(apiResponseDetails.get(0));
    }

    @Operation(summary = "Get the schedulers belonging to the network.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Found the schedulers status for the network", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = SchedulerConfigData.class)))}),
            @ApiResponse(responseCode = "500", description = "Error getting the status of the schedulers.", content = @Content)})
    @GetMapping
    public ResponseEntity<Object> getSchedulersOfNetwork(@RequestParam(value = "networkId") Long networkId) {
        log.info("Get all the schedulers belonging to the network {}" , networkId);
        List<SchedulerConfigData> schedulerConfigData = schedulerService.getSchedulersOfNetwork(networkId);
        return ResponseEntity.ok(schedulerConfigData);
    }

    @Operation(summary = "Get the scheduler config given the Id.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Found the scheduler", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SchedulerConfigData.class))}),
            @ApiResponse(responseCode = "404", description = "Scheduler is not yet created", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error getting the scheduler details.", content = @Content)})
    @GetMapping(path = "{id}")
    public ResponseEntity<Object> getScheduler(@PathVariable(value = "id") Long id) {
        log.info("Get the scheduler with the Id {}" , id);
        Optional<SchedulerConfigData> schedulerConfigData = schedulerService.getScheduler(id);
        if(schedulerConfigData.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(schedulerConfigData);
    }

    @Operation(summary = "Get the scheduler status for the given network.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Scheduler status returned successfully.", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SchedulerConfigData.class))}),
            @ApiResponse(responseCode = "404", description = "Scheduler status not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error getting the scheduler status.", content = @Content)})
    @GetMapping(path = "/status/network")
    public ResponseEntity<Object> getSchedulerStatusOfNetwork(@RequestParam(value = "networkId") Long networkId) {
        log.info("Get the scheduler status associated with the network Id {}" , networkId);
        NetworkStatus networkStatus = schedulerService.getNetworkStatus(networkId);
        return ResponseEntity.ok(networkStatus);
    }

    @Operation(summary = "Get the status history of all the tasks under the Job.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Found the tasks' history for the scheduler", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = JobTaskHistory.class))}),
            @ApiResponse(responseCode = "404", description = "Scheduler is not yet created", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error getting the task history of the scheduler.", content = @Content)})
    @GetMapping(path = "/task-history")
    public ResponseEntity<Object> getTaskHistory(@RequestParam(value = "schedulerId") Long schedulerId) {
        log.info("Get the status history of all the tasks under the Job {}." , schedulerId);
        final Optional<JobTaskHistory> taskHistory = schedulerService.getTaskHistory(schedulerId);
        if (taskHistory.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(taskHistory.get());
    }

    @Operation(summary = "Execute the scheduler action like execute, pause, resume and abort Job.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The scheduler action is successful.", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))}),
            @ApiResponse(responseCode = "404", description = "Scheduler action not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error executing the Scheduler action", content = @Content)})
    @PutMapping(path = "/execute-actions")
    public ResponseEntity<Object> runTool(@RequestParam(value = "schedulerId") Long schedulerId,
                                          @RequestParam(value = "actionName") WorkFlowManagerActions action) {
        log.info("Run the collector with id {} and action {}", schedulerId, action);
        if (schedulerService.executeActions(schedulerId,action)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Schedule Aggregator Resync action on the given Networks")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Resync action successfully scheduled on the network.", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = NetworkResyncInfo.class))}),
            @ApiResponse(responseCode = "500", description = "Error scheduling the resync on the network.", content = @Content)})
    @PostMapping(path = "/resync")
    public ResponseEntity<Object> resyncNetwork(@Valid @RequestBody NetworkResyncInfo networkResyncInfo) {
        log.info("Execute resync on the networks {} with id {} ", networkResyncInfo.getNetworkId()
                ,networkResyncInfo.getNetworkId() );
        Optional<NetworkResyncInfo> networkResyncInfoOptional = schedulerService
                .rsyncNetwork(networkResyncInfo);
        if(networkResyncInfoOptional.isPresent()){
            return ResponseEntity.status(HttpStatus.CREATED).body(networkResyncInfoOptional.get());
        } else {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Get the resync scheduler information for all the networks.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Found the resync schedulers", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = NetworkResyncInfo.class)))}),
            @ApiResponse(responseCode = "500", description = "Error getting the resync schedulers.", content = @Content)})
    @GetMapping(path = "/resync")
    public ResponseEntity<Object> getNetworkRsyncSchedulers() {
        log.info("Get all the resync schedulers");
        List<JobInfo> resyncJobInfoList = schedulerService.getNetworkRsyncInfo();
        return ResponseEntity.ok(resyncJobInfoList);
    }

    @Operation(summary = "Get the scheduler statistics required for the dashboard.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Scheduler statistics returned successfully.", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = JobTaskHistory.class))}),
            @ApiResponse(responseCode = "404", description = "Scheduler statistics is not found.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error getting the scheduler statistics .", content = @Content)})
    @GetMapping(path = "/statistics")
    public ResponseEntity<Object> getSchedulerStatistics() {
        log.info("Get the scheduler statistics.");
        final Optional<JobStats> jobStats = schedulerService.getSchedulerStatistics();
        if (jobStats.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(jobStats.get());
    }

    @Operation(summary = "Delete a scheduler by its ID")
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Scheduler is deleted", content = @Content),
            @ApiResponse(responseCode = "404", description = "Scheduler not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error deleting the scheduler", content = @Content)})
    @DeleteMapping
    public ResponseEntity<Object> deleteScheduler(@RequestParam(value = "id") Long id) {
        log.info("Delete scheduler with Id = " + id);
        boolean status  = schedulerService.deleteScheduler(id);
        if (status) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.internalServerError().build();
    }
}
