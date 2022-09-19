package ru.practicum.shareit.requests.service;

import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.requests.dto.ItemRequestOutputDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto createRequest(Long userId, ItemRequestDto itemRequestDto);

    ItemRequestOutputDto findRequestById(Long requestId, Long userId);

    List<ItemRequestOutputDto> findAllRequestFromRequester(Long requesterId);

    List<ItemRequestOutputDto> findAllRequest(Long userId, Integer from, Integer size);
}