package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ServerApiBookingStatesSwitchTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private ItemRequestRepository requestRepository;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDb() {
        commentRepository.deleteAll();
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        requestRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void booking_list_states_cover_all_switch_branches() throws Exception {
        long ownerId = createUser("Owner", "own" + System.nanoTime() + "@ex.ru");
        long bookerId = createUser("Booker", "bok" + System.nanoTime() + "@ex.ru");
        long itemId = createItem(ownerId, "Item", "Desc", true);

        User booker = userRepository.findById(bookerId).orElseThrow();
        Item item = itemRepository.findById(itemId).orElseThrow();

        LocalDateTime now = LocalDateTime.now().withNano(0);

        // past
        bookingRepository.save(Booking.builder()
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
                .startTime(now.plusDays(2))
                .endTime(now.plusDays(3))
                .build());

        // future rejected
        bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .status(Status.REJECTED)
                .startTime(now.plusDays(4))
                .endTime(now.plusDays(5))
                .build());

        // --- booker endpoint ---
        assertStateOk("/bookings", bookerId, "ALL");
        assertStateOk("/bookings", bookerId, "CURRENT");
        assertStateOk("/bookings", bookerId, "PAST");
        assertStateOk("/bookings", bookerId, "FUTURE");
        assertStateOk("/bookings", bookerId, "WAITING");
        assertStateOk("/bookings", bookerId, "REJECTED");

        // also cover "state omitted" default branch
        mvc.perform(get("/bookings").header(USER_HEADER, bookerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // --- owner endpoint ---
        assertStateOk("/bookings/owner", ownerId, "ALL");
        assertStateOk("/bookings/owner", ownerId, "CURRENT");
        assertStateOk("/bookings/owner", ownerId, "PAST");
        assertStateOk("/bookings/owner", ownerId, "FUTURE");
        assertStateOk("/bookings/owner", ownerId, "WAITING");
        assertStateOk("/bookings/owner", ownerId, "REJECTED");

        mvc.perform(get("/bookings/owner").header(USER_HEADER, ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    private void assertStateOk(String path, long userId, String state) throws Exception {
        mvc.perform(get(path)
                        .header(USER_HEADER, userId)
                        .param("state", state)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // -------- helpers --------

    private long createUser(String name, String email) throws Exception {
        MvcResult res = mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", name, "email", email))))
                .andExpect(status().isOk())
                .andReturn();
        return readId(res);
    }

    private long createItem(long ownerId, String name, String description, boolean available) throws Exception {
        MvcResult res = mvc.perform(post("/items")
                        .header(USER_HEADER, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", name,
                                "description", description,
                                "available", available
                        ))))
                .andExpect(status().isOk())
                .andReturn();
        return readId(res);
    }

    private long readId(MvcResult res) throws Exception {
        JsonNode node = objectMapper.readTree(res.getResponse().getContentAsString());
        return node.get("id").asLong();
    }
}
