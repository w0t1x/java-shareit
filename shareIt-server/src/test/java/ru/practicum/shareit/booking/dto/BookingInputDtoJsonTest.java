package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingInputDtoJsonTest {

    @Autowired
    JacksonTester<BookingInputDto> json;

    @Test
    void serialize_and_deserialize() throws Exception {
        BookingInputDto dto = new BookingInputDto();
        dto.setItemId(99L);
        dto.setStart(LocalDateTime.of(2030, 1, 1, 10, 0));
        dto.setEnd(LocalDateTime.of(2030, 1, 2, 10, 0));

        var written = json.write(dto);
        assertThat(written).hasJsonPathNumberValue("$.itemId");
        assertThat(written).hasJsonPathStringValue("$.start");
        assertThat(written).hasJsonPathStringValue("$.end");

        BookingInputDto parsed = json.parseObject(written.getJson());
        assertThat(parsed.getItemId()).isEqualTo(99L);
        assertThat(parsed.getStart()).isEqualTo(dto.getStart());
        assertThat(parsed.getEnd()).isEqualTo(dto.getEnd());
    }
}

