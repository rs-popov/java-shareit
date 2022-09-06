package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Value;
import ru.practicum.shareit.booking.dto.BookingDtoForItem;
import ru.practicum.shareit.item.comment.dto.CommentDto;

import java.util.List;
import java.util.Optional;

@Value
@Builder
public class ItemOutputDto {
    Long id;
    String name;
    String description;
    Boolean available;
    Optional<BookingDtoForItem> lastBooking;
    Optional<BookingDtoForItem> nextBooking;
    List<CommentDto> comments;
}
