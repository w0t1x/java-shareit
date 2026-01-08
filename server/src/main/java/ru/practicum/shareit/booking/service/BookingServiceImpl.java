package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapper bookingMapper;

    @Transactional
    @Override
    public BookingDto addBooking(long bookerId, BookingInputDto dto) {
        User booker = findUserById(bookerId);
        if (dto == null) throw new ValidationException("body is null");
        if (dto.getItemId() == null) throw new ValidationException("itemId is null");
        if (dto.getStart() == null) throw new ValidationException("start is null");
        if (dto.getEnd() == null) throw new ValidationException("end is null");

        if (!dto.getStart().isBefore(dto.getEnd())) {
            throw new ValidationException("начало должно быть раньше конца");
        }

        LocalDateTime now = LocalDateTime.now();
        if (dto.getStart().isBefore(now) || dto.getEnd().isBefore(now)) {
            throw new ValidationException("начало/конец должны быть в будущем");
        }

        Item item = findItemById(dto.getItemId());

        if (item.getAvailable() != null && !item.getAvailable()) {
            throw new ValidationException("Товар недоступен");
        }

        if (item.getUserId() == bookerId) {
            throw new NotFoundException("Владелец не может забронировать собственный товар");
        }

        Booking booking = Booking.builder()
                .item(item)
                .booker(booker)
                .startTime(dto.getStart())
                .endTime(dto.getEnd())
                .status(Status.WAITING)
                .build();

        return bookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto approveOrRejectBooking(long ownerId, long bookingId, boolean approved) {
        Booking booking = findBookingById(bookingId);

        if (!booking.getItem().getUserId().equals(ownerId)) {
            throw new ForbiddenException("Одобрить/отклонить может только владелец");
        }

        findUserById(ownerId);

        if (booking.getItem().getUserId() != ownerId) {
            throw new ForbiddenException("Одобрить/отклонить может только владелец");
        }

        if (booking.getStatus() != Status.WAITING) {
            throw new ValidationException("Решение о бронировании уже принято");
        }

        booking.setStatus(approved ? Status.APPROVED : Status.REJECTED);
        return bookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Transactional(readOnly = true)
    @Override
    public BookingDto getBookingById(long bookingId, long userId) {
        findUserById(userId);

        Booking booking = findBookingById(bookingId);

        long ownerId = booking.getItem().getUserId();
        long bookerId = booking.getBooker().getId();

        if (userId != ownerId && userId != bookerId) {
            throw new NotFoundException("Бронирование не найдено");
        }

        return bookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getBookingsOfCurrentUser(State state, long bookerId) {
        findUserById(bookerId);
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findAllByBookerIdOrderByStartTimeDesc(bookerId);
            case CURRENT ->
                    bookingRepository.findAllByBookerIdAndStartTimeBeforeAndEndTimeAfterOrderByStartTimeDesc(bookerId, now, now);
            case PAST -> bookingRepository.findAllByBookerIdAndEndTimeBeforeOrderByStartTimeDesc(bookerId, now);
            case FUTURE -> bookingRepository.findAllByBookerIdAndStartTimeAfterOrderByStartTimeDesc(bookerId, now);
            case WAITING -> bookingRepository.findAllByBookerIdAndStatusOrderByStartTimeDesc(bookerId, Status.WAITING);
            case REJECTED ->
                    bookingRepository.findAllByBookerIdAndStatusOrderByStartTimeDesc(bookerId, Status.REJECTED);
        };

        return bookings.stream().map(bookingMapper::toBookingDto).toList();
    }

    @Override
    public List<BookingDto> getBookingsOfOwner(State state, long ownerId) {
        findUserById(ownerId);
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findAllByItemUserIdOrderByStartTimeDesc(ownerId);
            case CURRENT ->
                    bookingRepository.findAllByItemUserIdAndStartTimeBeforeAndEndTimeAfterOrderByStartTimeDesc(ownerId, now, now);
            case PAST -> bookingRepository.findAllByItemUserIdAndEndTimeBeforeOrderByStartTimeDesc(ownerId, now);
            case FUTURE -> bookingRepository.findAllByItemUserIdAndStartTimeAfterOrderByStartTimeDesc(ownerId, now);
            case WAITING -> bookingRepository.findAllByItemUserIdAndStatusOrderByStartTimeDesc(ownerId, Status.WAITING);
            case REJECTED ->
                    bookingRepository.findAllByItemUserIdAndStatusOrderByStartTimeDesc(ownerId, Status.REJECTED);
        };

        return bookings.stream().map(bookingMapper::toBookingDto).toList();
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Нет такого пользователя"));
    }

    private Item findItemById(Long ItemId) {
        return itemRepository.findById(ItemId).orElseThrow(() -> new NotFoundException("Нет такого пользователя"));
    }

    private Booking findBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId).orElseThrow(() -> new NotFoundException("Нет такого пользователя"));
    }
}
