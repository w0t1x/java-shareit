package ru.practicum.shareit.booking.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {

    private static final String USER_HEADER = "X-Sharer-User-Id";
    private final BookingService bookingService;

    @PostMapping
    public BookingDto addBooking(@RequestHeader(USER_HEADER) @Positive long userId,
                                 @Valid @RequestBody BookingInputDto bookingInputDto) {
        return bookingService.addBooking(userId, bookingInputDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approve(@RequestHeader(USER_HEADER) @Positive long userId,
                              @PathVariable @Positive long bookingId,
                              @RequestParam boolean approved) {
        return bookingService.approveOrRejectBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBookingById(@RequestHeader(USER_HEADER) @Positive long userId,
                                     @PathVariable @Positive long bookingId) {
        return bookingService.getBookingById(bookingId, userId);
    }

    @GetMapping
    public List<BookingDto> getBookingsOfCurrentUser(@RequestHeader(USER_HEADER) @Positive long userId,
                                                     @RequestParam(defaultValue = "ALL") String state,
                                                     @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                     @RequestParam(defaultValue = "10") @Positive int size) {
        List<BookingDto> all = bookingService.getBookingsOfCurrentUser(parseState(state), userId);
        return paginate(all, from, size);
    }

    @GetMapping("/owner")
    public List<BookingDto> getBookingsOfOwnerItems(@RequestHeader(USER_HEADER) @Positive long userId,
                                                    @RequestParam(defaultValue = "ALL") String state,
                                                    @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                    @RequestParam(defaultValue = "10") @Positive int size) {
        List<BookingDto> all = bookingService.getBookingsOfOwner(parseState(state), userId);
        return paginate(all, from, size);
    }

    private State parseState(String state) {
        try {
            return State.valueOf(state);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown state: " + state);
        }
    }

    private <T> List<T> paginate(List<T> list, int from, int size) {
        if (from >= list.size()) return List.of();
        int to = Math.min(from + size, list.size());
        return list.subList(from, to);
    }
}
