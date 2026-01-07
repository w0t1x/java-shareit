package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDTO;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ShareItApiIT {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private TestRestTemplate rest;

    @Test
    void fullHappyPath_createUser_createItem_createBooking() {
        // create owner
        UserDTO owner = new UserDTO(null, "Owner", "owner@mail.com");
        ResponseEntity<UserDTO> ownerResp = rest.postForEntity("/users", owner, UserDTO.class);
        assertThat(ownerResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        Long ownerId = ownerResp.getBody().getId();
        assertThat(ownerId).isNotNull();

        // create booker
        UserDTO booker = new UserDTO(null, "Booker", "booker@mail.com");
        ResponseEntity<UserDTO> bookerResp = rest.postForEntity("/users", booker, UserDTO.class);
        assertThat(bookerResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        Long bookerId = bookerResp.getBody().getId();
        assertThat(bookerId).isNotNull();

        // create item (as owner)
        ItemDto itemIn = new ItemDto();
        itemIn.setName("Drill");
        itemIn.setDescription("Good drill");
        itemIn.setAvailable(true);

        ResponseEntity<ItemDto> itemResp = rest.exchange(
                "/items",
                HttpMethod.POST,
                new HttpEntity<>(itemIn, headersWithUser(ownerId)),
                ItemDto.class
        );
        assertThat(itemResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        Long itemId = itemResp.getBody().getId();
        assertThat(itemId).isNotNull();

        // create booking (as booker)
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(1);
        BookingInputDto bookingIn = new BookingInputDto(itemId, start, end);

        ResponseEntity<String> bookingResp = rest.exchange(
                "/bookings",
                HttpMethod.POST,
                new HttpEntity<>(bookingIn, headersWithUser(bookerId)),
                String.class
        );
        assertThat(bookingResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(bookingResp.getBody()).contains("\"id\"");
    }

    private HttpHeaders headersWithUser(Long userId) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.add(USER_HEADER, String.valueOf(userId));
        return h;
    }
}



