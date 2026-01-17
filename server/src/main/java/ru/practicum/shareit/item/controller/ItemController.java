package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private static final String HEADER = "X-Sharer-User-Id";
    private final ItemService itemService;

    @PostMapping
    public ItemDto create(@RequestHeader(HEADER) long userId,
                          @RequestBody ItemDto dto) {
        return itemService.create(userId, dto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader(HEADER) long userId,
                          @PathVariable long itemId,
                          @RequestBody ItemDto dto) {
        return itemService.update(userId, itemId, dto);
    }

    @GetMapping("/{itemId}")
    public ItemDto getById(@RequestHeader(HEADER) long userId,
                           @PathVariable long itemId) {
        return itemService.getById(userId, itemId);
    }

    @GetMapping
    public List<ItemDto> getOwnerItems(@RequestHeader(HEADER) long userId,
                                       @RequestParam(defaultValue = "0") int from,
                                       @RequestParam(defaultValue = "10") int size) {
        return itemService.getAllByOwner(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestHeader(HEADER) long userId,
                                @RequestParam String text,
                                @RequestParam(defaultValue = "0") int from,
                                @RequestParam(defaultValue = "10") int size) {
        return itemService.search(userId, text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader(HEADER) long userId,
                                 @PathVariable long itemId,
                                 @RequestBody CommentCreateDto dto) {
        return itemService.addComment(userId, itemId, dto);
    }
}

