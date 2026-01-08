package ru.practicum.shareit.item.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ItemEqualsHashCodeTest {

    @Test
    void shouldGetSetAllFields() {
        Item item = new Item();

        item.setId(1L);
        item.setUserId(10L);
        item.setName("Drill");
        item.setDescription("Power drill");
        item.setAvailable(true);
        item.setRequestId(99L);

        assertAll(
                () -> assertEquals(1L, item.getId()),
                () -> assertEquals(10L, item.getUserId()),
                () -> assertEquals("Drill", item.getName()),
                () -> assertEquals("Power drill", item.getDescription()),
                () -> assertTrue(item.getAvailable()),
                () -> assertEquals(99L, item.getRequestId())
        );
    }

    @Test
    void equals_shouldCoverBranches() {
        Item a = new Item();
        Item b = new Item();

        // 1) same reference
        assertTrue(a.equals(a));

        // 2) null
        assertFalse(a.equals(null));

        // 3) other class
        assertFalse(a.equals("x"));

        // 4) equal objects
        a.setId(1L);
        a.setUserId(10L);
        a.setName("n");
        a.setDescription(null);   // специально null, чтобы не было “случайных” различий
        a.setAvailable(true);

        b.setId(1L);
        b.setUserId(10L);
        b.setName("n");
        b.setDescription(null);
        b.setAvailable(true);

        assertTrue(a.equals(b));
        assertEquals(a.hashCode(), b.hashCode());

        // 5) not equal (одно поле отличается)
        b.setName("other");
        assertFalse(a.equals(b));
    }
}
