package ru.practicum.shareit.gateway.booking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingDtoShort {
    private Long id;
    private Long bookerId;
    private Status status;
}
