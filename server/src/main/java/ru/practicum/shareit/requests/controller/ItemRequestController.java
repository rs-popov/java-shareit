package ru.practicum.shareit.requests.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.requests.service.ItemRequestService;
import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.requests.dto.ItemRequestOutputDto;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestService itemRequestService;
    private static final String USERID = "X-Sharer-User-Id";

    @PostMapping
    public ItemRequestDto createRequest(@RequestHeader(USERID) Long requesterId,
                                        @RequestBody ItemRequestDto itemRequestDto) {
        return itemRequestService.createRequest(requesterId, itemRequestDto);
    }

    @GetMapping("/{requestId}")
    public ItemRequestOutputDto getRequestById(@RequestHeader(USERID) Long userId,
                                               @PathVariable Long requestId) {
        return itemRequestService.findRequestById(requestId, userId);
    }

    @GetMapping
    public List<ItemRequestOutputDto> getAllRequestsFromRequester(@RequestHeader(USERID) Long requesterId) {
        return itemRequestService.findAllRequestFromRequester(requesterId);
    }

    @GetMapping("/all")
    public List<ItemRequestOutputDto> getAllRequests(@RequestHeader(USERID) Long requesterId,
                                                     @RequestParam(required = false, defaultValue = "0") Integer from,
                                                     @RequestParam(required = false, defaultValue = "20") Integer size) {
        return itemRequestService.findAllRequest(requesterId, from, size);
    }
}