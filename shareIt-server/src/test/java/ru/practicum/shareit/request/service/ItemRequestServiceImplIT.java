package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class ItemRequestServiceImplIT {

    @Autowired
    private ItemRequestService requestService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void create_thenGetOwn_containsRequest() {
        User user = userRepository.save(new User(null, "req@test.ru", "Requester"));

        ItemRequestCreateDto in = new ItemRequestCreateDto();
        in.setDescription("Need a drill");

        ItemRequestDto created = requestService.create(user.getId(), in);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getDescription()).isEqualTo("Need a drill");
        assertThat(created.getItems()).isNotNull();
        assertThat(created.getItems()).isEmpty();

        List<ItemRequestDto> own = requestService.getOwn(user.getId());
        assertThat(own).hasSize(1);
        assertThat(own.get(0).getId()).isEqualTo(created.getId());
    }
}
