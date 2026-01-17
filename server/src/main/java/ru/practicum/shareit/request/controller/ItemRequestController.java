package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    private static final String HEADER = "X-Sharer-User-Id";
    private final ItemRequestService service;

    @PostMapping
    public ItemRequestDto create(@RequestHeader(HEADER) long userId,
                                 @RequestBody ItemRequestCreateDto dto) {
        return service.create(userId, dto);
    }

    @GetMapping
    public List<ItemRequestDto> getOwn(@RequestHeader(HEADER) long userId) {
        return service.getOwn(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAll(@RequestHeader(HEADER) long userId,
                                       @RequestParam(defaultValue = "0") int from,
                                       @RequestParam(defaultValue = "10") int size) {
        return service.getAllOther(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getById(@RequestHeader(HEADER) long userId,
                                  @PathVariable long requestId) {
        return service.getById(userId, requestId);
    }
}


