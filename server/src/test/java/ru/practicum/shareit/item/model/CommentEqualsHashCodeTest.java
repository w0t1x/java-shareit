package ru.practicum.shareit.item.model;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CommentEqualsHashCodeTest {

    @Test
    void shouldGetSetAllFields() {
        Comment c = new Comment();

        Item item = new Item();
        User author = new User();

        LocalDateTime created = LocalDateTime.of(2020, 1, 1, 0, 0);

        c.setId(1L);
        c.setText("text");
        c.setItem(item);
        c.setAuthor(author);
        c.setCreated(created);

        assertAll(
                () -> assertEquals(1L, c.getId()),
                () -> assertEquals("text", c.getText()),
                () -> assertSame(item, c.getItem()),
                () -> assertSame(author, c.getAuthor()),
                () -> assertEquals(created, c.getCreated())
        );
    }

    @Test
    void equals_shouldCoverBranches() {
        LocalDateTime created = LocalDateTime.of(2020, 1, 1, 0, 0);

        Comment a = new Comment();
        Comment b = new Comment();

        // 1) same reference
        assertTrue(a.equals(a));

        // 2) null
        assertFalse(a.equals(null));

        // 3) other class
        assertFalse(a.equals("x"));

        // 4) equal objects (ВАЖНО: created одинаковый!)
        a.setId(1L);
        a.setText("t");
        a.setItem(null);
        a.setAuthor(null);
        a.setCreated(created);

        b.setId(1L);
        b.setText("t");
        b.setItem(null);
        b.setAuthor(null);
        b.setCreated(created);

        assertTrue(a.equals(b));
        assertEquals(a.hashCode(), b.hashCode());

        // 5) not equal
        b.setText("other");
        assertFalse(a.equals(b));
    }
}
