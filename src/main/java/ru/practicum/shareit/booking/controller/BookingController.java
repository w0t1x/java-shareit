package ru.practicum.shareit.booking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private static final String USER_HEADER = "X-Sharer-User-Id";
    private final BookingService bookingService;

    @PostMapping
    public BookingDto create(@RequestHeader(USER_HEADER) long userId,
                             @Valid @RequestBody BookingInputDto dto) {
        return bookingService.addBooking(userId, dto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approve(@RequestHeader(USER_HEADER) long userId,
                              @PathVariable long bookingId,
                              @RequestParam boolean approved) {
        return bookingService.approveOrRejectBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto get(@RequestHeader(USER_HEADER) long userId,
                          @PathVariable long bookingId) {
        return bookingService.getBookingById(bookingId, userId);
    }

    @GetMapping
    public List<BookingDto> getMy(@RequestHeader(USER_HEADER) long userId,
                                  @RequestParam(name = "state", defaultValue = "ALL") State state) {
        return bookingService.getBookingsOfCurrentUser(state, userId);
    }

    @GetMapping("/owner")
    public List<BookingDto> getOwner(@RequestHeader(USER_HEADER) long userId,
                                     @RequestParam(name = "state", defaultValue = "ALL") State state) {
        return bookingService.getBookingsOfOwner(state, userId);
    }
}

