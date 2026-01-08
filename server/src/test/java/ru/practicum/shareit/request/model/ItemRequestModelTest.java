package ru.practicum.shareit.request.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ItemRequestModelTest {

    @Test
    void builder_setsFields() {
        LocalDateTime t = LocalDateTime.now();

        ItemRequest r = ItemRequest.builder()
                .description("d")
                .requestorId(2L)
                .created(t)
                .build();

        assertNotNull(r);
        assertEquals("d", r.getDescription());
        assertEquals(2L, r.getRequestorId());
        assertEquals(t, r.getCreated());
    }
}
