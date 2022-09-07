package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.StatusType;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.dto.CommentMapper;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.comment.repository.CommentRepository;
import ru.practicum.shareit.exceptions.BadRequestException;
import ru.practicum.shareit.exceptions.ForbiddenAccessException;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.item.dto.ItemInputDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.dto.ItemOutputDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public List<ItemOutputDto> getAllItemsByOwner(Long ownerId) {
        return itemRepository.findAll().stream()
                .filter(item -> Objects.equals(item.getOwner().getId(), ownerId))
                .map(item -> convertToItemOutputDto(item, ownerId))
                .collect(Collectors.toList());
    }

    @Override
    public ItemOutputDto getItemById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ObjectNotFoundException("Предмет c id=" + itemId + " не найден."));
        return convertToItemOutputDto(item, userId);
    }

    @Override
    public ItemInputDto createItem(ItemInputDto itemDto, Long ownerId) {
        validate(itemDto);
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ObjectNotFoundException("Пользователь c id=" + ownerId + " не найден."));
        Item item = ItemMapper.fromItemDto(itemDto, owner);
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemInputDto updateItem(Long itemId, ItemInputDto itemDto, Long userId) {
        Item itemUpd = itemRepository.findById(itemId)
                .orElseThrow(() -> new ObjectNotFoundException("Предмет c id=" + itemId + " не найден."));
        if (!Objects.equals(userId, itemUpd.getOwner().getId())) {
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
        return ItemMapper.toItemDto(itemRepository.save(itemUpd));
    }

    @Override
    public void deleteItem(Long itemId) {
        itemRepository.deleteById(itemId);
    }

    @Override
    public List<ItemInputDto> searchItems(String query) {
        if (query.isEmpty() || query.isBlank()) {
            return Collections.emptyList();
        } else {
            return itemRepository.search(query).stream()
                    .filter(Item::getAvailable)
                    .map(ItemMapper::toItemDto)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public CommentDto createComment(Long userId, Long itemId, CommentDto commentDto) {
        Booking booking = bookingRepository.getAllByBookerId(userId).stream()
                .filter(b -> b.getItem().getId().equals(itemId))
                .filter(b -> b.getEnd().isBefore(LocalDateTime.now()))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Пользователь c id=" + userId +
                        " не имеет бронирований, к которым можно добавить комментарий."));
        if (booking.getStatus().equals(StatusType.APPROVED)) {
            User author = userRepository.findById(userId)
                    .orElseThrow(() -> new ObjectNotFoundException("Пользователь c id=" + userId + " не найден."));
            Item item = itemRepository.findById(itemId)
                    .orElseThrow(() -> new ObjectNotFoundException("Предмет c id=" + itemId + " не найден."));
            Comment comment = CommentMapper.fromCommentDto(commentDto, author, item);
            return CommentMapper.toCommentDto(commentRepository.save(comment));
        } else {
            throw new BadRequestException("Статус бронирования - " + booking.getStatus().toString() +
                    ". Добавление комментария не разрешено.");
        }
    }

    private void validate(ItemInputDto itemDto) {
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

    private ItemOutputDto convertToItemOutputDto(Item item, Long ownerId) {
        //Booking lastBooking = bookingRepository.findLastBooking(item.getId(), ownerId).orElse(null);
        //Booking nextBooking = bookingRepository.findNextBooking(item.getId(), ownerId).orElse(null);
//        List<CommentDto> comments = commentRepository.findAllByItemId(item.getId()).stream()
//                .map(CommentMapper::toCommentDto)
//                .collect(Collectors.toList());
        ItemOutputDto itemOutputDto = ItemMapper.toItemOutputDto(item);
        //itemOutputDto.setLastBooking(BookingMapper.toBookingDtoForItem(lastBooking));
        //itemOutputDto.setNextBooking(BookingMapper.toBookingDtoForItem(nextBooking));
        //itemOutputDto.setComments(comments);
        return itemOutputDto;
    }
}