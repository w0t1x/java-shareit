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
    private JacksonTester<BookingInputDto> json;

    @Test
    void serialize_containsStartEndAsIso() throws Exception {
        BookingInputDto dto = new BookingInputDto(
                10L,
                LocalDateTime.of(2026, 1, 1, 10, 0),
                LocalDateTime.of(2026, 1, 2, 10, 0)
        );

        var content = json.write(dto);

        assertThat(content).extractingJsonPathNumberValue("$.itemId").isEqualTo(10);
        assertThat(content).extractingJsonPathStringValue("$.start").isEqualTo("2026-01-01T10:00:00");
        assertThat(content).extractingJsonPathStringValue("$.end").isEqualTo("2026-01-02T10:00:00");
    }
}
