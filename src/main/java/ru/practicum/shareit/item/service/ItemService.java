package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemInputDto;
import ru.practicum.shareit.item.dto.ItemOutputDto;

import java.util.List;

public interface ItemService {
    List<ItemOutputDto> getAllItemsByOwner(Long ownerId);

    ItemOutputDto getItemById(Long itemId, Long userId);

    List<ItemOutputDto> searchItems(String query);

    ItemOutputDto createItem(ItemInputDto itemDto, Long ownerId);

    ItemOutputDto updateItem(Long itemId, ItemInputDto itemDto, Long userId);

    void deleteItem(Long itemId);

    CommentDto createComment(Long userId, Long itemId, CommentDto commentDto);
}