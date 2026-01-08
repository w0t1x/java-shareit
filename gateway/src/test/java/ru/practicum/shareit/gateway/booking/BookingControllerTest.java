package ru.practicum.shareit.gateway.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.gateway.exception.GatewayErrorHandler;

import java.time.LocalDateTime;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class)
@Import(GatewayErrorHandler.class)
class BookingControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;

    @MockBean private BookingClient client;

    @Test
    void getMy_unknownState_returns400() throws Exception {
        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "BAD"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Unknown state")));
        verifyNoInteractions(client);
    }

    @Test
    void create_invalidDates_returns400() throws Exception {
        BookingInputDto dto = new BookingInputDto(1L,
                LocalDateTime.now().plusHours(2),
                LocalDateTime.now().plusHours(1));

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("start must be before end")));
        verifyNoInteractions(client);
    }

    @Test
    void create_valid_callsClient() throws Exception {
        Mockito.when(client.create(eq(1L), any()))
                .thenReturn(ResponseEntity.ok(Map.of("ok", true)));

        BookingInputDto dto = new BookingInputDto(1L,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2));

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        Mockito.verify(client).create(eq(1L), any());
    }
}
