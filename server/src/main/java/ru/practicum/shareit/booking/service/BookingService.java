package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.model.State;

import java.util.List;

public interface BookingService {
    BookingDto addBooking(long bookerId, BookingInputDto bookingInputDto);

    BookingDto approveOrRejectBooking(long ownerId, long bookingId, boolean approved);

    BookingDto getBookingById(long bookingId, long userId);

    List<BookingDto> getBookingsOfCurrentUser(State state, long bookerId);

    List<BookingDto> getBookingsOfOwner(State state, long ownerId);
}
