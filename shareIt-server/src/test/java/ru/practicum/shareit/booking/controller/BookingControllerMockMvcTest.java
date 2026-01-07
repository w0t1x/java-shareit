package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDTO;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerMockMvcTest {

    private static final String HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private BookingService bookingService;

    @Test
    void addBooking_ok() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        BookingInputDto in = new BookingInputDto(10L, start, end);

        BookingDto out = new BookingDto(
                1L, start, end, Status.WAITING,
                new UserDTO(2L, "Booker", "b@test.ru"),
                new ItemDto(10L, "Item", "desc", true, null, null, null, List.of())
        );

        when(bookingService.addBooking(eq(2L), any())).thenReturn(out);

        mvc.perform(post("/bookings")
                        .header(HEADER, 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(in)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void approve_ok() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        BookingDto out = new BookingDto(
                1L, start, end, Status.APPROVED,
                new UserDTO(2L, "Booker", "b@test.ru"),
                new ItemDto(10L, "Item", "desc", true, null, null, null, List.of())
        );

        when(bookingService.approveOrRejectBooking(1L, 1L, true)).thenReturn(out);

        mvc.perform(patch("/bookings/1")
                        .header(HEADER, 1)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void getById_ok() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        when(bookingService.getBookingById(1L, 2L)).thenReturn(new BookingDto(
                1L, start, end, Status.WAITING,
                new UserDTO(2L, "Booker", "b@test.ru"),
                new ItemDto(10L, "Item", "desc", true, null, null, null, List.of())
        ));

        mvc.perform(get("/bookings/1").header(HEADER, 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getMy_ok_withPagination() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        List<BookingDto> all = List.of(
                new BookingDto(1L, start, end, Status.WAITING, null, null),
                new BookingDto(2L, start, end, Status.WAITING, null, null)
        );

        when(bookingService.getBookingsOfCurrentUser(eq(State.ALL), eq(2L))).thenReturn(all);

        mvc.perform(get("/bookings")
                        .header(HEADER, 2)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getOwner_ok() throws Exception {
        when(bookingService.getBookingsOfOwner(eq(State.ALL), eq(1L)))
                .thenReturn(List.of(new BookingDto(1L, null, null, Status.WAITING, null, null)));

        mvc.perform(get("/bookings/owner")
                        .header(HEADER, 1)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }
}
