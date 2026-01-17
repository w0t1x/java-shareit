package ru.practicum.shareit.gateway.client;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class BaseClientTest {

    static class TestClient extends BaseClient {
        TestClient(String baseUrl, RestTemplateBuilder builder) {
            super(baseUrl, builder);
        }

        void ping(long userId) {
            get("/ping", userId);
        }
    }

    @Test
    void get_addsUserHeader_andUsesBaseUrl() throws Exception {
        TestClient client = new TestClient("http://localhost:9090", new RestTemplateBuilder());
        RestTemplate rest = extractRestTemplate(client);

        MockRestServiceServer server = MockRestServiceServer.bindTo(rest).build();
        server.expect(requestTo("http://localhost:9090/ping"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(BaseClient.USER_HEADER, "123"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        client.ping(123L);
        server.verify();
    }

    private static RestTemplate extractRestTemplate(BaseClient client) throws Exception {
        Field f = BaseClient.class.getDeclaredField("rest");
        f.setAccessible(true);
        return (RestTemplate) f.get(client);
    }
}
