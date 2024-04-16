package com.cisco.configService.webClient;

import com.cisco.configService.AppPropertiesReader;
import com.cisco.configService.exception.CustomException;
import com.cisco.configService.model.netflow.status.NetflowClusterStatus;
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
public class NetflowWebClient extends WebClientImpl{

    @Autowired
    private AppPropertiesReader configuration;

    public NetflowWebClient(@Value("${app.netflow.baseUrl}") String baseUrl) {
        super(baseUrl);
    }

    public ResponseEntity<NetflowClusterStatus> getNetflowStatus() {
        try {
            return getWebClient()
                    .get()
                    .uri(configuration.getNetflowBaseUrl() +
                            configuration.getNetflowStatus())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toEntity(NetflowClusterStatus.class)
                    .block();
        } catch (WebClientRequestException e) {
            throw new CustomException("Error getting the Netflow cluster status");
        } catch (WebClientResponseException e) {
            String responseBody = e.getResponseBodyAsString();
            HttpStatusCode httpStatus = e.getStatusCode();
            throw new CustomException(httpStatus, "Error getting the Netflow cluster status", responseBody);
        }
    }
}
