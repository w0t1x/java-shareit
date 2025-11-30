package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ItemStorageImpl implements ItemStorage {
    private final Map<Long, Item> storage = new HashMap<>();
    private Long idGenerator = 0L;

    @Override
    public Item addItem(Item item) {
        if (item.getId() == null) {
            item.setId(++idGenerator);
        }
        storage.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<Item> getItem(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Collection<Item> AllItems() {
        return storage.values();
    }

    @Override
    public List<Item> findByOwnerItem(Long ownerId) {
        return storage.values().stream()
                .filter(item -> Objects.equals(item.getOwnerId(), ownerId))
                .collect(Collectors.toList());
    }

    @Override
    public Item updateItem(Item item) {
        storage.put(item.getId(), item);
        return storage.get(item.getId());
    }

    @Override
    public void removeItem(Long id) {
        storage.remove(id);
    }
}
