package com.cisco.configService.webClient;

import com.cisco.aggregator.*;
import com.cisco.configService.AppPropertiesReader;
import com.cisco.configService.exception.CustomException;
import com.cisco.workflowmanager.WorkflowResponseEntity;
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

import java.util.Optional;


/**
 * The class uses WebClient to make API calls to sr pce service.
 */
@Component
@Slf4j
public class AggregatorWebClient extends WebClientImpl{

    @Autowired
    private AppPropertiesReader configuration;

    public AggregatorWebClient(@Value("${app.aggregator.baseUrl}")String baseUrl) {
        super(baseUrl);
    }

    /*
    This method invokes the Aggregator API to validate the capabilities of the
    custom collector.
     */
    public void validateCapabilityOfCustomCollector(ExtScriptCapabilityInfo extScriptCapabilityInfo) {
        try {
            log.info("Validate Aggregator of the custom collector API : " + configuration.getAggregatorBaseUrl()
                    + configuration.getAggregatorScriptPropertiesValidator());
            ResponseEntity<AggrResponseData<ExtScriptCapabilityInfo>> response =  getWebClient()
                    .post()
                    .uri(baseUrl
                            + configuration.getAggregatorScriptPropertiesValidator())
                    .accept(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(extScriptCapabilityInfo))
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<AggrResponseData<ExtScriptCapabilityInfo>>(){})
                    .block();
            if(null != response && null != response.getBody()) {
                AggrResponseData<ExtScriptCapabilityInfo> responseEntity = response.getBody();
                if(!response.getStatusCode().is2xxSuccessful()){
                    throw new CustomException(response.getStatusCode(), "Error validating the aggregator capability of the custom collector.",
                            responseEntity.getError());
                }
            }
        } catch (WebClientRequestException e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error validating the aggregator capability of the custom collector.",
                    "Error validating the aggregator capability." +e.getMessage());
        } catch (WebClientResponseException e) {
            String responseBody = e.getResponseBodyAsString();
            HttpStatusCode httpStatus = e.getStatusCode();
            log.error("Error received for validation of aggregator properties of the custom collector with httpStatus " + httpStatus, e);
            throw  new CustomException(httpStatus, "Error validating the aggregator capability of the custom collector.",
                    "Error validating the aggregator capability of the custom collector." + responseBody);
        }
    }

    /*
    This method invokes the Aggregator API to update the capabilities of the
    custom collector.
     */
    public void updateCapability(ExtScriptCapabilityInfo extScriptCapabilityInfo) {
        try {
            log.info("Update Aggregator capabilities of the custom collector API : " + configuration.getAggregatorBaseUrl()
                    + configuration.getAggregatorScriptProperties());
            ResponseEntity<AggrResponseData<ExtScriptCapabilityInfo>> response =  getWebClient()
                    .post()
                    .uri(baseUrl
                            + configuration.getAggregatorScriptProperties())
                    .accept(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(extScriptCapabilityInfo))
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<AggrResponseData<ExtScriptCapabilityInfo>>(){})
                    .block();
            if(null != response && null != response.getBody()) {
                AggrResponseData<ExtScriptCapabilityInfo> responseEntity = response.getBody();
                if(!response.getStatusCode().is2xxSuccessful()){
                    throw new CustomException(response.getStatusCode(), "Error updating the aggregator capability of the custom collector.",
                            responseEntity.getError());
                }
            }
        } catch (WebClientRequestException e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating the aggregator capability of the custom collector.",
                    "Error updating the aggregator capability of the custom collector." +e.getMessage());
        } catch (WebClientResponseException e) {
            String responseBody = e.getResponseBodyAsString();
            HttpStatusCode httpStatus = e.getStatusCode();
            log.error("Error updating aggregator properties of the custom collector with httpStatus " + httpStatus, e);
            throw  new CustomException(httpStatus, "Error updating aggregator capability of the custom collector.",
                    "Error updating the aggregator capability of the custom collector." + responseBody);
        }
    }

    /*
   This method invokes the Aggregator API to update the aggregator capabilities.
    */
    public boolean updateConfig(UserFileInfo userFileInfo) {
        try {
            log.info("Updating global Aggregator capabilities API :" + configuration.getAggregatorBaseUrl()
                    + configuration.getAggregatorConfigUpdate());
            ResponseEntity<AggrResponseData> response =  getWebClient()
                    .post()
                    .uri(baseUrl
                            + configuration.getAggregatorConfigUpdate())
                    .accept(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(userFileInfo))
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<AggrResponseData>(){})
                    .block();
            if(null != response && null != response.getBody()) {
                AggrResponseData responseEntity = response.getBody();
                if(!response.getStatusCode().is2xxSuccessful()){
                    throw new CustomException(response.getStatusCode(), "Error updating the global aggregator capability.",
                            responseEntity.getError());
                }
            }
        } catch (WebClientRequestException e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating the global aggregator capability.",
                    "Error updating the global aggregator capability." +e.getMessage());
        } catch (WebClientResponseException e) {
            String responseBody = e.getResponseBodyAsString();
            HttpStatusCode httpStatus = e.getStatusCode();
            log.error("Error updating the global aggregator properties with httpStatus " + httpStatus, e);
            throw  new CustomException(httpStatus, "Error updating the global aggregator capability.",
                    "Error updating the global aggregator capability." + responseBody);
        }
        return true;
    }

    /*
    This method invokes Aggregator service to reset the aggregator config.
     */
    public boolean resetConfig() {
        try {
            log.info("Resetting global Aggregator capabilities API : " + configuration.getAggregatorBaseUrl()
                    + configuration.getAggregatorConfigReset());
            ResponseEntity<AggrResponseData> response =  getWebClient()
                    .put()
                    .uri(baseUrl
                            + configuration.getAggregatorConfigReset())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<AggrResponseData>(){})
                    .block();
            if(null != response && null != response.getBody()) {
                AggrResponseData<ExtScriptCapabilityInfo> responseEntity = response.getBody();
                if(!response.getStatusCode().is2xxSuccessful()){
                    throw new CustomException(response.getStatusCode(), "Error resetting the global aggregator capability.",
                            responseEntity.getError());
                }
            }
        } catch (WebClientRequestException e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error resetting the global aggregator capability.",
                    "Error resetting the global aggregator capability." +e.getMessage());
        } catch (WebClientResponseException e) {
            String responseBody = e.getResponseBodyAsString();
            HttpStatusCode httpStatus = e.getStatusCode();
            log.error("Error resetting the global aggregator properties with httpStatus " + httpStatus, e);
            throw  new CustomException(httpStatus, "Error setting aggregator capability.",
                    "Error resetting the global aggregator capability." + responseBody);
        }
        return true;
    }

    public Optional<String> getAggrConfig() {
        log.info("Get global Aggregator capabilities API : " + configuration.getAggregatorBaseUrl()
                + configuration.getAggregatorConfigGet());
        try {
            ResponseEntity<WorkflowResponseEntity<String>> response = getWebClient()
                    .get()
                    .uri(baseUrl + configuration.getAggregatorConfigGet())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<WorkflowResponseEntity<String>>(){})
                    .block();
            if(null != response && null != response.getBody()) {
                WorkflowResponseEntity<String> responseEntity = response.getBody();
                log.info("Status Code: " + response.getStatusCode());

                if(!response.getStatusCode().is2xxSuccessful()){
                    throw new CustomException(response.getStatusCode(), "Error getting the global aggregator properties.",
                            responseEntity.getErrorMessage());
                }

                log.debug("The global aggregator properties " + responseEntity.getData());
                return Optional.of(responseEntity.getData());
            }
        } catch (WebClientRequestException e) {
            throw  new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error getting the global aggregator properties.",
                    "Error getting the global aggregator properties from aggregator service." + e.getMessage());
        } catch (WebClientResponseException e) {
            String responseBody = e.getResponseBodyAsString();
            HttpStatusCode httpStatus = e.getStatusCode();
            throw  new CustomException(httpStatus, "Error getting the global aggregator properties.",
                    "Error getting the global aggregator properties from aggregator service." + responseBody);
        }
        return Optional.empty();
    }

    /*
    This method returns the aging configuration.
     */
    public Optional<AgeingFlagsConfig> getAgingConfig() {
        log.info("Get Aging configuration API : " + baseUrl + configuration.getAggregatorAging());
        try {
            ResponseEntity<AggrResponseEntity<AgeingFlagsConfig>> response = getWebClient()
                    .get()
                    .uri(baseUrl + configuration.getAggregatorAging())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<AggrResponseEntity<AgeingFlagsConfig>>(){})
                    .block();
            if(null != response && null != response.getBody()) {
                AggrResponseEntity<AgeingFlagsConfig> responseEntity = response.getBody();
                log.info("Status Code: " + response.getStatusCode());

                if(!response.getStatusCode().is2xxSuccessful()){
                    throw new CustomException(response.getStatusCode(), "Error getting the purge configuration.",
                            responseEntity.getErrorMessage());
                }

                log.debug("The Aging config received : " + responseEntity.getData());
                if(null != responseEntity.getData()){
                    return Optional.of(responseEntity.getData());
                }
            }
        } catch (WebClientRequestException e) {
            throw  new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error getting the purge configuration.",
                    "Error getting the purge configuration from aggregator service." + e.getMessage());
        } catch (WebClientResponseException e) {
            String responseBody = e.getResponseBodyAsString();
            HttpStatusCode httpStatus = e.getStatusCode();
            throw  new CustomException(httpStatus, "Error getting the purge configuration.",
                    "Error getting the purge configuration from aggregator service." + responseBody);
        }
        return Optional.empty();
    }

    /*
  This method invokes the Aggregator API to update the aging configuration.
   */
    public boolean updateAgingConfig(AgeingFlagsConfig ageingFlagsConfig) {
        try {
            log.info("Update Aging API :" + baseUrl + configuration.getAggregatorAging());
            ResponseEntity<AggrResponseEntity<AgeingFlagsConfig>> response =  getWebClient()
                    .post()
                    .uri(baseUrl + configuration.getAggregatorAging())
                    .accept(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(ageingFlagsConfig))
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<AggrResponseEntity<AgeingFlagsConfig>>(){})
                    .block();
            if(null != response && null != response.getBody()) {
                AggrResponseEntity<AgeingFlagsConfig> responseEntity = response.getBody();
                if(!response.getStatusCode().is2xxSuccessful()){
                    throw new CustomException(response.getStatusCode(), "Error updating the purge configuration.");
                }
            }
        } catch (WebClientRequestException e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating the purge configuration.",
                    "Error updating the purge configuration." +e.getMessage());
        } catch (WebClientResponseException e) {
            String responseBody = e.getResponseBodyAsString();
            HttpStatusCode httpStatus = e.getStatusCode();
            log.error("Error updating the purge configuration with httpStatus " + httpStatus, e);
            throw  new CustomException(httpStatus, "Error updating the purge configuration.",
                    "Error updating the purge configuration." + responseBody);
        }
        return true;
    }

}
