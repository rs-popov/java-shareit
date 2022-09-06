package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public class ItemMapper {
    public static ItemInputDto toItemDto(Item item) {
        return ItemInputDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();
    }

    public static Item fromItemDto(ItemInputDto itemDto, User owner) {
        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .owner(owner)
                .build();
    }

    public static ItemOutputDto toItemOutputDto(Item item, Booking lastBooking, Booking nextBooking, List<CommentDto> comments) {
        return ItemOutputDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .lastBooking(Optional.ofNullable(BookingMapper.toBookingDtoForItem(lastBooking)))
                .nextBooking(Optional.ofNullable(BookingMapper.toBookingDtoForItem(nextBooking)))
                .comments(comments)
                .build();
    }
}