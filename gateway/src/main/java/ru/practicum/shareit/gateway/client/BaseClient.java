package ru.practicum.shareit.gateway.client;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.Map;
import java.util.Set;

public class BaseClient {
    public static final String USER_HEADER = "X-Sharer-User-Id";

    // hop-by-hop headers — их НЕЛЬЗЯ прокидывать через прокси
    private static final Set<String> HOP_HEADERS = Set.of(
            "transfer-encoding",
            "content-length",
            "connection",
            "keep-alive",
            "proxy-authenticate",
            "proxy-authorization",
            "te",
            "trailer",
            "upgrade"
    );

    private final RestTemplate rest;

    public BaseClient(String serverUrl, RestTemplateBuilder builder) {
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(serverUrl);

        this.rest = builder
                .uriTemplateHandler(factory)
                .errorHandler(new DefaultResponseErrorHandler() {
                    @Override
                    public boolean hasError(ClientHttpResponse response) {
                        return false;
                    }
                })
                .build();
    }

    // ---------- GET ----------
    protected ResponseEntity<Object> get(String path, long userId) {
        return makeAndSendRequest(HttpMethod.GET, path, userId, null, null);
    }

    protected ResponseEntity<Object> get(String path, long userId, Map<String, Object> params) {
        return makeAndSendRequest(HttpMethod.GET, path, userId, params, null);
    }

    // ---------- POST ----------
    protected <T> ResponseEntity<Object> post(String path, long userId, T body) {
        return makeAndSendRequest(HttpMethod.POST, path, userId, null, body);
    }

    protected <T> ResponseEntity<Object> post(String path, long userId, T body, Map<String, Object> params) {
        return makeAndSendRequest(HttpMethod.POST, path, userId, params, body);
    }

    protected <T> ResponseEntity<Object> post(String path, long userId, Map<String, Object> params, T body) {
        return makeAndSendRequest(HttpMethod.POST, path, userId, params, body);
    }

    // ---------- PATCH ----------
    public <T> ResponseEntity<Object> patch(String path, long userId, T body) {
        return makeAndSendRequest(HttpMethod.PATCH, path, userId, null, body);
    }

    protected <T> ResponseEntity<Object> patch(String path, long userId, T body, Map<String, Object> params) {
        return makeAndSendRequest(HttpMethod.PATCH, path, userId, params, body);
    }

    protected <T> ResponseEntity<Object> patch(String path, long userId, Map<String, Object> params, T body) {
        return makeAndSendRequest(HttpMethod.PATCH, path, userId, params, body);
    }

    // ---------- DELETE ----------
    public ResponseEntity<Object> delete(String path, long userId) {
        return makeAndSendRequest(HttpMethod.DELETE, path, userId, null, null);
    }

    protected ResponseEntity<Object> delete(String path, long userId, Map<String, Object> params) {
        return makeAndSendRequest(HttpMethod.DELETE, path, userId, params, null);
    }

    // ---------- core ----------
    private <T> ResponseEntity<Object> makeAndSendRequest(HttpMethod method,
                                                          String path,
                                                          long userId,
                                                          Map<String, Object> params,
                                                          T body) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(USER_HEADER, String.valueOf(userId)); // включая 0

        HttpEntity<T> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Object> resp;
        if (params != null && !params.isEmpty()) {
            resp = rest.exchange(path, method, requestEntity, Object.class, params);
        } else {
            resp = rest.exchange(path, method, requestEntity, Object.class);
        }

        return sanitizeResponse(resp);
    }

    private ResponseEntity<Object> sanitizeResponse(ResponseEntity<Object> resp) {
        HttpHeaders safe = new HttpHeaders();

        resp.getHeaders().forEach((name, values) -> {
            if (!HOP_HEADERS.contains(name.toLowerCase())) {
                safe.put(name, values);
            }
        });

        // если backend не прислал Content-Type, можно проставить JSON
        if (!safe.containsKey(HttpHeaders.CONTENT_TYPE)) {
            safe.setContentType(MediaType.APPLICATION_JSON);
        }

        return new ResponseEntity<>(resp.getBody(), safe, resp.getStatusCode());
    }
}
