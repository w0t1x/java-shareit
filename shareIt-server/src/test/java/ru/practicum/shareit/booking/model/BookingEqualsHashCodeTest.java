package ru.practicum.shareit.booking.model;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BookingEqualsHashCodeTest {

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

        LocalDateTime s = LocalDateTime.now();
        LocalDateTime e = s.plusHours(1);

        Booking a = Booking.builder()
                .id(5L)
                .booker(u)
                .item(it)
                .status(Status.WAITING)
                .startTime(s)
                .endTime(e)
                .build();

        Booking b = Booking.builder()
                .id(5L)
                .booker(u)
                .item(it)
                .status(Status.WAITING)
                .startTime(s)
                .endTime(e)
                .build();

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
