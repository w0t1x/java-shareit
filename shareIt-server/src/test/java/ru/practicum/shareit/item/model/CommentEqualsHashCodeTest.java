package ru.practicum.shareit.item.model;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CommentEqualsHashCodeTest {

    @Test
    void equalsHashCode_works() {
        User u = new User();
        u.setId(1L);
        u.setEmail("u@u.ru");
        u.setName("u");

        Item it = new Item();
        it.setId(2L);
        it.setUserId(10L);
        it.setName("i");
        it.setDescription("d");
        it.setAvailable(true);

        LocalDateTime t = LocalDateTime.now();

        Comment a = Comment.builder().id(1L).text("x").author(u).item(it).created(t).build();
        Comment b = Comment.builder().id(1L).text("x").author(u).item(it).created(t).build();

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
