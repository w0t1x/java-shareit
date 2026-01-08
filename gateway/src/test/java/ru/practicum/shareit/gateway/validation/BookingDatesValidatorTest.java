package ru.practicum.shareit.gateway.validation;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BookingDatesValidatorTest {

    @Test
    void validate_nullStartOrEnd_throws() {
        LocalDateTime now = LocalDateTime.now();
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
                () -> BookingDatesValidator.validate(null, now));
        assertTrue(ex1.getMessage().contains("must not be null"));

        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
                () -> BookingDatesValidator.validate(now, null));
        assertTrue(ex2.getMessage().contains("must not be null"));
    }

    @Test
    void validate_startNotBeforeEnd_throws() {
        LocalDateTime now = LocalDateTime.now();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> BookingDatesValidator.validate(now, now));
        assertTrue(ex.getMessage().contains("before"));

        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
                () -> BookingDatesValidator.validate(now.plusHours(2), now.plusHours(1)));
        assertTrue(ex2.getMessage().contains("before"));
    }

    @Test
    void validate_ok_doesNotThrow() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusHours(2);
        assertDoesNotThrow(() -> BookingDatesValidator.validate(start, end));
    }
}
