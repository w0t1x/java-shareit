package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto create(long userId, ItemRequestCreateDto dto);

    List<ItemRequestDto> getOwn(long userId);

    List<ItemRequestDto> getAllOther(long userId, int from, int size);

    ItemRequestDto getById(long userId, long requestId);
}

