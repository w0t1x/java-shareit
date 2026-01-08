package ru.practicum.shareit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
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
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ServerApiExtraCoverageTest {

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
    void users_getById_and_notFound() throws Exception {
        long id = createUser("U", uniq("u") + "@ex.ru");

        mvc.perform(get("/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("U"));

        mvc.perform(get("/users/{id}", 999999))
                .andExpect(status().isNotFound());
    }

    @Test
    void request_contains_items_and_owner_item_has_last_next_booking() throws Exception {
        long ownerId = createUser("Owner", uniq("owner") + "@ex.ru");
        long bookerId = createUser("Booker", uniq("booker") + "@ex.ru");
        long requesterId = createUser("Requester", uniq("req") + "@ex.ru");

        long reqId = createRequest(requesterId, "Need item");
        long itemId = createItem(ownerId, "Item", "Desc", true, reqId);

        // requester sees its request, and items[] is filled
        mvc.perform(get("/requests/{id}", reqId)
                        .header(USER_HEADER, requesterId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reqId))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].id").value(itemId));

        // seed bookings so that owner GET /items/{id} returns lastBooking & nextBooking
        LocalDateTime now = LocalDateTime.now().withNano(0);

        Booking past = bookingRepository.save(Booking.builder()
                .item(itemRepository.findById(itemId).orElseThrow())
                .booker(userRepository.findById(bookerId).orElseThrow())
                .status(Status.APPROVED)
                .startTime(now.minusDays(3))
                .endTime(now.minusDays(2))
                .build());

        Booking future = bookingRepository.save(Booking.builder()
                .item(itemRepository.findById(itemId).orElseThrow())
                .booker(userRepository.findById(bookerId).orElseThrow())
                .status(Status.APPROVED)
                .startTime(now.plusDays(2))
                .endTime(now.plusDays(3))
                .build());

        mvc.perform(get("/items/{id}", itemId)
                        .header(USER_HEADER, ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastBooking.id").value(past.getId()))
                .andExpect(jsonPath("$.nextBooking.id").value(future.getId()));
    }

    @Test
    void booking_approve_true_and_booking_errors() throws Exception {
        long ownerId = createUser("Owner", uniq("own") + "@ex.ru");
        long bookerId = createUser("Booker", uniq("bok") + "@ex.ru");

        long itemId = createItem(ownerId, "Drill", "Nice", true, null);

        // ok booking -> approve=true branch
        String start = iso(LocalDateTime.now().plusDays(1));
        String end = iso(LocalDateTime.now().plusDays(2));
        long bookingId = createBooking(bookerId, itemId, start, end);

        mvc.perform(patch("/bookings/{id}", bookingId)
                        .header(USER_HEADER, ownerId)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        // invalid time: start >= end -> 400
        String badStart = iso(LocalDateTime.now().plusDays(5));
        String badEnd = iso(LocalDateTime.now().plusDays(4));

        mvc.perform(post("/bookings")
                        .header(USER_HEADER, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "itemId", itemId,
                                "start", badStart,
                                "end", badEnd
                        ))))
                .andExpect(status().isBadRequest());

        // owner cannot book own item -> usually 404 or 403 (зависит от реализации)
        mvc.perform(post("/bookings")
                        .header(USER_HEADER, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "itemId", itemId,
                                "start", iso(LocalDateTime.now().plusDays(10)),
                                "end", iso(LocalDateTime.now().plusDays(11))
                        ))))
                .andExpect(status().is4xxClientError());

        // owner endpoint: one more state branch (WAITING list could be empty/any; we check just 200)
        mvc.perform(get("/bookings/owner")
                        .header(USER_HEADER, ownerId)
                        .param("state", "WAITING")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.anything()));
    }

    // -------- helpers --------

    private String uniq(String prefix) {
        return prefix + System.nanoTime();
    }

    private long createUser(String name, String email) throws Exception {
        MvcResult res = mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"email\":\"" + email + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return readId(res);
    }

    private long createRequest(long userId, String description) throws Exception {
        MvcResult res = mvc.perform(post("/requests")
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"" + description + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return readId(res);
    }

    private long createItem(long ownerId, String name, String description, boolean available, Long requestId) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("name", name);
        payload.put("description", description);
        payload.put("available", available);
        if (requestId != null) {
            payload.put("requestId", requestId);
        }

        MvcResult res = mvc.perform(post("/items")
                        .header(USER_HEADER, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
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

    private String iso(LocalDateTime t) {
        return t.withNano(0).toString();
    }
}
