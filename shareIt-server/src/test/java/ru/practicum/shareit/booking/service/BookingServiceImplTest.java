package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingServiceImplTest {

    private BookingRepository bookingRepository;
    private UserRepository userRepository;
    private ItemRepository itemRepository;
    private BookingMapper mapper;

    private BookingServiceImpl service;

    @BeforeEach
    void setUp() {
        bookingRepository = mock(BookingRepository.class);
        userRepository = mock(UserRepository.class);
        itemRepository = mock(ItemRepository.class);
        mapper = mock(BookingMapper.class);
        service = new BookingServiceImpl(bookingRepository, userRepository, itemRepository, mapper);
    }

    @Test
    void addBooking_nullBody_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(User.builder().id(1L).build()));
        assertThrows(ValidationException.class, () -> service.addBooking(1L, null));
    }

    @Test
    void addBooking_itemNotAvailable_throws() {
        User booker = User.builder().id(1L).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));

        Item item = Item.builder().id(10L).userId(2L).available(false).build();
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        BookingInputDto dto = new BookingInputDto(10L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));

        assertThrows(ValidationException.class, () -> service.addBooking(1L, dto));
    }

    @Test
    void addBooking_ownerCannotBookOwnItem_throws() {
        User booker = User.builder().id(1L).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));

        Item item = Item.builder().id(10L).userId(1L).available(true).build();
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        BookingInputDto dto = new BookingInputDto(10L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));

        assertThrows(NotFoundException.class, () -> service.addBooking(1L, dto));
    }

    @Test
    void addBooking_success_savesWAITING() {
        User booker = User.builder().id(1L).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));

        Item item = Item.builder().id(10L).userId(2L).available(true).build();
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        BookingInputDto dto = new BookingInputDto(10L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));

        Booking saved = Booking.builder().id(100L).booker(booker).item(item).status(Status.WAITING).build();
        when(bookingRepository.save(any())).thenReturn(saved);

        BookingDto out = new BookingDto();
        when(mapper.toBookingDto(saved)).thenReturn(out);

        BookingDto res = service.addBooking(1L, dto);
        assertSame(out, res);

        verify(bookingRepository).save(argThat(b -> b.getStatus() == Status.WAITING));
    }

    @Test
    void approve_notOwner_throwsForbidden() {
        Booking booking = Booking.builder()
                .id(1L)
                .item(Item.builder().id(10L).userId(99L).build())
                .status(Status.WAITING)
                .build();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        assertThrows(ForbiddenException.class, () -> service.approveOrRejectBooking(1L, 1L, true));
    }
}
