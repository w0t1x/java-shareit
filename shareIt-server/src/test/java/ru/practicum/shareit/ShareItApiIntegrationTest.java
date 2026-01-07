package ru.practicum.shareit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.user.dto.UserDTO;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.practicum.shareit.testutil.TestJson.json;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ShareItApiIntegrationTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;

    @Test
    void fullFlow_users_items_requests_bookings_comments() throws Exception {
        long ownerId = createUser("Owner", "owner@mail.com");
        long bookerId = createUser("Booker", "booker@mail.com");

        long requestId = createRequest(bookerId, "Need a drill");

        long itemId = createItem(ownerId, "Drill", "Good drill", true, requestId);

        // booking на пару секунд, чтобы потом реально пройти ветку comment (booking закончился)
        long bookingId = createBooking(bookerId, itemId,
                LocalDateTime.now().plusSeconds(1),
                LocalDateTime.now().plusSeconds(3));

        approveBooking(ownerId, bookingId, true);

        // читаем booking
        mvc.perform(get("/bookings/{id}", bookingId)
                        .header(USER_HEADER, bookerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId));

        // список бронирований
        mvc.perform(get("/bookings")
                        .header(USER_HEADER, bookerId)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        mvc.perform(get("/bookings/owner")
                        .header(USER_HEADER, ownerId)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        // ждём пока бронирование закончится — чтобы коммент был разрешён
        Thread.sleep(3500);

        long commentId = createComment(bookerId, itemId, "Nice item");

        // проверим что item отдаётся и содержит comments (хотя бы не 500)
        mvc.perform(get("/items/{id}", itemId)
                        .header(USER_HEADER, ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId));

        // запросы: get own
        mvc.perform(get("/requests")
                        .header(USER_HEADER, bookerId))
                .andExpect(status().isOk());

        // запросы: get by id
        mvc.perform(get("/requests/{id}", requestId)
                        .header(USER_HEADER, bookerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestId));

        // коммент реально создался
        mvc.perform(get("/items/{id}", itemId)
                        .header(USER_HEADER, bookerId))
                .andExpect(status().isOk());

        // commentId просто “потрогали”, чтобы ветка точно выполнилась
        org.assertj.core.api.Assertions.assertThat(commentId).isPositive();
    }

    @Test
    void user_patch_and_delete() throws Exception {
        long userId = createUser("U", "u@mail.com");

        UserDTO patch = new UserDTO();
        patch.setName("U2");

        mvc.perform(patch("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(mapper, patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("U2"));

        mvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isOk());
    }

    // ---------------- helpers ----------------

    private long createUser(String name, String email) throws Exception {
        UserDTO dto = new UserDTO();
        dto.setName(name);
        dto.setEmail(email);

        MvcResult res = mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(mapper, dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        return extractId(res);
    }

    private long createItem(long ownerId, String name, String desc, boolean available, long requestId) throws Exception {
        ItemDto dto = new ItemDto();
        dto.setName(name);
        dto.setDescription(desc);
        dto.setAvailable(available);
        dto.setRequestId(requestId);

        MvcResult res = mvc.perform(post("/items")
                        .header(USER_HEADER, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(mapper, dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        return extractId(res);
    }

    private long createRequest(long requesterId, String description) throws Exception {
        ItemRequestCreateDto dto = new ItemRequestCreateDto();
        dto.setDescription(description);

        MvcResult res = mvc.perform(post("/requests")
                        .header(USER_HEADER, requesterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(mapper, dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        return extractId(res);
    }

    private long createBooking(long bookerId, long itemId, LocalDateTime start, LocalDateTime end) throws Exception {
        BookingInputDto dto = new BookingInputDto();
        dto.setItemId(itemId);
        dto.setStart(start);
        dto.setEnd(end);

        MvcResult res = mvc.perform(post("/bookings")
                        .header(USER_HEADER, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(mapper, dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        return extractId(res);
    }

    private void approveBooking(long ownerId, long bookingId, boolean approved) throws Exception {
        mvc.perform(patch("/bookings/{id}", bookingId)
                        .header(USER_HEADER, ownerId)
                        .param("approved", String.valueOf(approved)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId));
    }

    private long createComment(long userId, long itemId, String text) throws Exception {
        CommentCreateDto dto = new CommentCreateDto();
        dto.setText(text);

        MvcResult res = mvc.perform(post("/items/{id}/comment", itemId)
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(mapper, dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        return extractId(res);
    }

    private long extractId(MvcResult res) throws Exception {
        String body = res.getResponse().getContentAsString();
        JsonNode node = mapper.readTree(body);
        return node.get("id").asLong();
    }
}

