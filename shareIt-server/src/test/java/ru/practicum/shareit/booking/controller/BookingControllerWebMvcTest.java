package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.user.dto.UserDTO;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerWebMvcTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper mapper;

    @MockBean
    BookingService bookingService;

    @Test
    void addBooking_returnsBookingDto() throws Exception {
        long userId = 1L;
        long itemId = 2L;

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(1);

        BookingInputDto in = new BookingInputDto(itemId, start, end);

        BookingDto out = new BookingDto(
                10L, start, end, Status.WAITING,
                new UserDTO(userId, "u", "u@mail.com"),
                itemDto(itemId)
        );

        when(bookingService.addBooking(eq(userId), ArgumentMatchers.any(BookingInputDto.class)))
                .thenReturn(out);

        mvc.perform(post("/bookings")
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(in)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));

        verify(bookingService, times(1)).addBooking(eq(userId), any(BookingInputDto.class));
    }

    @Test
    void approve_returnsBookingDto() throws Exception {
        long ownerId = 1L;
        long bookingId = 10L;

        BookingDto out = new BookingDto(
                bookingId, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                Status.APPROVED, new UserDTO(2L, "b", "b@mail.com"), itemDto(3L)
        );

        when(bookingService.approveOrRejectBooking(ownerId, bookingId, true)).thenReturn(out);

        mvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .param("approved", "true")
                        .header(USER_HEADER, ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(bookingService).approveOrRejectBooking(ownerId, bookingId, true);
    }

    @Test
    void getById_returnsBookingDto() throws Exception {
        long userId = 1L;
        long bookingId = 10L;

        BookingDto out = new BookingDto(
                bookingId, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                Status.WAITING, new UserDTO(2L, "b", "b@mail.com"), itemDto(3L)
        );

        when(bookingService.getBookingById(bookingId, userId)).thenReturn(out);

        mvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header(USER_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId));

        verify(bookingService).getBookingById(bookingId, userId);
    }

    @Test
    void getBookingsOfCurrentUser_returnsList() throws Exception {
        long userId = 1L;

        BookingDto b1 = new BookingDto(1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                Status.WAITING, new UserDTO(2L, "b", "b@mail.com"), itemDto(3L));
        BookingDto b2 = new BookingDto(2L, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(4),
                Status.WAITING, new UserDTO(2L, "b", "b@mail.com"), itemDto(4L));

        when(bookingService.getBookingsOfCurrentUser(State.ALL, userId)).thenReturn(List.of(b1, b2));

        mvc.perform(get("/bookings")
                        .header(USER_HEADER, userId)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(bookingService).getBookingsOfCurrentUser(State.ALL, userId);
    }

    private ItemDto itemDto(long id) {
        ItemDto i = new ItemDto();
        i.setId(id);
        i.setName("item");
        i.setDescription("desc");
        i.setAvailable(true);
        return i;
    }
}

