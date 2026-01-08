package ru.practicum.shareit.item.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ItemEqualsHashCodeTest {

    @Test
    void equalsHashCode_works() {
        Item a = Item.builder().id(1L).userId(2L).name("n").description("d").available(true).requestId(null).build();
        Item b = Item.builder().id(1L).userId(2L).name("n").description("d").available(true).requestId(null).build();

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
