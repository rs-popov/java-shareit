package ru.practicum.shareit.item.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepository {
    private final Map<Long, Item> items;
    private static Integer globalId = 1;

    private static Integer getNextId() {
        return globalId++;
    }

    @Override
    public List<Item> getAllItems() {
        return new ArrayList<>(items.values());
    }

    @Override
    public Optional<Item> getItemById(long itemId) {
        if (items.containsKey(itemId)) {
            return Optional.ofNullable(items.get(itemId));
        } else {
            log.warn("Предмет с id={}  не найден.", itemId);
            throw new ObjectNotFoundException("Предмет не найден.");
        }
    }

    @Override
    public Item createItem(Item item) {
        item.setId(getNextId());
        items.put(item.getId(), item);
        log.info("Добавлен новый предмет {}.", item.getName());
        return item;
    }

    @Override
    public Item updateItem(Item item) {
        items.put(item.getId(), item);
        log.info("Изменен предмет {}.", item.getName());
        return item;
    }

    @Override
    public void deleteItem(long itemId) {
        if (items.containsKey(itemId)) {
            items.remove(itemId);
            log.info("Удален предмет с id {}.", itemId);
        } else {
            throw new ObjectNotFoundException("Предмет не найден.");
        }
    }
}