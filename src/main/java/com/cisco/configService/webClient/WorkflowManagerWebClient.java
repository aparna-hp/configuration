package com.cisco.configService.webClient;

import com.cisco.configService.AppPropertiesReader;
import com.cisco.configService.exception.CustomException;
import com.cisco.workflowmanager.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;


/**
 * The class uses WebClient to make API calls to Work flow manager.
 */
@Component
@Slf4j
public class WorkflowManagerWebClient extends WebClientImpl{

    @Autowired
    private AppPropertiesReader configuration;

    public WorkflowManagerWebClient(@Value("${app.workflow.baseUrl}")String baseUrl) {
        super(baseUrl);
    }

    public Optional<JobInfo> createScheduler(JobInfo jobInfo) {

        try {
            ResponseEntity<WorkflowResponseEntity<JobInfo>> response =  getWebClient()
                    .post()
                    .uri(configuration.getWorkflowBaseUrl() + configuration.getWorkflowJobUrl())
                    .accept(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(jobInfo))
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<WorkflowResponseEntity<JobInfo>>(){})
                    .block();
            if(null != response && null != response.getBody()) {
                WorkflowResponseEntity<JobInfo> responseEntity = response.getBody();
                if(!response.getStatusCode().is2xxSuccessful()){
                    throw new CustomException(response.getStatusCode(), "Error creating the Job.",
                           responseEntity.getErrorMessage());
                }

                return Optional.of(responseEntity.getData());
            }
        } catch (WebClientRequestException e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating the Job.",
                    "Error creating the Job using workflow manager." +e.getMessage());
        } catch (WebClientResponseException e) {
            String responseBody = e.getResponseBodyAsString();
            HttpStatusCode httpStatus = e.getStatusCode();
            throw  new CustomException(httpStatus, "Error creating the Job.",
                    "Error creating the Job using workflow manager." + responseBody);
        }
        return Optional.empty();
    }

    /*
    This method invokes the workflow manager API to restart/stop agents.
     */
    public Optional<JobInfo> createAgentJob(AgentAction agentAction) {

        try {
            ResponseEntity<WorkflowResponseEntity<JobInfo>> response =  getWebClient()
                    .post()
                    .uri(configuration.getWorkflowBaseUrl() + configuration.getWorkflowAgentJob())
                    .accept(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(agentAction))
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<WorkflowResponseEntity<JobInfo>>(){})
                    .block();
            if(null != response && null != response.getBody()) {
                WorkflowResponseEntity<JobInfo> responseEntity = response.getBody();
                if(!response.getStatusCode().is2xxSuccessful()){
                    throw new CustomException(response.getStatusCode(), "Error creating the agent Job.",
                            responseEntity.getErrorMessage());
                }

                return Optional.of(responseEntity.getData());
            }
        } catch (WebClientRequestException e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating the agent Job.",
                    "Error creating the agent Job using workflow manager." +e.getMessage());
        } catch (WebClientResponseException e) {
            String responseBody = e.getResponseBodyAsString();
            HttpStatusCode httpStatus = e.getStatusCode();
            throw  new CustomException(httpStatus, "Error creating the agent Job.",
                    "Error creating the agent Job using workflow manager." + responseBody);
        }
        return Optional.empty();
    }

    public List<JobInfo> getAllSchedulers() {

        try {
            ResponseEntity<WorkflowResponseEntity> response =  getWebClient()
                    .get()
                    .uri(configuration.getWorkflowBaseUrl() + configuration.getWorkflowJobUrl())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toEntity(WorkflowResponseEntity.class)
                    .block();
            if(null != response && null != response.getBody()) {
                //Reference: Result is a list of LinkedHashMap https://www.baeldung.com/spring-webclient-json-list
                WorkflowResponseEntity<List<LinkedHashMap>> responseEntity = response.getBody();
                log.info("Status Code: " + response.getStatusCode());

                if(!response.getStatusCode().is2xxSuccessful()){
                    throw new CustomException(response.getStatusCode(), "Error getting the Job details.",
                            responseEntity.getErrorMessage());
                }
                List<JobInfo> jobInfos = new ArrayList<>();
                ObjectMapper mapper = new ObjectMapper();
                for(LinkedHashMap map : responseEntity.getData()) {
                    JobInfo jobInfo = mapper.convertValue(map, JobInfo.class);
                    jobInfos.add(jobInfo);
                }
                return jobInfos;
            }
        } catch (WebClientRequestException e) {
            throw  new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error getting the Job details.",
                    "Error getting the Job details from workflow manager." + e.getMessage());
        } catch (WebClientResponseException e) {
            String responseBody = e.getResponseBodyAsString();
            HttpStatusCode httpStatus = e.getStatusCode();
            throw  new CustomException(httpStatus, "Error getting the Job details.",
                    "Error getting the Job details from workflow manager." + responseBody);
        }
        return List.of();
    }

    public Optional<JobInfo> getScheduler(Long id) {

        try {
            ResponseEntity<WorkflowResponseEntity<JobInfo>> response = getWebClient()
                    .get()
                    .uri(configuration.getWorkflowBaseUrl() + configuration.getWorkflowJobUrl() + id)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<WorkflowResponseEntity<JobInfo>>(){})
                    .block();
            if(null != response && null != response.getBody()) {
                WorkflowResponseEntity<JobInfo> responseEntity = response.getBody();
                log.info("Status Code: " + response.getStatusCode());

                if(!response.getStatusCode().is2xxSuccessful()){
                    throw new CustomException(response.getStatusCode(), "Error getting the Job details.",
                            responseEntity.getErrorMessage());
                }
                return Optional.of(responseEntity.getData());
            }
        } catch (WebClientRequestException e) {
            throw  new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error getting the Job details.",
                    "Error getting the Job details from workflow manager." + e.getMessage());
        } catch (WebClientResponseException e) {
            String responseBody = e.getResponseBodyAsString();
            HttpStatusCode httpStatus = e.getStatusCode();
            throw  new CustomException(httpStatus, "Error getting the Job details.",
                    "Error getting the Job details from workflow manager." + responseBody);
        }
        return Optional.empty();
    }

    public Optional<NetworkStatus> getNetworkStatus(Long networkId) {

        try {
            ResponseEntity<WorkflowResponseEntity<NetworkStatus>> response = getWebClient()
                    .get()
                    .uri(configuration.getWorkflowBaseUrl() +
                            configuration.getWorkflowNetworkStatus()  + networkId)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<WorkflowResponseEntity<NetworkStatus>>(){})
                    .block();
            if(null != response && null != response.getBody()) {
                WorkflowResponseEntity<NetworkStatus> responseEntity = response.getBody();
                log.info("Status Code: " + response.getStatusCode());

                if(!response.getStatusCode().is2xxSuccessful()){
                    throw new CustomException(response.getStatusCode(), "Error getting the network status.",
                            responseEntity.getErrorMessage());
                }
                return Optional.of(responseEntity.getData());
            }
        } catch (WebClientRequestException e) {
            throw  new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error getting the network status.",
                    "Error getting the network status API from workflow manager. "+ e.getMessage());
        } catch (WebClientResponseException e) {
            String responseBody = e.getResponseBodyAsString();
            HttpStatusCode httpStatus = e.getStatusCode();
            throw  new CustomException(httpStatus, "Error getting the network status.",
                    "Error getting the network status API from workflow manager. " +responseBody);
        }
        return Optional.empty();
    }

    public Optional<JobStatus> getSchedulerStatus(Long schedulerId) {

        try {
            ResponseEntity<WorkflowResponseEntity<JobStatus>> response =  getWebClient()
                    .get()
                    .uri(configuration.getWorkflowBaseUrl() +
                            configuration.getWorkflowJobStatus()  + schedulerId)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<WorkflowResponseEntity<JobStatus>>(){})
                    .block();
            if(null != response && null != response.getBody()) {
                WorkflowResponseEntity<JobStatus> responseEntity = response.getBody();
                log.info("Status Code: " + response.getStatusCode());

                if(!response.getStatusCode().is2xxSuccessful()){
                    throw new CustomException(response.getStatusCode(), "Error aborting the Job.",
                            responseEntity.getErrorMessage());
                }
                return Optional.of(responseEntity.getData());
            }
        } catch (WebClientRequestException e) {
            throw  new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error getting the scheduler status.",
                    "Error getting the scheduler status from workflow manager" + e.getMessage());
        } catch (WebClientResponseException e) {
            String responseBody = e.getResponseBodyAsString();
            HttpStatusCode httpStatus = e.getStatusCode();
            throw  new CustomException(httpStatus, "Error getting the scheduler status.",
                    "Error getting the scheduler status from workflow manager" + responseBody);
        }
        return Optional.empty();
    }


    public Optional<JobTaskHistory> getTaskHistory(Long schedulerId) {

        try {
            ResponseEntity<WorkflowResponseEntity<JobTaskHistory>> response = getWebClient()
                    .get()
                    .uri(configuration.getWorkflowBaseUrl() +
                            configuration.getWorkflowTaskHistory().replace("$ID", schedulerId.toString()))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<WorkflowResponseEntity<JobTaskHistory>>(){})
                    .block();
            if(null != response && null != response.getBody()) {
                WorkflowResponseEntity<JobTaskHistory> responseEntity = response.getBody();
                log.info("Status Code: " + response.getStatusCode());

                if(!response.getStatusCode().is2xxSuccessful()){
                    throw new CustomException(response.getStatusCode(), "Error aborting the Job.",
                            responseEntity.getErrorMessage());
                }
                return Optional.of(responseEntity.getData());
            }
        } catch (WebClientRequestException e) {
            throw  new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error getting the Task History.",
                    "Error getting the Task History from workflow manager." + e.getMessage());
        } catch (WebClientResponseException e) {
            String responseBody = e.getResponseBodyAsString();
            HttpStatusCode httpStatus = e.getStatusCode();
            throw  new CustomException(httpStatus, "Error getting the Task History.",
                    "Error getting the Task History from workflow manager." + responseBody);
        }
        return Optional.empty();
    }

    public boolean pauseScheduler(Long schedulerId) {
        try {
            ResponseEntity<WorkflowResponseEntity<ActionResponse>> response = getWebClient()
                    .put()
                    .uri(configuration.getWorkflowBaseUrl() + configuration.getWorkflowPauseJob()
                            .replace("$ID", schedulerId.toString()))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<WorkflowResponseEntity<ActionResponse>>(){})
                    .block();
            if(null != response) {
                WorkflowResponseEntity<ActionResponse> responseEntity = response.getBody();
                log.debug("Pause scheduler response " + responseEntity);
                return response.getStatusCode().is2xxSuccessful();
            }
        } catch (WebClientRequestException e) {
            throw  new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error pausing the Job."
                    , "Error pausing the Job using workflow manager." + e.getMessage());
        } catch (WebClientResponseException e) {
            String responseBody = e.getResponseBodyAsString();
            HttpStatusCode httpStatus = e.getStatusCode();
            throw  new CustomException(httpStatus, "Error pausing the Job.",
                    "Error pausing the Job using workflow manager." + responseBody);
        }
        return false;
    }

    public boolean resumeScheduler(Long schedulerId) {
        try {
            ResponseEntity<WorkflowResponseEntity<ActionResponse>> response = getWebClient()
                    .put()
                    .uri(configuration.getWorkflowBaseUrl() + configuration.getWorkflowResumeJob()
                            .replace("$ID", schedulerId.toString()))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<WorkflowResponseEntity<ActionResponse>>(){})
                    .block();
            if(null != response) {
                WorkflowResponseEntity<ActionResponse> responseEntity = response.getBody();
                log.debug("Resume scheduler response " + responseEntity);
                return response.getStatusCode().is2xxSuccessful();
            }
        } catch (WebClientRequestException e) {
            throw  new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error resuming the Job."
                    , "Error resuming the Job using workflow manager."+ e.getMessage());
        } catch (WebClientResponseException e) {
            String responseBody = e.getResponseBodyAsString();
            HttpStatusCode httpStatus = e.getStatusCode();
            throw  new CustomException(httpStatus, "Error resuming the Job.",
                    "Error resuming the Job using workflow manager." + responseBody);
        }
        return false;
    }

    public boolean executeScheduler(Long schedulerId) {
        try {
            ResponseEntity<WorkflowResponseEntity<ActionResponse>> response = getWebClient()
                    .post()
                    .uri(configuration.getWorkflowBaseUrl() + configuration.getWorkflowExecuteJob()
                            .replace("$ID", schedulerId.toString()))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<WorkflowResponseEntity<ActionResponse>>(){})
                    .block();
            if(null != response) {
                WorkflowResponseEntity<ActionResponse> responseEntity = response.getBody();
                log.debug("Execute scheduler response " + responseEntity);
                return response.getStatusCode().is2xxSuccessful();
            }
        } catch (WebClientRequestException e) {
            throw  new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error executing the Job."
                    , "Error executing the Job using the workflow manager." + e.getMessage());
        } catch (WebClientResponseException e) {
            String responseBody = e.getResponseBodyAsString();
            HttpStatusCode httpStatus = e.getStatusCode();
            throw  new CustomException(httpStatus, "Error executing the Job.",
                    "Error executing the Job using the workflow manager." + responseBody);
        }
        return false;
    }

    public boolean abortScheduler(Long schedulerId) {
        try {
            ResponseEntity<WorkflowResponseEntity<ActionResponse>> response = getWebClient()
                    .post()
                    .uri(configuration.getWorkflowBaseUrl() + configuration.getWorkflowAbortJob()
                            .replace("$ID", schedulerId.toString()))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<WorkflowResponseEntity<ActionResponse>>(){})
                    .block();
            if(null != response) {
                WorkflowResponseEntity<ActionResponse> responseEntity = response.getBody();
                log.debug("Abort scheduler response " + responseEntity);
                return response.getStatusCode().is2xxSuccessful();
            }
        } catch (WebClientRequestException e) {
            throw  new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error aborting the Job.",
                    "Error aborting the Job using workflow manager." + e.getMessage());
        } catch (WebClientResponseException e) {
            String responseBody = e.getResponseBodyAsString();
            HttpStatusCode httpStatus = e.getStatusCode();
            throw  new CustomException(httpStatus, "Error aborting the Job.",
                    "Error aborting the Job using workflow manager." + responseBody);
        }
        return false;
    }
    public Optional<JobInfo> updateScheduler(JobInfo jobInfo) {

        try {
            ResponseEntity<WorkflowResponseEntity<JobInfo>> response =  getWebClient()
                    .put()
                    .uri(configuration.getWorkflowBaseUrl() +
                            configuration.getWorkflowJobUrl() + jobInfo.getId())
                    .accept(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(jobInfo))
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<WorkflowResponseEntity<JobInfo>>(){})
                    .block();
            if(null != response && null != response.getBody()) {
                WorkflowResponseEntity<JobInfo> responseEntity = response.getBody();
                log.info("Status Code: " + response.getStatusCode());

                if(!response.getStatusCode().is2xxSuccessful()){
                    throw new CustomException(response.getStatusCode(), "Error updating the Job.",
                            responseEntity.getErrorMessage());
                }
                return Optional.of(responseEntity.getData());
            }
        } catch (WebClientRequestException e) {
            throw  new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating the Job.",
                    "Error updating the Job using workflow manager." + e.getMessage());
        } catch (WebClientResponseException e) {
            String responseBody = e.getResponseBodyAsString();
            HttpStatusCode httpStatus = e.getStatusCode();
            throw  new CustomException(httpStatus, "Error updating the Job.",
                    "Error updating the Job using workflow manager." + responseBody);
        }
        return Optional.empty();
    }

    public boolean deleteScheduler(Long jobId) {

        try {
            ResponseEntity<WorkflowResponseEntity<ActionResponse>> response = getWebClient()
                    .delete()
                    .uri(configuration.getWorkflowBaseUrl() +
                            configuration.getWorkflowJobUrl() + jobId)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<WorkflowResponseEntity<ActionResponse>>() {
                    })
                    .block();
            if (null != response) {
                WorkflowResponseEntity<ActionResponse> responseEntity = response.getBody();
                log.debug("Pause scheduler response " + responseEntity);
                return response.getStatusCode().is2xxSuccessful();
            }
        } catch (WebClientRequestException e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting the Job."
                    , "Error deleting the Job using workflow manager." + e.getMessage());
        } catch (WebClientResponseException e) {
            String responseBody = e.getResponseBodyAsString();
            HttpStatusCode httpStatus = e.getStatusCode();
            throw new CustomException(httpStatus, "Error deleting the Job.",
                    "Error deleting the Job using workflow manager." + responseBody);
        }
        return false;
    }

    /*
    This method invokes the workflow manager API to delete the schedulers defined for the network.
     */
    public void deleteSchedulersOfNetwork(Long networkId) {
        log.info("Delete Jobs of network API :" +configuration.getWorkflowBaseUrl() +
                configuration.getWorkflowDeleteSchedulersOfNetwork() + networkId);
        try {
            ResponseEntity<WorkflowResponseEntity<String>> response = getWebClient()
                    .delete()
                    .uri(configuration.getWorkflowBaseUrl() +
                            configuration.getWorkflowDeleteSchedulersOfNetwork() + networkId)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<WorkflowResponseEntity<String>>() {
                    })
                    .block();
            if (null != response) {
                WorkflowResponseEntity<String> responseEntity = response.getBody();
                log.debug("Delete Jobs of network response " + responseEntity);
                boolean status = response.getStatusCode().is2xxSuccessful();
                if(!status) {
                    throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting the Jobs of network."
                            , "Error deleting the Jobs of network using workflow manager.");
                }
                log.info("Successfully deleted the jobs associated with the network " + networkId);
            }
        } catch (WebClientRequestException e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting the Jobs of network."
                    , "Error deleting the Jobs of network using workflow manager." + e.getMessage());
        } catch (WebClientResponseException e) {
            String responseBody = e.getResponseBodyAsString();
            HttpStatusCode httpStatus = e.getStatusCode();
            throw new CustomException(httpStatus, "Error deleting the Job.",
                    "Error deleting the Job using workflow manager." + responseBody);
        }
    }

    /*
    This method invokes the workflow manager API to delete the collector tasks from schedulers.
     */
    public void deleteCollectorTasks(Long networkId, Long collectorId) {
        String uri = configuration.getWorkflowBaseUrl() +
                configuration.getWorkflowDeleteCollectorTasks().replace("$NETWORK_ID", networkId+"") + collectorId;
        log.info("Delete collector tasks API :" + uri);
        try {
            ResponseEntity<WorkflowResponseEntity<List<Long>>> response = getWebClient()
                    .delete()
                    .uri(uri)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<WorkflowResponseEntity<List<Long>>>() {
                    })
                    .block();
            if (null != response) {
                WorkflowResponseEntity<List<Long>> responseEntity = response.getBody();
                log.debug("Delete collector tasks response " + responseEntity);
                boolean status = response.getStatusCode().is2xxSuccessful();
                if(status) {
                    log.error("Successfully deleted the  collector tasks .");
                } else {
                    log.error("Error deleting the  collector tasks with status code {} ", response.getStatusCode());
                }
            }
        } catch (WebClientRequestException e) {
            log.error("Error deleting the collector tasks using workflow manager.", e);
        } catch (WebClientResponseException e) {
            String responseBody = e.getResponseBodyAsString();
            HttpStatusCode httpStatus = e.getStatusCode();
            log.error("Error deleting the collector tasks using workflow manager with httpstatus {} and response body {}." ,
                    httpStatus, responseBody);
        }
    }

    /*
    API to Rsync the network.
     */
    public Optional<NetworkResyncInfo> rsyncNetwork(NetworkResyncInfo networkResyncInfo) {

        try {
            ResponseEntity<WorkflowResponseEntity<NetworkResyncInfo>> response =  getWebClient()
                    .post()
                    .uri(baseUrl + configuration.getWorkflowResyncJob())
                    .accept(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(networkResyncInfo))
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<WorkflowResponseEntity<NetworkResyncInfo>>(){})
                    .block();
            if(null != response && null != response.getBody()) {
                WorkflowResponseEntity<NetworkResyncInfo> responseEntity = response.getBody();
                if(!response.getStatusCode().is2xxSuccessful()){
                    log.error("Error creating the Rsync Job using workflow manager." + responseEntity.getErrorMessage());
                    throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error creating the Resync Job." + responseEntity.getErrorMessage());

                }
                return Optional.of(responseEntity.getData());
            }
        } catch (WebClientRequestException e) {
            log.error("Error creating the Rsync Job using workflow manager." ,e);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR,  "Error creating the Rsync Job.",
                    "Error creating the Rsync Job using workflow manager." + e.getMessage());
        } catch (WebClientResponseException e) {
            String responseBody = e.getResponseBodyAsString();
            HttpStatusCode httpStatus = e.getStatusCode();
            log.error("Error creating the Rsync Job using workflow manager." + responseBody);
            throw new CustomException(httpStatus,  "Error creating the Rsync Job.",
                    "Error creating the Rsync Job using workflow manager." + responseBody);
        }
        return Optional.empty();
    }

    /*
    API to get the Rsync jobs associated with all networks
     */
    public List<JobInfo> getRsyncJobs() {

        try {
            ResponseEntity<WorkflowResponseEntity> response =  getWebClient()
                    .get()
                    .uri(baseUrl + configuration.getWorkflowResyncJob())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toEntity(WorkflowResponseEntity.class)
                    .block();
            if(null != response && null != response.getBody()) {
                //Reference: Result is a list of LinkedHashMap https://www.baeldung.com/spring-webclient-json-list
                WorkflowResponseEntity<List<LinkedHashMap>> responseEntity = response.getBody();
                log.info("Status Code: " + response.getStatusCode());

                if(!response.getStatusCode().is2xxSuccessful()){
                    throw new CustomException(response.getStatusCode(), "Error getting the rsync job details.",
                            responseEntity.getErrorMessage());
                }
                List<JobInfo> jobInfoList = new ArrayList<>();
                ObjectMapper mapper = new ObjectMapper();
                for(LinkedHashMap map : responseEntity.getData()) {
                    JobInfo jobInfo = mapper.convertValue(map, JobInfo.class);
                    jobInfoList.add(jobInfo);
                }
                return jobInfoList;
            }
        } catch (WebClientRequestException e) {
            throw  new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error getting the Rsync Job details.",
                    "Error getting the Rsync Job details from workflow manager." + e.getMessage());
        } catch (WebClientResponseException e) {
            String responseBody = e.getResponseBodyAsString();
            HttpStatusCode httpStatus = e.getStatusCode();
            throw  new CustomException(httpStatus, "Error getting the Rsync Job details.",
                    "Error getting the Rsync Job details from workflow manager." + responseBody);
        }
        return List.of();
    }

    /*
    This method is used to get the job status required for dashboard.
     */
    public Optional<JobStats> getSchedulerStats() {

        try {
            ResponseEntity<WorkflowResponseEntity<JobStats>> response = getWebClient()
                    .get()
                    .uri(configuration.getWorkflowBaseUrl() + configuration.getWorkflowJobStats())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<WorkflowResponseEntity<JobStats>>(){})
                    .block();
            if(null != response && null != response.getBody()) {
                WorkflowResponseEntity<JobStats> responseEntity = response.getBody();
                log.info("Status Code: " + response.getStatusCode());

                if(!response.getStatusCode().is2xxSuccessful()){
                    throw new CustomException(response.getStatusCode(), "Error getting the scheduler statistics.",
                            responseEntity.getErrorMessage());
                }

                log.debug("Scheduler stats obtained " + responseEntity.getData());
                return Optional.of(responseEntity.getData());
            }
        } catch (WebClientRequestException e) {
            throw  new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error getting the scheduler statistics.",
                    "Error getting the scheduler statistics from workflow manager." + e.getMessage());
        } catch (WebClientResponseException e) {
            String responseBody = e.getResponseBodyAsString();
            HttpStatusCode httpStatus = e.getStatusCode();
            throw  new CustomException(httpStatus, "Error getting the scheduler statistics.",
                    "Error getting the scheduler statistics from workflow manager." + responseBody);
        }
        return Optional.empty();
    }


}
