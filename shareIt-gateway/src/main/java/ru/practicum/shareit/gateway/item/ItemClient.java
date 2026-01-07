package ru.practicum.shareit.gateway.item;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.gateway.client.BaseClient;

import java.util.Map;

@Service
public class ItemClient extends BaseClient {

    public ItemClient(@Value("${shareit-server.url}") String serverUrl,
                      RestTemplateBuilder builder) {
        super(serverUrl + "/items", builder);
    }

    public ResponseEntity<Object> create(long userId, Object body) {
        return post("", userId, body);
    }


    public ResponseEntity<Object> getById(long userId, long itemId) {
        return get("/{id}", userId, Map.of("id", itemId));
    }

    public ResponseEntity<Object> getOwnerItems(long userId, int from, int size) {
        return get("?from={from}&size={size}", userId, Map.of("from", from, "size", size));
    }

    public ResponseEntity<Object> search(long userId, String text) {
        return get("/search?text={text}", userId, Map.of("text", text));
    }

    public ResponseEntity<Object> update(long userId, long itemId, Object body) {
        return patch("/{id}", userId, body, Map.of("id", itemId));
    }

    public ResponseEntity<Object> createComment(long userId, long itemId, Object body) {
        return post("/{id}/comment", userId, body, Map.of("id", itemId));
    }

}
