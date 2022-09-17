package ru.practicum.shareit.requests.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.item.dto.ItemInputDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.requests.dto.ItemRequestMapper;
import ru.practicum.shareit.requests.repository.ItemRequestRepository;
import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.requests.dto.ItemRequestOutputDto;
import ru.practicum.shareit.requests.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public ItemRequestDto createRequest(Long userId, ItemRequestDto itemRequestDto) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("Пользователь c id=" + userId + " не найден."));
        ItemRequest itemRequest = ItemRequestMapper.fromItemRequestDto(itemRequestDto, requester);
        ItemRequestDto itemRequestCreated = ItemRequestMapper.toItemRequestDto(itemRequestRepository.save(itemRequest));
        log.info("Создан запрос с id={}", itemRequestCreated.getId());
        return itemRequestCreated;
    }

    @Override
    public ItemRequestOutputDto findRequestById(Long requestId, Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("Пользователь c id=" + userId + " не найден."));
        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new ObjectNotFoundException("Запрос c id=" + requestId + " не найден."));
        List<ItemInputDto> items = getItems(requestId);
        return ItemRequestMapper.itemRequestOutputDto(itemRequest, items);
    }

    @Override
    public List<ItemRequestOutputDto> findAllRequestFromRequester(Long requesterId) {
        userRepository.findById(requesterId)
                .orElseThrow(() -> new ObjectNotFoundException("Пользователь c id=" + requesterId + " не найден."));
        return itemRequestRepository.findItemRequestsByRequestorId(requesterId).stream()
                .map(request -> ItemRequestMapper.itemRequestOutputDto(request, getItems(request.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestOutputDto> findAllRequest(Long userId, Integer from, Integer size) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("Пользователь c id=" + userId + " не найден."));
        return itemRequestRepository.findAll(getPageRequest(from, size)).stream()
                .filter(itemRequest -> !Objects.equals(itemRequest.getRequestor().getId(), requester.getId()))
                .map(request -> ItemRequestMapper.itemRequestOutputDto(request, getItems(request.getId())))
                .collect(Collectors.toList());
    }

    private List<ItemInputDto> getItems(Long requestId) {
        return itemRepository.findItemByItemRequestId(requestId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private PageRequest getPageRequest(Integer from, Integer size) {
        int page = from < size ? 0 : from / size;
        return PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
    }
}