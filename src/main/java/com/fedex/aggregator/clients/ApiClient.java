package com.fedex.aggregator.clients;

import com.fedex.aggregator.clients.exceptions.ServiceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.UriSpec;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiClient {
    private final WebClient client;

    public static final String DEFAULT_HOST = "http://127.0.0.1";
    public static final String DEFAULT_PORT = "8080";

    public static final int RETRY_MAX_ATTEMPTS = 2;
    public static final int RETRY_IN_MILLISECONDS = 500;

    public ApiClient() {
        this(DEFAULT_HOST, DEFAULT_PORT, WebClient.create(DEFAULT_HOST + ":" + DEFAULT_PORT));
    }

    public ApiClient(String host, String port, WebClient client) {
        this.client = client;
    }

    public <T> Mono<T> get(String uri, List<String> queryParams, Class<T> responseType) {
        UriSpec<RequestBodySpec> uriSpec = client.method(HttpMethod.GET);
        RequestBodySpec bodySpec = uriSpec.uri(uriBuilder -> uriBuilder.path(uri).queryParam("q", queryParams).build())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML)
                .acceptCharset(StandardCharsets.UTF_8)
                .ifNoneMatch("*");
        Mono<T> monoResponse = bodySpec.exchangeToMono(response -> {
            if (response.statusCode()
                    .equals(HttpStatus.OK)) {
                return response.bodyToMono(responseType);
            } else if (response.statusCode()
                    .is5xxServerError()) {
                return Mono.error(new ServiceException("Service error, retry.", response.rawStatusCode()));
            } else {
                return response.createException()
                        .flatMap(Mono::error);
            }
        });
        monoResponse.retryWhen(Retry.backoff(RETRY_MAX_ATTEMPTS, Duration.ofMillis(RETRY_IN_MILLISECONDS))
                .filter(throwable -> throwable instanceof ServiceException));

        return monoResponse;
    }
}
