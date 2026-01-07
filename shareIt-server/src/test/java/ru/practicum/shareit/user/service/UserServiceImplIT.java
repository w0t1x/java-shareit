package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDTO;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class UserServiceImplIT {

    @Autowired
    private UserService userService;

    @Test
    void add_thenGet_returnsSameUser() {
        UserDTO in = new UserDTO(null, "Ivan", "ivan@test.ru");

        UserDTO created = userService.add(in);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Ivan");
        assertThat(created.getEmail()).isEqualTo("ivan@test.ru");

        UserDTO loaded = userService.get(created.getId());
        assertThat(loaded.getId()).isEqualTo(created.getId());
        assertThat(loaded.getName()).isEqualTo("Ivan");
        assertThat(loaded.getEmail()).isEqualTo("ivan@test.ru");
    }
}
