package ru.practicum.shareit.gateway.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.gateway.client.BaseClient;

import java.util.Map;

@Service
public class UserClient extends BaseClient {

    public UserClient(@Value("${shareit-server.url}") String serverUrl,
                      RestTemplateBuilder builder) {
        super(serverUrl + "/users", builder);
    }

    public ResponseEntity<Object> getById(long userId) {
        return get("/{id}", 0L, Map.of("id", userId)); // заголовок не нужен для users
    }

    public ResponseEntity<Object> getAll() {
        return get("", 0L);
    }

    public ResponseEntity<Object> create(Object body) {
        return post("", 0L, body);
    }
}
