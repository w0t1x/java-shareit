package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDTO;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.practicum.shareit.testutil.TestJson.entity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ShareItRestTemplateTest {

    @Autowired
    TestRestTemplate rest;

    @Test
    void createUser_createItem_usingEntityHelper() {
        // user
        UserDTO user = new UserDTO(null, "User", "user@mail.com");
        ResponseEntity<UserDTO> uResp = rest.postForEntity("/users", user, UserDTO.class);
        assertThat(uResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        long userId = uResp.getBody().getId();

        // item
        ItemDto item = new ItemDto();
        item.setName("Item");
        item.setDescription("Desc");
        item.setAvailable(true);

        ResponseEntity<ItemDto> iResp = rest.exchange(
                "/items",
                HttpMethod.POST,
                entity(item, userId),
                ItemDto.class
        );

        assertThat(iResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(iResp.getBody().getId()).isNotNull();
    }
}





