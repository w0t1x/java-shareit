package ru.practicum.shareit.gateway.validation;

import java.time.LocalDateTime;

public class BookingDatesValidator {

    private BookingDatesValidator() {
    }

    public static void validate(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("start/end must not be null");
        }
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("start must be before end");
        }
    }
}
