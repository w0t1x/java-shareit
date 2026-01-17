package ru.practicum.shareit.gateway.request;

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

class ItemRequestClientTest {

    private ItemRequestClient client;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() throws Exception {
        client = new ItemRequestClient("http://localhost:9090", new RestTemplateBuilder());
        RestTemplate rest = extractRestTemplate(client);
        server = MockRestServiceServer.bindTo(rest).build();
    }

    @Test
    void getAll_buildsAllUrlWithQueryParams() {
        server.expect(requestTo("http://localhost:9090/requests/all?from=0&size=20"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(BaseClient.USER_HEADER, "2"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        client.getAll(2L, 0, 20);
        server.verify();
    }

    private static RestTemplate extractRestTemplate(BaseClient client) throws Exception {
        Field f = BaseClient.class.getDeclaredField("rest");
        f.setAccessible(true);
        return (RestTemplate) f.get(client);
    }
}
