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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingServiceImplTest {

    @Autowired
    BookingService bookingService;
    @Autowired
    UserService userService;
    @Autowired
    ItemService itemService;

    @Test
    void add_approve_and_stateQueries() {
        long ownerId = userService.add(user("owner", "o@mail.com")).getId();
        long bookerId = userService.add(user("booker", "b@mail.com")).getId();

        long itemId = itemService.create(ownerId, item("Item", "Desc", true)).getId();

        BookingInputDto in = new BookingInputDto();
        in.setItemId(itemId);
        in.setStart(LocalDateTime.now().plusDays(1));
        in.setEnd(LocalDateTime.now().plusDays(2));

        BookingDto created = bookingService.addBooking(bookerId, in);
        bookingService.approveOrRejectBooking(ownerId, created.getId(), true);

        assertThat(bookingService.getBookingsOfCurrentUser(State.ALL, bookerId)).isNotEmpty();
        assertThat(bookingService.getBookingsOfOwner(State.ALL, ownerId)).isNotEmpty();
    }

    private UserDTO user(String name, String email) {
        UserDTO u = new UserDTO();
        u.setName(name);
        u.setEmail(email);
        return u;
    }

    private ItemDto item(String name, String desc, boolean available) {
        ItemDto i = new ItemDto();
        i.setName(name);
        i.setDescription(desc);
        i.setAvailable(available);
        return i;
    }
}



