package com.kuriosys.messagingprocessor.config;

import io.netty.channel.ChannelOption;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        ConnectionProvider provider = ConnectionProvider.builder("WebClientPool")
                .maxConnections(50) // limit total connections
                .maxIdleTime(Duration.ofSeconds(30))
                .maxLifeTime(Duration.ofMinutes(5))
                .pendingAcquireTimeout(Duration.ofSeconds(10))
                .pendingAcquireMaxCount(1000) // max waiting requests
                .metrics(true)
                .build();

        HttpClient httpClient = HttpClient.create(provider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}

