package com.cisco.configService.webClient;

import com.cisco.configService.AppPropertiesReader;
import com.cisco.configService.exception.CustomException;
import com.cisco.configService.model.trafficPoller.CPStatusResponse;
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
public class TrafficPollerWebClient extends WebClientImpl{

    @Autowired
    private AppPropertiesReader configuration;

    public TrafficPollerWebClient(@Value("${app.traffic.poller.baseUrl}") String baseUrl) {
        super(baseUrl);
    }

    public ResponseEntity<CPStatusResponse> getTrafficPollerStatus(long id) {
        try {
            return getWebClient()
                    .get()
                    .uri(configuration.getTrafficPollerBaseUrl() +
                            configuration.getTrafficPollerStatus()+id)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toEntity(CPStatusResponse.class)
                    .block();
        } catch (WebClientRequestException e) {
            throw new CustomException("Error getting the traffic poller status");
        } catch (WebClientResponseException e) {
            String responseBody = e.getResponseBodyAsString();
            HttpStatusCode httpStatus = e.getStatusCode();
            throw new CustomException(httpStatus, "Error getting the traffic poller status", responseBody);
        }
    }
}
