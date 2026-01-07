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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerMockMvcTest {

    private static final String HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemRequestService requestService;

    @Test
    void create_ok() throws Exception {
        ItemRequestCreateDto in = new ItemRequestCreateDto();
        in.setDescription("Need item");

        ItemRequestDto out = new ItemRequestDto(1L, "Need item", LocalDateTime.of(2026, 1, 1, 10, 0), List.of());

        when(requestService.create(eq(1L), any())).thenReturn(out);

        mvc.perform(post("/requests")
                        .header(HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(in)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getOwn_ok() throws Exception {
        when(requestService.getOwn(1L))
                .thenReturn(List.of(new ItemRequestDto(1L, "Need", null, List.of())));

        mvc.perform(get("/requests").header(HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getAll_ok() throws Exception {
        when(requestService.getAllOther(1L, 0, 10))
                .thenReturn(List.of(new ItemRequestDto(2L, "Other", null, List.of())));

        mvc.perform(get("/requests/all")
                        .header(HEADER, 1)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2));
    }

    @Test
    void getById_ok() throws Exception {
        when(requestService.getById(1L, 5L))
                .thenReturn(new ItemRequestDto(5L, "One", null, List.of()));

        mvc.perform(get("/requests/5").header(HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }
}
