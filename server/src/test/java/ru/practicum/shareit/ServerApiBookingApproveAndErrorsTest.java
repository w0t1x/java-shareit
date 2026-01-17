package ru.practicum.shareit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ServerApiBookingApproveAndErrorsTest {

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
    void approve_true_branch_and_booking_create_errors() throws Exception {
        long ownerId = createUser("Owner", uniq("owner"));
        long bookerId = createUser("Booker", uniq("booker"));
        long strangerId = createUser("Stranger", uniq("str"));

        long itemId = createItem(ownerId, "Item", "Desc", true);

        // create booking (WAITING)
        String start = iso(LocalDateTime.now().plusDays(1));
        String end = iso(LocalDateTime.now().plusDays(2));
        long bookingId = createBooking(bookerId, itemId, start, end);

        // approve=true (ветка approve)
        mvc.perform(patch("/bookings/{id}", bookingId)
                        .header(USER_HEADER, ownerId)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        // get by id: owner ok, booker ok
        mvc.perform(get("/bookings/{id}", bookingId).header(USER_HEADER, ownerId))
                .andExpect(status().isOk());

        mvc.perform(get("/bookings/{id}", bookingId).header(USER_HEADER, bookerId))
                .andExpect(status().isOk());

        // get by id: stranger -> любой 4xx (ветка "не участник")
        mvc.perform(get("/bookings/{id}", bookingId).header(USER_HEADER, strangerId))
                .andExpect(status().is4xxClientError());

        // ---- ошибки создания брони ----

        // end before start -> 4xx (ветка валидации дат)
        mvc.perform(post("/bookings")
                        .header(USER_HEADER, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "itemId", itemId,
                                "start", iso(LocalDateTime.now().plusDays(5)),
                                "end", iso(LocalDateTime.now().plusDays(4))
                        ))))
                .andExpect(status().is4xxClientError());

        // unavailable item -> 4xx
        long unavailableItemId = createItem(ownerId, "No", "No", false);
        mvc.perform(post("/bookings")
                        .header(USER_HEADER, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "itemId", unavailableItemId,
                                "start", iso(LocalDateTime.now().plusDays(1)),
                                "end", iso(LocalDateTime.now().plusDays(2))
                        ))))
                .andExpect(status().is4xxClientError());

        // owner tries to book own item -> 4xx
        mvc.perform(post("/bookings")
                        .header(USER_HEADER, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "itemId", itemId,
                                "start", iso(LocalDateTime.now().plusDays(7)),
                                "end", iso(LocalDateTime.now().plusDays(8))
                        ))))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void item_owner_sees_last_and_next_booking_branches() throws Exception {
        long ownerId = createUser("Owner", uniq("own2"));
        long bookerId = createUser("Booker", uniq("bok2"));

        long itemId = createItem(ownerId, "Drill", "Nice", true);

        User booker = userRepository.findById(bookerId).orElseThrow();
        Item item = itemRepository.findById(itemId).orElseThrow();
        LocalDateTime now = LocalDateTime.now().withNano(0);

        // past approved -> lastBooking
        bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .status(Status.APPROVED)
                .startTime(now.minusDays(3))
                .endTime(now.minusDays(2))
                .build());

        // future approved -> nextBooking
        bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .status(Status.APPROVED)
                .startTime(now.plusDays(2))
                .endTime(now.plusDays(3))
                .build());

        mvc.perform(get("/items/{id}", itemId).header(USER_HEADER, ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastBooking.id").exists())
                .andExpect(jsonPath("$.nextBooking.id").exists());
    }

    // -------- helpers --------

    private String uniq(String prefix) {
        return prefix + System.nanoTime() + "@ex.ru";
    }

    private String iso(LocalDateTime t) {
        return t.withNano(0).toString();
    }

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

    private long createBooking(long bookerId, long itemId, String start, String end) throws Exception {
        MvcResult res = mvc.perform(post("/bookings")
                        .header(USER_HEADER, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "itemId", itemId,
                                "start", start,
                                "end", end
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
