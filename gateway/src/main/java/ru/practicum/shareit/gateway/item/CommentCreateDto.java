package ru.practicum.shareit.gateway.item;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentCreateDto {
    @NotBlank
    private String text;
}