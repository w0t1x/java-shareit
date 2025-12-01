package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ItemStorage {
    Item addItem(Item item);

    Optional<Item> getItem(Long id);

    Collection<Item> allItems();

    List<Item> findByOwnerItem(Long ownerId);

    Item updateItem(Item item);

    void removeItem(Long id);
}
