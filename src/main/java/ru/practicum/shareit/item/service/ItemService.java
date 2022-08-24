package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    List<ItemDto> getAllItemsByOwner(long ownerId);

    ItemDto getItemById(long itemId);

    List<ItemDto> searchItems(String query);

    ItemDto createItem(ItemDto itemDto, long ownerId);

    ItemDto updateItem(long itemId, ItemDto itemDto, long userId);

    void deleteItem(long itemId);
}