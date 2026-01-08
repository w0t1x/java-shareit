package ru.practicum.shareit.gateway.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDTO;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserClient client;

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable @Positive long id) {
        return client.getById(id);
    }

    @GetMapping
    public ResponseEntity<Object> getAll() {
        return client.getAll();
    }

    @PostMapping
    public ResponseEntity<Object> create(@Valid @RequestBody UserDTO dto) {
        validateUserCreate(dto);
        return client.create(dto);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> patch(@PathVariable @Positive long userId,
                                        @Valid @RequestBody UserDTO dto) {
        validateUserPatch(dto);
        return client.patch("/" + userId, 0L, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable @Positive long id) {
        return client.delete("/" + id, 0L);
    }

    private void validateUserCreate(UserDTO dto) {
        if (dto == null) throw new IllegalArgumentException("body is null");
        if (dto.getName() == null || dto.getName().isBlank()) throw new IllegalArgumentException("name must not be blank");
        if (dto.getEmail() == null || dto.getEmail().isBlank()) throw new IllegalArgumentException("email must not be blank");
    }

    private void validateUserPatch(UserDTO dto) {
        if (dto == null) return;
        if (dto.getName() != null && dto.getName().isBlank()) throw new IllegalArgumentException("name must not be blank");
        if (dto.getEmail() != null && dto.getEmail().isBlank()) throw new IllegalArgumentException("email must not be blank");
    }
}
