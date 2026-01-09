package ru.practicum.shareit.gateway.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import ru.practicum.shareit.gateway.client.BaseClient;
import ru.practicum.shareit.gateway.validation.BookingDatesValidator;

import java.util.Set;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {

    private static final Set<String> ALLOWED_STATES =
            Set.of("ALL", "CURRENT", "PAST", "FUTURE", "WAITING", "REJECTED");

    private final BookingClient client;

    @PostMapping
    public ResponseEntity<Object> create(
            @RequestHeader(BaseClient.USER_HEADER) @Positive long userId,
            @Valid @RequestBody BookingInputDto dto
    ) {
        BookingDatesValidator.validate(dto.getStart(), dto.getEnd());
        return client.create(userId, dto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approve(
            @RequestHeader(BaseClient.USER_HEADER) @Positive long userId,
            @PathVariable @Positive long bookingId,
            @RequestParam boolean approved
    ) {
        // С params проще: формируем URL строкой (approved — boolean)
        return client.approve(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getById(
            @RequestHeader(BaseClient.USER_HEADER) @Positive long userId,
            @PathVariable @Positive long bookingId
    ) {
        return client.getById(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getMy(
            @RequestHeader(BaseClient.USER_HEADER) @Positive long userId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size
    ) {
        validateState(state);
        return client.getMy(userId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getOwner(
            @RequestHeader(BaseClient.USER_HEADER) @Positive long userId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size
    ) {
        validateState(state);
        return client.getOwner(userId, state, from, size);
    }

    private void validateState(String state) {
        if (!ALLOWED_STATES.contains(state)) {
            throw new IllegalArgumentException("Unknown state: " + state);
        }
    }
}
