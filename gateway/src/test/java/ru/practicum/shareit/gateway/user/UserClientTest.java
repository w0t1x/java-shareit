package ru.practicum.shareit.gateway.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.gateway.client.BaseClient;

import java.lang.reflect.Field;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class UserClientTest {

    private UserClient client;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() throws Exception {
        client = new UserClient("http://localhost:9090", new RestTemplateBuilder());
        RestTemplate rest = extractRestTemplate(client);
        server = MockRestServiceServer.bindTo(rest).build();
    }

    @Test
    void getById_sendsHeaderZero_andCorrectUrl() {
        server.expect(requestTo("http://localhost:9090/users/1"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(BaseClient.USER_HEADER, "0"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        client.getById(1L);
        server.verify();
    }

    private static RestTemplate extractRestTemplate(BaseClient client) throws Exception {
        Field f = BaseClient.class.getDeclaredField("rest");
        f.setAccessible(true);
        return (RestTemplate) f.get(client);
    }
}
