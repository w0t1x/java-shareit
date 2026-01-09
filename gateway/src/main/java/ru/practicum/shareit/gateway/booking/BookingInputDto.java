package ru.practicum.shareit.gateway.booking;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingInputDto {
    private Long itemId;

    @FutureOrPresent(message = "Дата не должна быть в прошлом")
    @NotNull(message = "Дата не должна быть пустой")
    private LocalDateTime start;

    @FutureOrPresent(message = "Дата не должна быть в прошлом")
    @NotNull(message = "Дата не должна быть пустой")
    private LocalDateTime end;
}
