package ru.practicum.shareit.booking.model;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookingStatesApiTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDb() {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void states_cover_past_current_future_waiting_rejected_and_forbidden() throws Exception {
        User owner = new User();
        owner.setName("owner");
        owner.setEmail("owner@ex.ru");
        owner = userRepository.save(owner);

        User booker = new User();
        booker.setName("booker");
        booker.setEmail("booker@ex.ru");
        booker = userRepository.save(booker);

        User other = new User();
        other.setName("other");
        other.setEmail("other@ex.ru");
        other = userRepository.save(other);

        Item item = Item.builder()
                .userId(owner.getId())
                .name("i")
                .description("d")
                .available(true)
                .build();
        item = itemRepository.save(item);

        LocalDateTime now = LocalDateTime.now();

        // past
        Booking past = bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .status(Status.APPROVED)
                .startTime(now.minusDays(3))
                .endTime(now.minusDays(2))
                .build());

        // current
        bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .status(Status.APPROVED)
                .startTime(now.minusHours(1))
                .endTime(now.plusHours(1))
                .build());

        // future waiting
        bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .status(Status.WAITING)
                .startTime(now.plusDays(1))
                .endTime(now.plusDays(2))
                .build());

        // future rejected
        bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .status(Status.REJECTED)
                .startTime(now.plusDays(3))
                .endTime(now.plusDays(4))
                .build());

        // ALL
        mvc.perform(get("/bookings")
                        .header(USER_HEADER, booker.getId())
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)));

        // PAST
        mvc.perform(get("/bookings")
                        .header(USER_HEADER, booker.getId())
                        .param("state", "PAST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // CURRENT
        mvc.perform(get("/bookings")
                        .header(USER_HEADER, booker.getId())
                        .param("state", "CURRENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // FUTURE (у тебя FUTURE возвращает WAITING+REJECTED, поэтому 2)
        mvc.perform(get("/bookings")
                        .header(USER_HEADER, booker.getId())
                        .param("state", "FUTURE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        // WAITING
        mvc.perform(get("/bookings")
                        .header(USER_HEADER, booker.getId())
                        .param("state", "WAITING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // REJECTED
        mvc.perform(get("/bookings")
                        .header(USER_HEADER, booker.getId())
                        .param("state", "REJECTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // owner endpoint тоже должен отдавать 4
        mvc.perform(get("/bookings/owner")
                        .header(USER_HEADER, owner.getId())
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)));

        // ВАЖНО: в твоей реализации для "левого" пользователя это 404, а не 403
        mvc.perform(get("/bookings/{id}", past.getId())
                        .header(USER_HEADER, other.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", Matchers.containsString("Бронирование не найдено")));

        // invalid state -> 400 (у тебя текст "Unknown state")
        mvc.perform(get("/bookings")
                        .header(USER_HEADER, booker.getId())
                        .param("state", "BAD"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", Matchers.containsString("Unknown state")));
    }
}
