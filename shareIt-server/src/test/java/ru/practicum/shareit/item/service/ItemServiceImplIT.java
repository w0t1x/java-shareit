package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.UserDTO;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemServiceImplIT {

    @Autowired
    UserService userService;
    @Autowired
    ItemService itemService;

    @Autowired
    UserRepository userRepository;
    @Autowired
    ItemRepository itemRepository;
    @Autowired
    BookingRepository bookingRepository;

    @Test
    void create_update_get_search_comment() {
        long ownerId = userService.add(user("owner", "o@mail.ru")).getId();
        long bookerId = userService.add(user("booker", "b@mail.ru")).getId();

        ItemDto created = itemService.create(ownerId, item("Drill", "Good", true));
        assertThat(created.getId()).isNotNull();

        // forbidden update
        assertThatThrownBy(() -> itemService.update(bookerId, created.getId(), item("X", null, null)))
                .isInstanceOf(ForbiddenException.class);

        // search blank -> empty
        assertThat(itemService.search(bookerId, "  ")).isEmpty();

        // comment: сначала без бронирования -> ValidationException
        CommentCreateDto c = new CommentCreateDto();
        c.setText("Nice");
        assertThatThrownBy(() -> itemService.addComment(bookerId, created.getId(), c))
                .isInstanceOf(ValidationException.class);

        // добавим APPROVED booking в прошлом напрямую
        User booker = userRepository.findById(bookerId).orElseThrow();
        Item itemEntity = itemRepository.findById(created.getId()).orElseThrow();

        bookingRepository.save(Booking.builder()
                .item(itemEntity)
                .booker(booker)
                .status(Status.APPROVED)
                .startTime(LocalDateTime.now().minusDays(2))
                .endTime(LocalDateTime.now().minusDays(1))
                .build());

        // теперь comment allowed
        assertThat(itemService.addComment(bookerId, created.getId(), c).getId()).isNotNull();
    }

    private static UserDTO user(String name, String email) {
        UserDTO u = new UserDTO();
        u.setName(name);
        u.setEmail(email);
        return u;
    }

    private static ItemDto item(String name, String desc, Boolean available) {
        ItemDto i = new ItemDto();
        i.setName(name);
        i.setDescription(desc);
        i.setAvailable(available);
        return i;
    }
}

