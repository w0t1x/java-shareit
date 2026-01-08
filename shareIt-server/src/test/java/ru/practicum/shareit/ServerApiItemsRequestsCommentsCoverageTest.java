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
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ServerApiItemsRequestsCommentsCoverageTest {

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
    void items_requests_comments_and_lists_raise_complexity() throws Exception {
        long ownerId = createUser("Owner", uniq("owner"));
        long requesterId = createUser("Requester", uniq("req"));
        long bookerId = createUser("Booker", uniq("book"));

        // PATCH user (дергает update-ветку)
        mvc.perform(patch("/users/{id}", requesterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "Requester Updated"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Requester Updated"));

        // GET users + GET user
        mvc.perform(get("/users")).andExpect(status().isOk());
        mvc.perform(get("/users/{id}", ownerId)).andExpect(status().isOk());

        // ---- requests flow ----
        long requestId = createRequest(requesterId, "Need a drill");

        mvc.perform(get("/requests").header(USER_HEADER, requesterId))
                .andExpect(status().isOk());

        mvc.perform(get("/requests/all")
                        .header(USER_HEADER, ownerId)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        mvc.perform(get("/requests/{id}", requestId).header(USER_HEADER, ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestId));

        // ---- items flow ----
        long itemId = createItem(ownerId, "Drill", "Very good", true, requestId);

        // PATCH item
        mvc.perform(patch("/items/{id}", itemId)
                        .header(USER_HEADER, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("description", "Updated desc"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated desc"));

        // GET owner items (pagination)
        mvc.perform(get("/items")
                        .header(USER_HEADER, ownerId)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        // SEARCH: empty -> []
        mvc.perform(get("/items/search")
                        .header(USER_HEADER, ownerId)
                        .param("text", ""))
                .andExpect(status().isOk());

        // SEARCH: non-empty
        mvc.perform(get("/items/search")
                        .header(USER_HEADER, ownerId)
                        .param("text", "drill")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        // ---- bookings list endpoints (booker + owner) ----
        String start = iso(LocalDateTime.now().plusDays(1));
        String end = iso(LocalDateTime.now().plusDays(2));
        long bookingId = createBooking(bookerId, itemId, start, end);

        // approve=true
        mvc.perform(patch("/bookings/{id}", bookingId)
                        .header(USER_HEADER, ownerId)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        // Сдвигаем даты в прошлое, чтобы comment прошел (в ShareIt обычно можно комментить только после завершения)
        Booking b = bookingRepository.findById(bookingId).orElseThrow();
        b.setStartTime(LocalDateTime.now().minusDays(2).withNano(0));
        b.setEndTime(LocalDateTime.now().minusDays(1).withNano(0));
        bookingRepository.save(b);

        // booker bookings list
        mvc.perform(get("/bookings")
                        .header(USER_HEADER, bookerId)
                        .param("state", "PAST")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        // owner bookings list
        mvc.perform(get("/bookings/owner")
                        .header(USER_HEADER, ownerId)
                        .param("state", "PAST")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        // ---- comment ----
        mvc.perform(post("/items/{id}/comment", itemId)
                        .header(USER_HEADER, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("text", "Nice item!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Nice item!"));

        // GET item by id (проверяем что endpoint работает и маппит comments)
        mvc.perform(get("/items/{id}", itemId).header(USER_HEADER, ownerId))
                .andExpect(status().isOk());
    }

    // ---------- helpers ----------

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

    private long createRequest(long requesterId, String description) throws Exception {
        MvcResult res = mvc.perform(post("/requests")
                        .header(USER_HEADER, requesterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("description", description))))
                .andExpect(status().isOk())
                .andReturn();
        return readId(res);
    }

    private long createItem(long ownerId, String name, String description, boolean available, long requestId) throws Exception {
        MvcResult res = mvc.perform(post("/items")
                        .header(USER_HEADER, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", name,
                                "description", description,
                                "available", available,
                                "requestId", requestId
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
