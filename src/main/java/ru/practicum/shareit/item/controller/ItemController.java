package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemInputDto;
import ru.practicum.shareit.item.dto.ItemOutputDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;
    private static final String USERID = "X-Sharer-User-Id";

    @GetMapping
    public List<ItemOutputDto> getAllItemsByOwner(@RequestHeader(USERID) Long ownerId,
                                                  @RequestParam(required = false, defaultValue = "0")
                                                  @PositiveOrZero Integer from,
                                                  @RequestParam(required = false, defaultValue = "20")
                                                  @PositiveOrZero Integer size) {
        return itemService.getAllItemsByOwner(ownerId, from, size);
    }

    @GetMapping("{itemId}")
    public ItemOutputDto getItemById(@RequestHeader(USERID) Long userId,
                                     @PathVariable Long itemId) {
        return itemService.getItemById(itemId, userId);
    }

    @GetMapping("search")
    public List<ItemInputDto> searchItems(@RequestParam String text,
                                          @RequestParam(required = false, defaultValue = "0")
                                          @PositiveOrZero Integer from,
                                          @RequestParam(required = false, defaultValue = "20")
                                          @PositiveOrZero Integer size) {
        return itemService.searchItems(text, from, size);
    }

    @PostMapping
    public ItemInputDto createItem(@RequestHeader(USERID) Long ownerId,
                                   @Valid @NotNull @RequestBody ItemInputDto itemDto) {
        return itemService.createItem(itemDto, ownerId);
    }

    @PatchMapping("/{itemId}")
    public ItemInputDto updateItem(@RequestHeader(USERID) Long userId,
                                   @PathVariable Long itemId,
                                   @Valid @NotNull @RequestBody ItemInputDto itemDto) {
        return itemService.updateItem(itemId, itemDto, userId);
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(@PathVariable Long itemId) {
        itemService.deleteItem(itemId);
    }

    @PostMapping("{itemId}/comment")
    public CommentDto createComment(@RequestHeader(USERID) Long userId,
                                    @PathVariable Long itemId,
                                    @Valid @NotNull @RequestBody CommentDto commentDto) {
        return itemService.createComment(userId, itemId, commentDto);
    }
}