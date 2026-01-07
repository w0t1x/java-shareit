package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class BookingServiceImplIT {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void addBooking_savesAndReturnsWaiting() {
        User owner = userRepository.save(new User(null, "owner@test.ru", "Owner"));
        User booker = userRepository.save(new User(null, "booker@test.ru", "Booker"));

        Item item = itemRepository.save(Item.builder()
                .userId(owner.getId())
                .name("Bike")
                .description("Fast bike")
                .available(true)
                .requestId(null)
                .build());

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        BookingInputDto in = new BookingInputDto(item.getId(), start, end);

        BookingDto out = bookingService.addBooking(booker.getId(), in);

        assertThat(out.getId()).isNotNull();
        assertThat(out.getStatus()).isEqualTo(Status.WAITING);
        assertThat(out.getStart()).isEqualTo(start);
        assertThat(out.getEnd()).isEqualTo(end);
        assertThat(out.getBooker().getId()).isEqualTo(booker.getId());
        assertThat(out.getItem().getId()).isEqualTo(item.getId());
    }
}
