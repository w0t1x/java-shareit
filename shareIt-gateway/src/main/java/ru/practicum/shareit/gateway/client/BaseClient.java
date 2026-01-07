package ru.practicum.shareit.gateway.client;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;

public class BaseClient {
    public static final String USER_HEADER = "X-Sharer-User-Id";

    private static final Set<String> HOP_BY_HOP_HEADERS = Set.of(
            "connection",
            "keep-alive",
            "proxy-authenticate",
            "proxy-authorization",
            "te",
            "trailer",
            "transfer-encoding",
            "upgrade",
            // не hop-by-hop, но его лучше не пробрасывать от server наружу
            "content-length"
    );

    private final RestTemplate rest;
    private final String baseUrl;

    protected BaseClient(String baseUrl, RestTemplateBuilder builder) {
        this.baseUrl = baseUrl;
        this.rest = builder
                .errorHandler(new NoOpErrorHandler())
                .build();
    }

    protected ResponseEntity<Object> get(String path, long userId) {
        return get(path, userId, Map.of());
    }

    protected ResponseEntity<Object> get(String path, long userId, Map<String, Object> params) {
        ResponseEntity<Object> resp = rest.exchange(
                url(path),
                HttpMethod.GET,
                new HttpEntity<>(headers(userId)),
                Object.class,
                params
        );
        return sanitize(resp);
    }

    public <T> ResponseEntity<Object> post(String path, long userId, T body) {
        HttpHeaders h = headers(userId);
        h.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Object> resp = rest.exchange(
                url(path),
                HttpMethod.POST,
                new HttpEntity<>(body, h),
                Object.class
        );
        return sanitize(resp);
    }

    public <T> ResponseEntity<Object> post(String path, long userId, T body, Map<String, Object> params) {
        HttpHeaders h = headers(userId);
        h.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Object> resp = rest.exchange(
                url(path),
                HttpMethod.POST,
                new HttpEntity<>(body, h),
                Object.class,
                params
        );
        return sanitize(resp);
    }

    public <T> ResponseEntity<Object> patch(String path, long userId, T body) {
        HttpHeaders h = headers(userId);
        h.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Object> resp = rest.exchange(
                url(path),
                HttpMethod.PATCH,
                new HttpEntity<>(body, h),
                Object.class
        );
        return sanitize(resp);
    }

    public <T> ResponseEntity<Object> patch(String path, long userId, T body, Map<String, Object> params) {
        HttpHeaders h = headers(userId);
        h.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Object> resp = rest.exchange(
                url(path),
                HttpMethod.PATCH,
                new HttpEntity<>(body, h),
                Object.class,
                params
        );
        return sanitize(resp);
    }

    public ResponseEntity<Object> delete(String path, long userId) {
        ResponseEntity<Object> resp = rest.exchange(
                url(path),
                HttpMethod.DELETE,
                new HttpEntity<>(headers(userId)),
                Object.class
        );
        return sanitize(resp);
    }

    public ResponseEntity<Object> delete(String path, long userId, Map<String, Object> params) {
        ResponseEntity<Object> resp = rest.exchange(
                url(path),
                HttpMethod.DELETE,
                new HttpEntity<>(headers(userId)),
                Object.class,
                params
        );
        return sanitize(resp);
    }

    private HttpHeaders headers(long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        if (userId > 0) {
            headers.set(USER_HEADER, String.valueOf(userId));
        }
        return headers;
    }

    private String url(String path) {
        if (path == null || path.isBlank()) {
            return baseUrl;
        }
        if (path.startsWith("?")) {
            return baseUrl + path;
        }
        if (baseUrl.endsWith("/") && path.startsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1) + path;
        }
        if (!baseUrl.endsWith("/") && !path.startsWith("/")) {
            return baseUrl + "/" + path;
        }
        return baseUrl + path;
    }

    private ResponseEntity<Object> sanitize(ResponseEntity<Object> resp) {
        HttpHeaders cleaned = new HttpHeaders();
        resp.getHeaders().forEach((k, v) -> {
            if (!HOP_BY_HOP_HEADERS.contains(k.toLowerCase(Locale.ROOT))) {
                cleaned.put(k, v);
            }
        });
        return new ResponseEntity<>(resp.getBody(), cleaned, resp.getStatusCode());
    }

    private static class NoOpErrorHandler extends DefaultResponseErrorHandler {
        @Override
        public boolean hasError(org.springframework.http.client.ClientHttpResponse response) throws IOException {
            return false;
        }
    }
}
