package ru.practicum.shareit.gateway.client;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.Timeout;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Bean
    public RestTemplateBuilder restTemplateBuilder() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(5))
                .setConnectionRequestTimeout(Timeout.ofSeconds(5))
                .setResponseTimeout(Timeout.ofSeconds(10))
                .build();

        HttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();

        HttpComponentsClientHttpRequestFactory rf =
                new HttpComponentsClientHttpRequestFactory(httpClient);

        // можно и билдером дополнительно (не обязательно)
        return new RestTemplateBuilder()
                .requestFactory(() -> rf)
                .setConnectTimeout(Duration.ofSeconds(5));
    }
}
