package ru.practicum.shareit.requests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.requests.dto.ItemRequestMapper;
import ru.practicum.shareit.requests.dto.ItemRequestOutputDto;
import ru.practicum.shareit.requests.model.ItemRequest;
import ru.practicum.shareit.requests.repository.ItemRequestRepository;
import ru.practicum.shareit.requests.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RequestsUnitTests {

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;

    private final User owner = User.builder()
            .id(1L)
            .name("UserName")
            .email("user@mail.ru").build();
    private final User requester = User.builder()
            .id(2L)
            .name("BookerName")
            .email("booker@mail.ru").build();
    private final Item item = Item.builder()
            .id(1L)
            .name("ItemName")
            .description("ItemDesc")
            .owner(owner)
            .available(true)
            .build();
    private final Item anotherItem = Item.builder()
            .id(2L)
            .name("ItemName2")
            .description("ItemDesc2")
            .owner(owner)
            .available(true)
            .build();
    private final ItemRequest itemRequest = ItemRequest.builder()
            .id(2L)
            .description("itemRequestDescription")
            .requestor(requester)
            .created(LocalDateTime.now())
            .build();
    private final ItemRequest anotherItemRequest = ItemRequest.builder()
            .id(3L)
            .description("anotherItemRequestDescription")
            .requestor(owner)
            .created(LocalDateTime.now())
            .build();

    @Test
    void createRequest() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(requester));
        when(itemRequestRepository.save(any())).thenReturn(itemRequest);
        ItemRequestDto createdItemRequest = itemRequestService.createRequest(requester.getId(),
                ItemRequestMapper.toItemRequestDto(itemRequest));
        assertNotEquals(createdItemRequest, null);
        assertEquals(createdItemRequest.getId(), itemRequest.getId());
        assertEquals(createdItemRequest.getDescription(), itemRequest.getDescription());
        verify(itemRequestRepository, times(1)).save(any());
    }

    @Test
    void createRequestWhenUnknownRequester_shouldThrowException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> itemRequestService.createRequest(requester.getId(),
                        ItemRequestMapper.toItemRequestDto(itemRequest)));
        assertEquals("Пользователь c id=" + 2 + " не найден.", exception.getMessage());
    }

    @Test
    void findRequestById() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(requester));
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.ofNullable(itemRequest));
        when(itemRepository.findItemByItemRequestId(anyLong())).thenReturn(List.of(item, anotherItem));
        ItemRequestOutputDto foundItemRequest = itemRequestService.findRequestById(1L, 1L);
        assertNotEquals(foundItemRequest, null);
        assertEquals(foundItemRequest.getDescription(), itemRequest.getDescription());
        verify(itemRequestRepository, times(1)).findById(anyLong());
    }

    @Test
    void findRequestByIdWithUserUnknown() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> itemRequestService.findRequestById(1L, 1L));
        assertEquals("Пользователь c id=" + 1 + " не найден.", exception.getMessage());
    }

    @Test
    void findRequestByIdWithRequestUnknown() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(requester));
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.empty());
        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> itemRequestService.findRequestById(1L, 1L));
        assertEquals("Запрос c id=" + 1 + " не найден.", exception.getMessage());
    }

    @Test
    void findAllRequest() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(requester));
        when(itemRequestRepository.findAll(PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"))))
                .thenReturn(new PageImpl<ItemRequest>(List.of(itemRequest, anotherItemRequest)));
        when(itemRepository.findItemByItemRequestId(anyLong())).thenReturn(List.of(item, anotherItem));
        List<ItemRequestOutputDto> foundItemRequest = itemRequestService.findAllRequest(2L, 0, 10);
        assertNotEquals(foundItemRequest, null);
        assertEquals(1, foundItemRequest.size());
        assertEquals(anotherItemRequest.getDescription(), foundItemRequest.get(0).getDescription());
        verify(itemRequestRepository, times(1)).findAll(PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id")));
    }

    @Test
    void findAllRequestWithUserUnknown() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> itemRequestService.findRequestById(1L, 1L));
        assertEquals("Пользователь c id=" + 1 + " не найден.", exception.getMessage());
    }
}