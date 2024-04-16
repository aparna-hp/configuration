package com.cisco.configService.webClient;

import com.cisco.collectionService.model.srPce.status.XtcAgentStatusDto;
import com.cisco.configService.AppPropertiesReader;
import com.cisco.configService.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;


/**
 * The class uses WebClient to make API calls to sr pce service.
 */
@Component
@Slf4j
public class SrPceWebClient extends WebClientImpl{

    @Autowired
    private AppPropertiesReader configuration;

    public SrPceWebClient(@Value("${app.srpce.baseUrl}")String baseUrl) {
        super(baseUrl);
    }

    public ResponseEntity<XtcAgentStatusDto> getSrPceAgentStatus(Long agentId) {
        try {
            return getWebClient()
                    .get()
                    .uri(configuration.getSrpceAgentBaseUrl() +
                            configuration.getSrpceAgentStatus().replace("$ID", agentId.toString()))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toEntity(XtcAgentStatusDto.class)
                    .block();
        } catch (WebClientRequestException e) {
            throw new CustomException("Error getting the SR PCE agent status");
        } catch (WebClientResponseException e) {
            String responseBody = e.getResponseBodyAsString();
            HttpStatusCode httpStatus = e.getStatusCode();
            throw new CustomException(httpStatus, "Error getting the SR PCE agent status", responseBody);
        }
    }

    public ResponseEntity<XtcAgentStatusDto[]> getAllSrPceAgentStatus() {
        try {
            return getWebClient()
                    .get()
                    .uri(configuration.getSrpceAgentBaseUrl() +
                            configuration.getSrpceAllAgentStatus())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toEntity(XtcAgentStatusDto[].class)
                    .block();
        } catch (WebClientRequestException e) {
            throw new CustomException("Error getting the SR PCE agent status");
        } catch (WebClientResponseException e) {
            String responseBody = e.getResponseBodyAsString();
            HttpStatusCode httpStatus = e.getStatusCode();
            throw new CustomException(httpStatus, "Error getting the SR PCE agent status", responseBody);
        }
    }

    /*
    This method invokes srpce to stop bgpls collector.
     */
    public void stopBgpls(Long collectorId) {
        log.info("Stop BgpLs collector API :" + baseUrl +
                configuration.getSrpceStopBgpls().replace("$ID", collectorId.toString()));
        try {
            ResponseEntity<Object> response =  getWebClient()
                    .put()
                    .uri(baseUrl +
                            configuration.getSrpceStopBgpls().replace("$ID", collectorId.toString()))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toEntity(Object.class)
                    .block();
            if (response != null) {
                boolean status =  response.getStatusCode().is2xxSuccessful();
                if(status){
                    log.info("Successfully stopped the bgpls collector.");
                } else {
                    log.error("Error stopping the bgpls collector with status code {} ", response.getStatusCode());
                }
            }
        } catch (WebClientRequestException e) {
            log.error("Error stopping the bgpls collector " , e);
        } catch (WebClientResponseException e) {
            String responseBody = e.getResponseBodyAsString();
            HttpStatusCode httpStatus = e.getStatusCode();
            log.error("Error stopping the bgpls collector with https status {} and responseBody {} ", httpStatus, responseBody);
        }
    }

    /*
    This method invokes srpce to stop pcep collector.
     */
    public void stopPcepLsp(Long collectorId) {
        log.info("Stop Pcep LSP collector API :" + baseUrl +
                configuration.getSrpceStopPcep().replace("$ID", collectorId.toString()));
        try {
            ResponseEntity<Object> response =  getWebClient()
                    .put()
                    .uri(baseUrl +
                            configuration.getSrpceStopPcep().replace("$ID", collectorId.toString()))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toEntity(Object.class)
                    .block();

            if (response != null) {
                boolean status =  response.getStatusCode().is2xxSuccessful();
                if(status){
                    log.info("Successfully stopped the pcep lsp collector.");
                } else {
                    log.error("Error stopping the pcep lsp collector with status code {} ", response.getStatusCode());
                }
            }
        } catch (WebClientRequestException e) {
            log.error("Error stopping the pcep lsp collector " , e);
        } catch (WebClientResponseException e) {
            String responseBody = e.getResponseBodyAsString();
            HttpStatusCode httpStatus = e.getStatusCode();
            log.error("Error stopping the pcep lsp collector with https status {} and responseBody {} ", httpStatus, responseBody);
        }
    }
}
