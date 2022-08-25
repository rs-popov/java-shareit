package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import ru.practicum.shareit.exceptions.BadRequestException;
import ru.practicum.shareit.exceptions.ForbiddenAccessException;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public List<ItemDto> getAllItemsByOwner(long ownerId) {
        return itemRepository.getAllItems().stream()
                .filter(item -> item.getOwner().getId() == ownerId)
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto getItemById(long itemId) {
        Item item = itemRepository.getItemById(itemId)
                .orElseThrow(() -> new ObjectNotFoundException("Предмет не найден."));
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto createItem(ItemDto itemDto, long ownerId) {
        validate(itemDto);
        User owner = userRepository.getUserById(ownerId)
                .orElseThrow(() -> new ObjectNotFoundException("Пользователь не найден."));
        Item item = ItemMapper.fromItemDto(itemDto, owner);
        return ItemMapper.toItemDto(itemRepository.createItem(item));
    }

    @Override
    public ItemDto updateItem(long itemId, ItemDto itemDto, long userId) {
        Item itemUpd = itemRepository.getItemById(itemId)
                .orElseThrow(() -> new ObjectNotFoundException("Предмет не найден."));
        if (userId != itemUpd.getOwner().getId()) {
            log.warn("Редактировать вещь может только её владелец. Владелец вещи - id={}", itemUpd.getOwner().getId());
            throw new ForbiddenAccessException("Редактировать вещь может только её владелец.");
        }
        if (itemDto.getName() != null) {
            itemUpd.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            itemUpd.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            itemUpd.setAvailable(itemDto.getAvailable());
        }
        return ItemMapper.toItemDto(itemRepository.updateItem(itemUpd));
    }

    @Override
    public void deleteItem(long itemId) {
        itemRepository.deleteItem(itemId);
    }

    @Override
    public List<ItemDto> searchItems(String query) {
        if (query.isEmpty() || query.isBlank()) {
            return Collections.emptyList();
        } else {
            return itemRepository.getAllItems().stream()
                    .filter(item -> searchInNameAndDesc(item, query))
                    .map(ItemMapper::toItemDto)
                    .collect(Collectors.toList());
        }
    }

    private void validate(ItemDto itemDto) {
        if (itemDto.getName() == null || itemDto.getName().isEmpty()) {
            throw new BadRequestException("Отсутствует краткое название.");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isEmpty()) {
            throw new BadRequestException("Отсутствует развёрнутое описание.");
        }
        if (itemDto.getAvailable() == null) {
            throw new BadRequestException("Отсутствует статус.");
        }
    }

    private boolean searchInNameAndDesc(Item item, String query) {
        return (item.getDescription().toLowerCase(Locale.ROOT).contains(query.toLowerCase()) ||
                item.getName().toLowerCase(Locale.ROOT).contains(query.toLowerCase())) &&
                item.getAvailable().equals(true);
    }
}