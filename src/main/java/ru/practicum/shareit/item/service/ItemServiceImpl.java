package ru.practicum.shareit.item.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemMapper itemMapper;
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Override
    public ItemDto create(Long ownerId, ItemDto dto) {
        userStorage.getUser(ownerId);

        validateNewItem(dto);

        Item item = ItemMapper.toItem(dto, ownerId);
        Item saved = itemStorage.addItem(item);
        return itemMapper.toItemDto(saved);
    }

    @Override
    public ItemDto update(Long ownerId, Long itemId, ItemDto dto) {
        Item existing = itemStorage.getItem(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));

        if (!existing.getOwnerId().equals(ownerId)) {
            throw new NotFoundException("Вещь с id=" + itemId + " не принадлежит пользователю " + ownerId);
        }

        if (dto.getName() != null) {
            if (dto.getName().isBlank()) {
                throw new ValidationException("Имя вещи не может быть пустым");
            }
            existing.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            existing.setDescription(dto.getDescription());
        }
        if (dto.getAvailable() != null) {
            existing.setAvailable(dto.getAvailable());
        }

        Item saved = itemStorage.addItem(existing);
        return ItemMapper.toItemDto(saved);
    }

    @Override
    public ItemDto getById(Long userId, Long itemId) {
        Item item = itemStorage.getItem(itemId)
                .orElseThrow(() -> new RuntimeException("Такого Item нет"));
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getOwnerItems(Long ownerId) {
        return itemStorage.findByOwnerItem(ownerId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            // По ТЗ поиск по пустой строке должен возвращать пустой список
            return List.of();
        }
        String query = text.toLowerCase(Locale.ROOT);

        return itemStorage.allItems().stream()
                .filter(Item::getAvailable) // только доступные к аренде
                .filter(item ->
                        (item.getName() != null && item.getName().toLowerCase(Locale.ROOT).contains(query)) ||
                                (item.getDescription() != null && item.getDescription().toLowerCase(Locale.ROOT).contains(query))
                )
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        itemStorage.removeItem(id);
    }

    private void validateNewItem(ItemDto dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new ValidationException("Имя вещи не может быть пустым");
        }
        if (dto.getDescription() == null || dto.getDescription().isBlank()) {
            throw new ValidationException("Описание вещи не может быть пустым");
        }
        if (dto.getAvailable() == null) {
            throw new ValidationException("Статус доступности вещи не может быть null");
        }
    }
}
