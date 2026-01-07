package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDTO;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.practicum.shareit.testutil.TestJson.json;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerWebMvcTest {

    private static final String HDR = "X-Sharer-User-Id";

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper mapper;

    @MockBean
    BookingService bookingService;

    @Test
    void addBooking_ok() throws Exception {
        long userId = 1L;
        long itemId = 2L;

        BookingInputDto in = new BookingInputDto();
        in.setItemId(itemId);
        in.setStart(LocalDateTime.now().plusDays(1));
        in.setEnd(LocalDateTime.now().plusDays(2));

        BookingDto out = new BookingDto(
                10L,
                in.getStart(),
                in.getEnd(),
                Status.WAITING,
                new UserDTO(userId, "u", "u@mail.com"),
                item(itemId)
        );

        when(bookingService.addBooking(org.mockito.ArgumentMatchers.eq(userId), any(BookingInputDto.class)))
                .thenReturn(out);

        mvc.perform(post("/bookings")
                        .header(HDR, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(mapper, in)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    private ItemDto item(long id) {
        ItemDto i = new ItemDto();
        i.setId(id);
        i.setName("item");
        i.setDescription("desc");
        i.setAvailable(true);
        return i;
    }
}


