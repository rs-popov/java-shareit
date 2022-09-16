package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.StatusType;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.BadRequestException;
import ru.practicum.shareit.exceptions.ForbiddenAccessException;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.dto.CommentMapper;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.comment.repository.CommentRepository;
import ru.practicum.shareit.item.dto.ItemInputDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.dto.ItemOutputDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemUnitTests {
    @InjectMocks
    private ItemServiceImpl itemService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;

    private final User owner = User.builder()
            .id(1L)
            .name("UserName")
            .email("user@mail.ru").build();
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

    @Test
    void createItem() {
        ItemInputDto inputDto = ItemMapper.toItemDto(item);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.save(any())).thenReturn(item);
        ItemInputDto createdItem = itemService.createItem(inputDto, anyLong());
        assertEquals(createdItem, inputDto);
        verify(itemRepository, times(1)).save(any());
    }

    @Test
    void createItemWithOwnerUnknown_shouldThrowException() {
        ItemInputDto inputDto = ItemMapper.toItemDto(item);
        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> itemService.createItem(inputDto, 11L));
        assertEquals("Пользователь c id=11 не найден.", exception.getMessage());
    }

    @Test
    void createItemWithoutAvailable_shouldThrowException() {
        ItemInputDto inputDto = ItemInputDto.builder()
                .id(1L)
                .name("ItemName")
                .description("ItemDesc")
                .build();
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> itemService.createItem(inputDto, 1L));
        assertEquals("Отсутствует статус.", exception.getMessage());
    }

    @Test
    void createItemWithoutName_shouldThrowException() {
        ItemInputDto inputDto = ItemInputDto.builder()
                .id(1L)
                .description("ItemDesc")
                .available(true)
                .build();
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> itemService.createItem(inputDto, 1L));
        assertEquals("Отсутствует краткое название.", exception.getMessage());
    }

    @Test
    void createItemWithoutDescription_shouldThrowException() {
        ItemInputDto inputDto = ItemInputDto.builder()
                .id(1L)
                .name("name")
                .available(true)
                .build();
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> itemService.createItem(inputDto, 1L));
        assertEquals("Отсутствует развёрнутое описание.", exception.getMessage());
    }

    @Test
    void updateItemName() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any())).thenReturn(Item.builder()
                .id(1L)
                .name("ItemNameUpdate")
                .description("ItemDesc")
                .owner(owner)
                .available(true)
                .build());

        ItemInputDto itemUpdated = itemService.updateItem(1L, ItemInputDto.builder()
                .name("ItemNameUpdate").build(), 1L);
        assertEquals(itemUpdated.getName(), "ItemNameUpdate");
        assertEquals(itemUpdated.getDescription(), "ItemDesc");
        verify(itemRepository, times(1)).save(any());
    }

    @Test
    void updateItemDescription() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any())).thenReturn(Item.builder()
                .id(1L)
                .name("ItemName")
                .description("ItemDescUpdate")
                .owner(owner)
                .available(true)
                .build());
        ItemInputDto itemUpdated = itemService.updateItem(1L, ItemInputDto.builder()
                .description("ItemDescUpdate").build(), 1L);
        assertEquals(itemUpdated.getName(), "ItemName");
        assertEquals(itemUpdated.getDescription(), "ItemDescUpdate");
        verify(itemRepository, times(1)).save(any());
    }

    @Test
    void updateItemAvailable() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any())).thenReturn(Item.builder()
                .id(1L)
                .name("ItemName")
                .description("ItemDesc")
                .owner(owner)
                .available(false)
                .build());
        ItemInputDto itemUpdated = itemService.updateItem(1L, ItemInputDto.builder()
                .available(false).build(), 1L);
        assertEquals(itemUpdated.getName(), "ItemName");
        assertEquals(itemUpdated.getDescription(), "ItemDesc");
        verify(itemRepository, times(1)).save(any());
    }

    @Test
    void updateItemWithByNonOwner_shouldThrowException() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        ForbiddenAccessException exception = assertThrows(ForbiddenAccessException.class,
                () -> itemService.updateItem(1L, ItemInputDto.builder()
                        .name("ItemNameUpdate").build(), 11L));
        assertEquals("Редактировать вещь может только её владелец.", exception.getMessage());
    }

    @Test
    void deleteItem() {
        itemService.deleteItem(1L);
        verify(itemRepository, times(1)).deleteById(anyLong());
    }

    @Test
    void searchItems() {
        when(itemRepository.search(anyString(), any()))
                .thenReturn(new PageImpl<Item>(List.of(item, anotherItem)));
        List<ItemInputDto> searchResult = itemService.searchItems("Item", 0, 2);
        assertEquals(2, searchResult.size());
    }

    @Test
    void searchItemsWithBlankQuery() {
        List<ItemInputDto> searchResult = itemService.searchItems("", 0, 2);
        assertEquals(searchResult.size(), 0);
    }

    @Test
    void getItemById() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.findLastBooking(anyLong(), anyLong())).thenReturn(Optional.empty());
        when(bookingRepository.findNextBooking(anyLong(), anyLong())).thenReturn(Optional.empty());
        when(commentRepository.findAllByItemId(anyLong())).thenReturn(Collections.emptyList());

        ItemOutputDto itemOutputDto = itemService.getItemById(1L, 1L);
        assertEquals(itemOutputDto.getName(), item.getName());
        assertEquals(itemOutputDto.getDescription(), item.getDescription());
        assertEquals(itemOutputDto.getComments().size(), 0);
    }

    @Test
    void getItemWithBookingsById() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.findLastBooking(anyLong(), anyLong())).thenReturn(Optional.ofNullable(Booking.builder().booker(owner).build()));
        when(bookingRepository.findNextBooking(anyLong(), anyLong())).thenReturn(Optional.ofNullable(Booking.builder().booker(owner).build()));
        when(commentRepository.findAllByItemId(anyLong())).thenReturn(Collections.emptyList());
        ItemOutputDto itemOutputDto = itemService.getItemById(1L, 1L);
        assertEquals(itemOutputDto.getName(), item.getName());
        assertEquals(itemOutputDto.getDescription(), item.getDescription());
        assertEquals(itemOutputDto.getComments().size(), 0);
    }

    @Test
    void getItemByIdWithUnknownId_shouldThrowException() {
        when(itemRepository.findById(anyLong())).thenThrow(ObjectNotFoundException.class);
        assertThrows(ObjectNotFoundException.class,
                () -> itemService.getItemById(11L, anyLong()));
    }

    @Test
    void getAllItemsByOwner() {
        when(itemRepository.findAll(PageRequest.of(0, 2)))
                .thenReturn(new PageImpl<Item>(List.of(item, anotherItem)));
        List<ItemOutputDto> allItems = itemService.getAllItemsByOwner(1L, 0, 2);
        assertEquals(2, allItems.size());
    }

    @Test
    void createComment() {
        LocalDateTime dateTime = LocalDateTime.now();
        User booker = User.builder()
                .id(2L)
                .name("BookerName")
                .email("emailBooker@mail.ru")
                .build();
        Booking booking = Booking.builder()
                .id(1L)
                .start(dateTime.minusDays(1))
                .end(dateTime.minusHours(1))
                .item(item)
                .booker(booker)
                .status(StatusType.APPROVED)
                .build();
        Comment comment = Comment.builder()
                .id(1L)
                .text("this is comment")
                .item(item)
                .author(booker)
                .created(dateTime.plusDays(1))
                .build();
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerId(anyLong())).thenReturn(List.of(booking));
        when(commentRepository.save(any())).thenReturn(comment);
        CommentDto commentCreated = itemService.createComment(2L, 1L, CommentMapper.toCommentDto(comment));
        assertEquals(1L, commentCreated.getId());
        assertEquals(booker.getName(), commentCreated.getAuthorName());
        assertEquals("this is comment", commentCreated.getText());
        verify(commentRepository, times(1)).save(any());
    }

    @Test
    void createCommentWithWrongStatus() {
        User booker = User.builder()
                .id(2L)
                .name("BookerName")
                .email("emailBooker@mail.ru")
                .build();
        Booking booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now().minusHours(1))
                .item(item)
                .booker(booker)
                .status(StatusType.REJECTED)
                .build();
        Comment comment = Comment.builder()
                .id(1L)
                .text("this is comment")
                .item(item)
                .author(booker)
                .build();
        when(bookingRepository.findAllByBookerId(anyLong())).thenReturn(List.of(booking));
        assertThrows(BadRequestException.class,
                () -> itemService.createComment(2L, 1L, CommentMapper.toCommentDto(comment)));
    }
}