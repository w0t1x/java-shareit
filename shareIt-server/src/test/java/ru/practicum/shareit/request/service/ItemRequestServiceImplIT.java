package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.user.dto.UserDTO;
import ru.practicum.shareit.user.service.UserService;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemRequestServiceImplIT {

    @Autowired
    UserService userService;
    @Autowired
    ItemRequestService requestService;

    @Test
    void create_and_getFlows() {
        long u1 = userService.add(user("u1", "u1@mail.ru")).getId();
        long u2 = userService.add(user("u2", "u2@mail.ru")).getId();

        ItemRequestCreateDto dto = new ItemRequestCreateDto();
        dto.setDescription("Need a drill");

        var created = requestService.create(u1, dto);
        assertThat(created.getId()).isNotNull();

        assertThat(requestService.getOwn(u1)).isNotEmpty();
        assertThat(requestService.getAllOther(u2, 0, 10)).isNotNull();
        assertThat(requestService.getById(u2, created.getId()).getId()).isEqualTo(created.getId());

        // validation
        ItemRequestCreateDto bad = new ItemRequestCreateDto();
        bad.setDescription(" ");
        assertThatThrownBy(() -> requestService.create(u1, bad))
                .isInstanceOf(ValidationException.class);
    }

    private static UserDTO user(String name, String email) {
        UserDTO u = new UserDTO();
        u.setName(name);
        u.setEmail(email);
        return u;
    }
}

