package com.cisco.configService.webClient;

import io.netty.handler.logging.LogLevel;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

public class WebClientImpl {

    String baseUrl;

    public WebClientImpl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    protected WebClient getWebClient() {
        HttpClient httpClient = HttpClient
                .create()
                .wiretap("reactor.netty.http.client.HttpClient",
                        LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL);

        WebClient.Builder builder = WebClient
                .builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient));

        return builder
                .baseUrl(baseUrl)
                .build();

    }

}
