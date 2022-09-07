package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemInputDto;

import java.util.List;

public interface ItemService {
    List<ItemInputDto> getAllItemsByOwner(Long ownerId);

    ItemInputDto getItemById(Long itemId, Long userId);

    List<ItemInputDto> searchItems(String query);

    ItemInputDto createItem(ItemInputDto itemDto, Long ownerId);

    ItemInputDto updateItem(Long itemId, ItemInputDto itemDto, Long userId);

    void deleteItem(Long itemId);

    CommentDto createComment(Long userId, Long itemId, CommentDto commentDto);
}