package com.translator.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties(GeminiProperties.class)
public class GeminiClientConfig {

    @Bean
    public WebClient geminiWebClient(GeminiProperties geminiProperties) {
        // A fresh (non-pooled) connection per call: doOnConnected() only re-attaches
        // the read-timeout handler on a NEW connection, so a pooled/reused connection
        // would silently keep whichever handler state it had from a prior call - and
        // pooled connections can go stale between calls without the client noticing.
        HttpClient httpClient = HttpClient.create(ConnectionProvider.newConnection())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) geminiProperties.connectTimeout().toMillis())
                .doOnConnected(connection -> connection.addHandlerLast(
                        new ReadTimeoutHandler(geminiProperties.readTimeout().toMillis(), TimeUnit.MILLISECONDS)));

        return WebClient.builder()
                .baseUrl(geminiProperties.baseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
