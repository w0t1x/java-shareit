package ru.practicum.shareit.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerWebMvcTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper mapper;

    @MockBean
    ItemRequestService service;

    @Test
    void create_returnsDto() throws Exception {
        long userId = 1L;

        ItemRequestCreateDto in = new ItemRequestCreateDto();
        in.setDescription("need a drill");

        ItemRequestDto out = new ItemRequestDto(10L, "need a drill", LocalDateTime.now(), List.of());

        when(service.create(eq(userId), any(ItemRequestCreateDto.class))).thenReturn(out);

        mvc.perform(post("/requests")
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(in)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.description").value("need a drill"));

        verify(service).create(eq(userId), any(ItemRequestCreateDto.class));
    }

    @Test
    void getOwn_returnsList() throws Exception {
        long userId = 1L;
        when(service.getOwn(userId)).thenReturn(List.of(
                new ItemRequestDto(1L, "d1", LocalDateTime.now(), List.of()),
                new ItemRequestDto(2L, "d2", LocalDateTime.now(), List.of())
        ));

        mvc.perform(get("/requests")
                        .header(USER_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(service).getOwn(userId);
    }

    @Test
    void getAll_returnsList() throws Exception {
        long userId = 1L;
        when(service.getAllOther(userId, 0, 10)).thenReturn(List.of(
                new ItemRequestDto(3L, "d3", LocalDateTime.now(), List.of())
        ));

        mvc.perform(get("/requests/all")
                        .header(USER_HEADER, userId)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(service).getAllOther(userId, 0, 10);
    }

    @Test
    void getById_returnsDto() throws Exception {
        long userId = 1L;
        long requestId = 10L;

        when(service.getById(userId, requestId))
                .thenReturn(new ItemRequestDto(requestId, "d", LocalDateTime.now(), List.of()));

        mvc.perform(get("/requests/{requestId}", requestId)
                        .header(USER_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));

        verify(service).getById(userId, requestId);
    }
}
