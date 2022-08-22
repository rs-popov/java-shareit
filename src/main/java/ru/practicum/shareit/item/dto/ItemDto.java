package ru.practicum.shareit.item.dto;

import lombok.*;
import lombok.experimental.NonFinal;
import ru.practicum.shareit.booking.StatusType;
import ru.practicum.shareit.requests.ItemRequest;
import ru.practicum.shareit.user.User;

/**
 * // TODO .
 */

@Value
public class ItemDto {
//    @NonFinal
//    long id;

    String name;

    String description;

    boolean available;

    //User owner;

    long requestId;

}
