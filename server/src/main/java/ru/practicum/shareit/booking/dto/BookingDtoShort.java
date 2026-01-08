package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.model.Status;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingDtoShort {
    private Long id;
    private Long bookerId;
    private Status status;
}
