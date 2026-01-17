package ru.practicum.shareit.gateway.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemAnswerDto {
    private Long id;
    private String name;
    private Long ownerId; // это userId у Item
}
