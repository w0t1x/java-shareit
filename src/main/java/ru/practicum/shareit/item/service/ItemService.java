package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto create(Long ownerId, ItemDto dto);

    ItemDto update(Long ownerId, Long itemId, ItemDto dto);

    ItemDto getById(Long userId, Long itemId); // userId пригодится позже (для бронирований и отзывов)

    List<ItemDto> getOwnerItems(Long ownerId);

    List<ItemDto> search(String text);

    void delete(Long id);
}
