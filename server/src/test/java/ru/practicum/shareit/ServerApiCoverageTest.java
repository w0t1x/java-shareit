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
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ServerApiCoverageTest {

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
    void users_items_requests_basic_and_errors() throws Exception {
        long ownerId = createUser("Owner", "owner@ex.ru");
        long viewerId = createUser("Viewer", "viewer@ex.ru");     // ЭТОГО не удаляем, будем им читать item
        long toDeleteId = createUser("ToDelete", "del@ex.ru");    // удаляем его отдельно

        // users: get all
        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));

        // users: patch
        mvc.perform(patch("/users/{id}", ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Owner2\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Owner2"));

        // users: conflict (duplicate email)
        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Dup\",\"email\":\"owner@ex.ru\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));

        // users: delete (не тот, кто будет дальше в header)
        mvc.perform(delete("/users/{id}", toDeleteId))
                .andExpect(status().isOk());

        // requests: create + get own + get by id + get all
        long reqId = createRequest(ownerId, "Need drill");

        mvc.perform(get("/requests")
                        .header(USER_HEADER, ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mvc.perform(get("/requests/{id}", reqId)
                        .header(USER_HEADER, ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reqId));

        mvc.perform(get("/requests/all")
                        .header(USER_HEADER, ownerId)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        // item create with requestId (ветка requestRepo.findById)
        long itemId = createItem(ownerId, "Drill", "Nice", true, reqId);

        // item getById для НЕ-владельца (в твоём сервисе last/next зануляются)
        mvc.perform(get("/items/{id}", itemId)
                        .header(USER_HEADER, viewerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastBooking").doesNotExist())
                .andExpect(jsonPath("$.nextBooking").doesNotExist());

        // item update: forbidden для не-владельца
        mvc.perform(patch("/items/{id}", itemId)
                        .header(USER_HEADER, viewerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"X\"}"))
                .andExpect(status().isForbidden());

        // item update: blank name -> 400
        mvc.perform(patch("/items/{id}", itemId)
                        .header(USER_HEADER, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", Matchers.containsString("name must not be blank")));

        // item update ok
        mvc.perform(patch("/items/{id}", itemId)
                        .header(USER_HEADER, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Updated\",\"available\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated"));

        // items list owner + search
        mvc.perform(get("/items")
                        .header(USER_HEADER, ownerId)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        mvc.perform(get("/items/search")
                        .header(USER_HEADER, ownerId)
                        .param("text", "drill"))
                .andExpect(status().isOk());

        // search blank -> []
        mvc.perform(get("/items/search")
                        .header(USER_HEADER, ownerId)
                        .param("text", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        // create item with missing requestId -> 404
        mvc.perform(post("/items")
                        .header(USER_HEADER, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"A\",\"description\":\"B\",\"available\":true,\"requestId\":999999}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void comment_forbidden_then_allowed_and_visible_in_item() throws Exception {
        long ownerId = createUser("Owner", "o@ex.ru");
        long bookerId = createUser("Booker", "b@ex.ru");

        long itemId = createItem(ownerId, "Item", "Desc", true, null);

        // without past approved booking -> 400
        mvc.perform(post("/items/{id}/comment", itemId)
                        .header(USER_HEADER, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"hi\"}"))
                .andExpect(status().isBadRequest());

        // seed past approved booking to allow comment
        User booker = userRepository.findById(bookerId).orElseThrow();
        Item item = itemRepository.findById(itemId).orElseThrow();
        LocalDateTime now = LocalDateTime.now();

        bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .status(Status.APPROVED)
                .startTime(now.minusDays(2))
                .endTime(now.minusDays(1))
                .build());

        mvc.perform(post("/items/{id}/comment", itemId)
                        .header(USER_HEADER, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"good\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("good"))
                .andExpect(jsonPath("$.authorName").value("Booker"));

        mvc.perform(get("/items/{id}", itemId)
                        .header(USER_HEADER, ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments", hasSize(1)))
                .andExpect(jsonPath("$.comments[0].text").value("good"));
    }

    @Test
    void booking_create_approve_false_and_get() throws Exception {
        long ownerId = createUser("Owner", "owner2@ex.ru");
        long bookerId = createUser("Booker", "booker2@ex.ru");

        long itemId = createItem(ownerId, "Drill", "Nice", true, null);

        String start = iso(LocalDateTime.now().plusDays(1));
        String end = iso(LocalDateTime.now().plusDays(2));

        long bookingId = createBooking(bookerId, itemId, start, end);

        mvc.perform(patch("/bookings/{id}", bookingId)
                        .header(USER_HEADER, ownerId)
                        .param("approved", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        mvc.perform(get("/bookings/{id}", bookingId)
                        .header(USER_HEADER, ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId));

        mvc.perform(get("/bookings")
                        .header(USER_HEADER, bookerId)
                        .param("state", "BAD"))
                .andExpect(status().isBadRequest());
    }

    // ---------------- helpers ----------------

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
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("itemId", itemId);
        payload.put("start", start);
        payload.put("end", end);

        MvcResult res = mvc.perform(post("/bookings")
                        .header(USER_HEADER, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
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
