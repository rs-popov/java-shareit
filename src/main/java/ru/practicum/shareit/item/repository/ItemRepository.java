package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    List<Item> getAllItems();

    Optional<Item> getItemById(long itemId);

    Item createItem(Item item);

    Item updateItem(Item item);

    void deleteItem(long itemId);
}