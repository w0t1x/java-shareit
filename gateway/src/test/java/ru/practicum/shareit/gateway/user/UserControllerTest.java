package ru.practicum.shareit.gateway.user;

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
import ru.practicum.shareit.gateway.exception.GatewayErrorHandler;

import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@Import(GatewayErrorHandler.class)
class UserControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;

    @MockBean private UserClient client;

    @Test
    void create_blankName_returns400() throws Exception {
        UserDTO dto = new UserDTO();
        dto.setName(" ");
        dto.setEmail("a@b.ru");

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("name must not be blank")));
    }

    @Test
    void create_valid_callsClient() throws Exception {
        Mockito.when(client.create(any())).thenReturn(ResponseEntity.ok(Map.of("id", 1)));

        UserDTO dto = new UserDTO();
        dto.setName("Ann");
        dto.setEmail("ann@b.ru");

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        Mockito.verify(client).create(eq(dto));
    }
}
