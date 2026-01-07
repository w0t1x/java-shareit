package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto create(Long ownerId, ItemDto dto);

    ItemDto update(Long ownerId, Long itemId, ItemDto dto);

    ItemDto getById(Long userId, Long itemId); // userId пригодится позже (для бронирований и отзывов)

    List<ItemDto> getAllByOwner(Long ownerId);

    List<ItemDto> search(Long userId, String text);

    CommentDto addComment(long userId, long itemId, CommentCreateDto dto);
}
