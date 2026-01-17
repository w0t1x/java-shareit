package ru.practicum.shareit.gateway.request;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.gateway.client.BaseClient;

import java.util.Map;

@Service
public class ItemRequestClient extends BaseClient {

    public ItemRequestClient(@Value("${shareit-server.url}") String serverUrl,
                             RestTemplateBuilder builder) {
        super(serverUrl + "/requests", builder);
    }

    public ResponseEntity<Object> create(long userId, Object body) {
        return post("", userId, body);
    }

    public ResponseEntity<Object> getOwn(long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> getAll(long userId, int from, int size) {
        return get("/all?from={from}&size={size}", userId, Map.of("from", from, "size", size));
    }

    public ResponseEntity<Object> getById(long userId, long requestId) {
        return get("/{id}", userId, Map.of("id", requestId));
    }
}
