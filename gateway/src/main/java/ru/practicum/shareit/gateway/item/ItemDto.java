package ru.practicum.shareit.gateway.item;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import ru.practicum.shareit.gateway.booking.BookingDtoShort;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private Long requestId;

    private BookingDtoShort lastBooking;
    private BookingDtoShort nextBooking;

    private List<CommentDto> comments = new ArrayList<>();
}
