package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemDtoJsonTest {

    @Autowired
    private JacksonTester<ItemDto> json;

    @Test
    void serialize_defaultComments_isArray() throws Exception {
        ItemDto dto = new ItemDto();
        dto.setId(1L);
        dto.setName("Item");
        dto.setDescription("Desc");
        dto.setAvailable(true);

        var content = json.write(dto);

        assertThat(content).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(content).extractingJsonPathArrayValue("$.comments").isNotNull();
    }
}
