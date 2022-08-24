package ru.practicum.shareit.item.dto;

import lombok.*;

/**
 *
 */

@Value
@Builder
public class ItemDto {
    long id;
    String name;
    String description;
    Boolean available;
}
