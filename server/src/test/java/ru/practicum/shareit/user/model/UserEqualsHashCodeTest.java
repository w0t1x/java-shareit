package ru.practicum.shareit.user.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserEqualsHashCodeTest {

    @Test
    void equalsHashCode_works() {
        User a = new User();
        a.setId(1L);
        a.setEmail("u@u.ru");
        a.setName("u");

        User b = new User();
        b.setId(1L);
        b.setEmail("u@u.ru");
        b.setName("u");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
