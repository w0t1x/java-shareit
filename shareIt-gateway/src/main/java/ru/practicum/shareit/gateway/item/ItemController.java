package ru.practicum.shareit.gateway.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.gateway.client.BaseClient;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {

    private final ItemClient client;

    @PostMapping
    public ResponseEntity<Object> create(
            @RequestHeader(BaseClient.USER_HEADER) @Positive long userId,
            @RequestBody ItemDto dto
    ) {
        validateItemCreate(dto);
        return client.create(userId, dto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(
            @RequestHeader(BaseClient.USER_HEADER) @Positive long userId,
            @PathVariable @Positive long itemId,
            @RequestBody ItemDto dto
    ) {
        validateItemPatch(dto);
        return client.update(userId, itemId, dto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getById(
            @RequestHeader(BaseClient.USER_HEADER) @Positive long userId,
            @PathVariable @Positive long itemId
    ) {
        return client.getById(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getOwnerItems(
            @RequestHeader(BaseClient.USER_HEADER) @Positive long userId,
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size
    ) {
        return client.getOwnerItems(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(
            @RequestHeader(BaseClient.USER_HEADER) @Positive long userId,
            @RequestParam String text
    ) {
        // если text пустой — server обычно возвращает []
        return client.search(userId, text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(
            @RequestHeader(BaseClient.USER_HEADER) @Positive long userId,
            @PathVariable @Positive long itemId,
            @Valid @RequestBody CommentCreateDto dto
    ) {
        return client.createComment(userId, itemId, dto);
    }

    private void validateItemCreate(ItemDto dto) {
        if (dto == null) throw new IllegalArgumentException("body is null");
        if (dto.getName() == null || dto.getName().isBlank())
            throw new IllegalArgumentException("name must not be blank");
        if (dto.getDescription() == null || dto.getDescription().isBlank())
            throw new IllegalArgumentException("description must not be blank");
        if (dto.getAvailable() == null) throw new IllegalArgumentException("available must not be null");
        // requestId можно не валидировать по БД в gateway — это проверит server
    }

    private void validateItemPatch(ItemDto dto) {
        if (dto == null) return;
        if (dto.getName() != null && dto.getName().isBlank())
            throw new IllegalArgumentException("name must not be blank");
        if (dto.getDescription() != null && dto.getDescription().isBlank())
            throw new IllegalArgumentException("description must not be blank");
    }
}
