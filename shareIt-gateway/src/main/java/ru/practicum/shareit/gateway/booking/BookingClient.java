package ru.practicum.shareit.gateway.booking;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.gateway.client.BaseClient;

import java.util.Map;

@Service
public class BookingClient extends BaseClient {

    public BookingClient(@Value("${shareit-server.url}") String serverUrl,
                         RestTemplateBuilder builder) {
        super(serverUrl + "/bookings", builder);
    }

    public ResponseEntity<Object> create(long userId, Object body) {
        return post("", userId, body);
    }

    public ResponseEntity<Object> approve(long userId, long bookingId, boolean approved) {
        Map<String, Object> params = Map.of("id", bookingId, "approved", approved);
        return patch("/{id}?approved={approved}", userId, (Object) null, params);
    }

    public ResponseEntity<Object> getById(long userId, long bookingId) {
        return get("/{id}", userId, Map.of("id", bookingId));
    }

    public ResponseEntity<Object> getMy(long userId, String state, int from, int size) {
        Map<String, Object> p = Map.of("state", state, "from", from, "size", size);
        return get("?state={state}&from={from}&size={size}", userId, p);
    }

    public ResponseEntity<Object> getOwner(long userId, String state, int from, int size) {
        Map<String, Object> p = Map.of("state", state, "from", from, "size", size);
        return get("/owner?state={state}&from={from}&size={size}", userId, p);
    }
}
