package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDTO;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceImplIT {

    @Autowired
    UserService userService;

    @Test
    void add_getAll_patch_delete() {
        UserDTO u = new UserDTO();
        u.setName("Ivan");
        u.setEmail("ivan@mail.ru");

        UserDTO created = userService.add(u);
        assertThat(created.getId()).isNotNull();

        assertThat(userService.getAll()).isNotEmpty();

        UserDTO patch = new UserDTO();
        patch.setName("Ivan Updated");
        UserDTO updated = userService.patch(patch, created.getId());
        assertThat(updated.getName()).isEqualTo("Ivan Updated");

        userService.delete(created.getId());
        assertThatThrownBy(() -> userService.get(created.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void add_validationErrors() {
        assertThatThrownBy(() -> userService.add(null))
                .isInstanceOf(ValidationException.class);

        UserDTO bad = new UserDTO();
        bad.setName(" ");
        bad.setEmail("a@a.ru");
        assertThatThrownBy(() -> userService.add(bad))
                .isInstanceOf(ValidationException.class);
    }
}

