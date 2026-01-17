package ru.practicum.shareit.gateway.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.gateway.exception.GatewayErrorHandler;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemRequestController.class)
@Import(GatewayErrorHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemRequestClient client;

    @Test
    void create_callsClient_andReturnsOkJson() throws Exception {
        ItemRequestCreateDto in = new ItemRequestCreateDto();
        in.setDescription("Need drill");

        ItemRequestDto out = new ItemRequestDto();
        out.setId(1L);
        out.setDescription("Need drill");
        out.setCreated(LocalDateTime.now());
        out.setItems(List.of());

        Mockito.when(client.create(anyLong(), any()))
                .thenReturn(ResponseEntity.ok(out));

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(in)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Need drill"));
    }
}