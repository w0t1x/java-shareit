package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerMockMvcTest {

    private static final String HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemService itemService;

    @Test
    void create_ok() throws Exception {
        ItemDto in = new ItemDto();
        in.setName("Drill");
        in.setDescription("desc");
        in.setAvailable(true);

        when(itemService.create(eq(1L), any())).thenReturn(new ItemDto(10L, "Drill", "desc", true, null, null, null, List.of()));

        mvc.perform(post("/items")
                        .header(HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(in)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void update_ok() throws Exception {
        ItemDto patch = new ItemDto();
        patch.setName("New");

        when(itemService.update(eq(1L), eq(10L), any())).thenReturn(new ItemDto(10L, "New", "desc", true, null, null, null, List.of()));

        mvc.perform(patch("/items/10")
                        .header(HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New"));
    }

    @Test
    void getById_ok() throws Exception {
        when(itemService.getById(1L, 10L)).thenReturn(new ItemDto(10L, "Drill", "desc", true, null, null, null, List.of()));

        mvc.perform(get("/items/10").header(HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void getOwnerItems_ok() throws Exception {
        when(itemService.getAllByOwner(1L)).thenReturn(List.of(new ItemDto(10L, "Drill", "desc", true, null, null, null, List.of())));

        mvc.perform(get("/items")
                        .header(HEADER, 1)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10));
    }

    @Test
    void search_ok() throws Exception {
        when(itemService.search(1L, "dr")).thenReturn(List.of(new ItemDto(10L, "Drill", "desc", true, null, null, null, List.of())));

        mvc.perform(get("/items/search")
                        .header(HEADER, 1)
                        .param("text", "dr"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Drill"));
    }

    @Test
    void addComment_ok() throws Exception {
        CommentCreateDto in = new CommentCreateDto();
        in.setText("Nice");

        CommentDto out = new CommentDto();
        out.setId(5L);
        out.setText("Nice");
        out.setAuthorName("Ivan");
        out.setCreated(LocalDateTime.of(2026, 1, 1, 10, 0));

        when(itemService.addComment(eq(1L), eq(10L), any())).thenReturn(out);

        mvc.perform(post("/items/10/comment")
                        .header(HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(in)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.text").value("Nice"));
    }
}
