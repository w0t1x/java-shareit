package ru.practicum.shareit.gateway.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.gateway.client.BaseClient;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {

    private final ItemRequestClient client;

    @PostMapping
    public ResponseEntity<Object> create(
            @RequestHeader(BaseClient.USER_HEADER) @Positive long userId,
            @Valid @RequestBody ItemRequestCreateDto dto
    ) {
        return client.create(userId, dto);
    }

    @GetMapping
    public ResponseEntity<Object> getOwn(
            @RequestHeader(BaseClient.USER_HEADER) @Positive long userId
    ) {
        return client.getOwn(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAll(
            @RequestHeader(BaseClient.USER_HEADER) @Positive long userId,
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size
    ) {
        return client.getAll(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getById(
            @RequestHeader(BaseClient.USER_HEADER) @Positive long userId,
            @PathVariable @Positive long requestId
    ) {
        return client.getById(userId, requestId);
    }
}
