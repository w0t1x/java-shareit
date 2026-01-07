package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDTO;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.practicum.shareit.testutil.TestJson.json;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ShareItApiNegativeIntegrationTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;

    @Test
    void bookings_invalidState_returns400() throws Exception {
        long u = createUser("U", "u@mail.com");

        mvc.perform(get("/bookings")
                        .header(USER_HEADER, u)
                        .param("state", "BAD_STATE")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void booking_unavailableItem_returns4xx() throws Exception {
        long ownerId = createUser("Owner", "o@mail.com");
        long bookerId = createUser("Booker", "b@mail.com");

        long itemId = createItem(ownerId, "Item", "Desc", false);

        BookingInputDto dto = new BookingInputDto();
        dto.setItemId(itemId);
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(LocalDateTime.now().plusDays(2));

        mvc.perform(post("/bookings")
                        .header(USER_HEADER, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(mapper, dto)))
                .andExpect(status().is4xxClientError());
    }

    // helpers (минимум)
    private long createUser(String name, String email) throws Exception {
        UserDTO dto = new UserDTO();
        dto.setName(name);
        dto.setEmail(email);

        String body = mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(mapper, dto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return mapper.readTree(body).get("id").asLong();
    }

    private long createItem(long ownerId, String name, String desc, boolean available) throws Exception {
        ItemDto dto = new ItemDto();
        dto.setName(name);
        dto.setDescription(desc);
        dto.setAvailable(available);

        String body = mvc.perform(post("/items")
                        .header(USER_HEADER, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(mapper, dto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return mapper.readTree(body).get("id").asLong();
    }
}

