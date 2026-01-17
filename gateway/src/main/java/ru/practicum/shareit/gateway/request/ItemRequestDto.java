package ru.practicum.shareit.gateway.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemRequestDto {
    private Long id;
    private String description;
    private LocalDateTime created;
    private List<ItemAnswerDto> items = new ArrayList<>();
}
