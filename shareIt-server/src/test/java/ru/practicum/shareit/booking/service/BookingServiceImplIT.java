package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDTO;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingServiceImplIT {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private UserService userService;
    @Autowired
    private ItemService itemService;

    @Test
    void getBookingsOfCurrentUser_returnsCreatedBooking() {
        UserDTO owner = userService.add(new UserDTO(null, "Owner", "owner1@mail.com"));
        UserDTO booker = userService.add(new UserDTO(null, "Booker", "booker1@mail.com"));

        ItemDto item = new ItemDto();
        item.setName("Item");
        item.setDescription("Desc");
        item.setAvailable(true);
        ItemDto savedItem = itemService.create(owner.getId(), item);

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(1);
        BookingDto created = bookingService.addBooking(booker.getId(), new BookingInputDto(savedItem.getId(), start, end));

        List<BookingDto> bookings = bookingService.getBookingsOfCurrentUser(State.ALL, booker.getId()); // <-- 2 аргумента
        assertThat(bookings).extracting(BookingDto::getId).contains(created.getId());
    }

    @Test
    void getBookingsOfOwner_returnsBookingsForOwnerItems() {
        UserDTO owner = userService.add(new UserDTO(null, "Owner2", "owner2@mail.com"));
        UserDTO booker = userService.add(new UserDTO(null, "Booker2", "booker2@mail.com"));

        ItemDto item = new ItemDto();
        item.setName("Item2");
        item.setDescription("Desc2");
        item.setAvailable(true);
        ItemDto savedItem = itemService.create(owner.getId(), item);

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(1);
        bookingService.addBooking(booker.getId(), new BookingInputDto(savedItem.getId(), start, end));

        List<BookingDto> ownerBookings = bookingService.getBookingsOfOwner(State.ALL, owner.getId()); // <-- 2 аргумента
        assertThat(ownerBookings).isNotEmpty();
        assertThat(ownerBookings.get(0).getItem().getId()).isEqualTo(savedItem.getId());
    }
}


