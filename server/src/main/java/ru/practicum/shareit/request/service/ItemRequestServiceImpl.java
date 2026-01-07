package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemAnswerDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository requestRepo;
    private final UserRepository userRepo;
    private final ItemRepository itemRepo;

    @Transactional
    @Override
    public ItemRequestDto create(long userId, ItemRequestCreateDto dto) {
        userRepo.findById(userId).orElseThrow(() -> new NotFoundException("Нет такого пользователя"));

        if (dto == null || dto.getDescription() == null || dto.getDescription().isBlank()) {
            throw new ValidationException("description must not be blank");
        }

        ItemRequest saved = requestRepo.save(ItemRequest.builder()
                .description(dto.getDescription())
                .requestorId(userId)
                .created(LocalDateTime.now())
                .build());

        return toDto(saved, List.of());
    }

    @Override
    public List<ItemRequestDto> getOwn(long userId) {
        userRepo.findById(userId).orElseThrow(() -> new NotFoundException("Нет такого пользователя"));

        List<ItemRequest> requests = requestRepo.findAllByRequestorIdOrderByCreatedDesc(userId);
        return attachItems(requests);
    }

    @Override
    public List<ItemRequestDto> getAllOther(long userId, int from, int size) {
        userRepo.findById(userId).orElseThrow(() -> new NotFoundException("Нет такого пользователя"));

        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);
        List<ItemRequest> requests = requestRepo.findAllByRequestorIdNotOrderByCreatedDesc(userId, pageable).toList();

        return attachItems(requests);
    }

    @Override
    public ItemRequestDto getById(long userId, long requestId) {
        userRepo.findById(userId).orElseThrow(() -> new NotFoundException("Нет такого пользователя"));

        ItemRequest req = requestRepo.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден"));

        List<Item> items = itemRepo.findAllByRequestIdOrderByIdAsc(req.getId());
        return toDto(req, items.stream().map(this::toAnswer).toList());
    }

    private List<ItemRequestDto> attachItems(List<ItemRequest> requests) {
        if (requests.isEmpty()) return List.of();

        List<Long> ids = requests.stream().map(ItemRequest::getId).toList();
        List<Item> items = itemRepo.findAllByRequestIdIn(ids);

        Map<Long, List<ItemAnswerDto>> byReq = new HashMap<>();
        for (Item i : items) {
            if (i.getRequestId() == null) continue;
            byReq.computeIfAbsent(i.getRequestId(), k -> new ArrayList<>()).add(toAnswer(i));
        }

        return requests.stream()
                .map(r -> toDto(r, byReq.getOrDefault(r.getId(), List.of())))
                .toList();
    }

    private ItemAnswerDto toAnswer(Item item) {
        return new ItemAnswerDto(item.getId(), item.getName(), item.getUserId());
    }

    private ItemRequestDto toDto(ItemRequest r, List<ItemAnswerDto> items) {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(r.getId());
        dto.setDescription(r.getDescription());
        dto.setCreated(r.getCreated());
        dto.setItems(new ArrayList<>(items));
        return dto;
    }
}

