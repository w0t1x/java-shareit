package ru.practicum.shareit.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.user.dto.UserDTO;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.practicum.shareit.testutil.TestJson.json;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ShareItFullContextTest {

    private static final String HDR = "X-Sharer-User-Id";

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper mapper;

    @Test
    void fullFlow_users_items_bookings_requests() throws Exception {
        long ownerId = createUser("Owner", "owner@mail.com");
        long bookerId = createUser("Booker", "booker@mail.com");

        long itemId = createItem(ownerId, "Drill", "Good drill", true);

        long bookingId = createBooking(bookerId, itemId,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));

        mvc.perform(patch("/bookings/{id}", bookingId)
                        .header(HDR, ownerId)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        ItemRequestCreateDto req = new ItemRequestCreateDto();
        req.setDescription("Need a hammer");

        mvc.perform(post("/requests")
                        .header(HDR, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(mapper, req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()));

        mvc.perform(get("/requests")
                        .header(HDR, bookerId))
                .andExpect(status().isOk());
    }

    private long createUser(String name, String email) throws Exception {
        UserDTO dto = new UserDTO();
        dto.setName(name);
        dto.setEmail(email);

        String body = mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(mapper, dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andReturn().getResponse().getContentAsString();

        return mapper.readTree(body).get("id").asLong();
    }

    private long createItem(long ownerId, String name, String desc, boolean available) throws Exception {
        ItemDto dto = new ItemDto();
        dto.setName(name);
        dto.setDescription(desc);
        dto.setAvailable(available);

        String body = mvc.perform(post("/items")
                        .header(HDR, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(mapper, dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andReturn().getResponse().getContentAsString();

        return mapper.readTree(body).get("id").asLong();
    }

    private long createBooking(long bookerId, long itemId, LocalDateTime start, LocalDateTime end) throws Exception {
        BookingInputDto dto = new BookingInputDto();
        dto.setItemId(itemId);
        dto.setStart(start);
        dto.setEnd(end);

        String body = mvc.perform(post("/bookings")
                        .header(HDR, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(mapper, dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andReturn().getResponse().getContentAsString();

        return mapper.readTree(body).get("id").asLong();
    }
}

