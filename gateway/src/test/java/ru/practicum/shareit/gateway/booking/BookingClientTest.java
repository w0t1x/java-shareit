package ru.practicum.shareit.gateway.booking;

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

class BookingClientTest {

    private BookingClient client;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() throws Exception {
        client = new BookingClient("http://localhost:9090", new RestTemplateBuilder());
        RestTemplate rest = extractRestTemplate(client);
        server = MockRestServiceServer.bindTo(rest).build();
    }

    @Test
    void create_postsToBookingsRoot() {
        server.expect(requestTo("http://localhost:9090/bookings"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(BaseClient.USER_HEADER, "7"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        client.create(7L, java.util.Map.of("x", "y"));
        server.verify();
    }

    @Test
    void getOwner_buildsOwnerUrlWithQueryParams() {
        server.expect(requestTo("http://localhost:9090/bookings/owner?state=ALL&from=0&size=10"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(BaseClient.USER_HEADER, "1"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        client.getOwner(1L, "ALL", 0, 10);
        server.verify();
    }

    private static RestTemplate extractRestTemplate(BaseClient client) throws Exception {
        Field f = BaseClient.class.getDeclaredField("rest");
        f.setAccessible(true);
        return (RestTemplate) f.get(client);
    }
}
