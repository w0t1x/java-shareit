package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class ItemServiceImplIT {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void create_thenGetOwnerItems_containsCreatedItem() {
        User owner = userRepository.save(new User(null, "owner@test.ru", "Owner"));

        ItemDto in = new ItemDto();
        in.setName("Drill");
        in.setDescription("Simple drill");
        in.setAvailable(true);

        ItemDto created = itemService.create(owner.getId(), in);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getComments()).isNotNull();
        assertThat(created.getLastBooking()).isNull();
        assertThat(created.getNextBooking()).isNull();

        List<ItemDto> ownerItems = itemService.getAllByOwner(owner.getId());
        assertThat(ownerItems).hasSize(1);
        assertThat(ownerItems.get(0).getId()).isEqualTo(created.getId());
        assertThat(ownerItems.get(0).getName()).isEqualTo("Drill");
    }
}
