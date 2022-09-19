package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemInputDto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Slf4j
@Validated
@Controller
@RequiredArgsConstructor
@RequestMapping(path = "/items")
public class ItemController {
    private final ItemClient itemClient;
    private static final String USERID = "X-Sharer-User-Id";

    @GetMapping
    public ResponseEntity<Object> getAllItemsByOwner(@RequestHeader(USERID) Long ownerId,
                                                     @RequestParam(required = false, defaultValue = "0")
                                                     @PositiveOrZero Integer from,
                                                     @RequestParam(required = false, defaultValue = "20")
                                                     @Positive Integer size) {
        log.info("Get all items by owner, userId={}, from={}, size={}", ownerId, from, size);
        return itemClient.getAllItemsByOwner(ownerId, from, size);
    }

    @GetMapping("{itemId}")
    public ResponseEntity<Object> getItemById(@RequestHeader(USERID) Long userId,
                                              @PathVariable Long itemId) {
        log.info("Get item {}, userId={}", itemId, userId);
        return itemClient.getItemById(userId, itemId);
    }

    @GetMapping("search")
    public ResponseEntity<Object> searchItems(@RequestHeader(USERID) Long userId,
                                              @RequestParam String text,
                                              @RequestParam(required = false, defaultValue = "0")
                                              @PositiveOrZero Integer from,
                                              @RequestParam(required = false, defaultValue = "20")
                                              @Positive Integer size) {
        log.info("Search items by query={}, userId={}, from={}, size={}", text, userId, from, size);
        return itemClient.searchItems(userId, text, from, size);
    }

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestHeader(USERID) Long ownerId,
                                             @Valid @NotNull @RequestBody ItemInputDto itemDto) {
        log.info("Create item by userId={}", ownerId);
        return itemClient.createItem(ownerId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader(USERID) Long userId,
                                             @PathVariable Long itemId,
                                             @Valid @NotNull @RequestBody ItemInputDto itemDto) {
        log.info("Update item itemId={} by userId={}", itemId, userId);
        return itemClient.updateItem(userId, itemId, itemDto);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Object> deleteItem(@PathVariable Long itemId) {
        log.info("Delete item, itemId={}", itemId);
        return itemClient.deleteItem(itemId);
    }

    @PostMapping("{itemId}/comment")
    public ResponseEntity<Object> createComment(@RequestHeader(USERID) Long userId,
                                                @PathVariable Long itemId,
                                                @Valid @NotNull @RequestBody CommentDto commentDto) {
        log.info("Create comment, itemId={}, userId={}", itemId, userId);
        return itemClient.createComment(userId, itemId, commentDto);
    }
}