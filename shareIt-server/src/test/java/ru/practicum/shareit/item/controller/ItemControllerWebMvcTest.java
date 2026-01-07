package ru.practicum.shareit.item.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ErrorHandler;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
@Import(ErrorHandler.class)
class ItemControllerWebMvcTest {
    private static final String HDR = "X-Sharer-User-Id";

    @Autowired
    MockMvc mvc;
    @MockBean
    ItemService itemService;

    @Test
    void create_ok() throws Exception {
        ItemDto out = new ItemDto();
        out.setId(10L);
        out.setName("Drill");

        when(itemService.create(eq(1L), any())).thenReturn(out);

        mvc.perform(post("/items")
                        .header(HDR, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Drill\",\"description\":\"Good\",\"available\":true}"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.id").value(10));
    }
}

