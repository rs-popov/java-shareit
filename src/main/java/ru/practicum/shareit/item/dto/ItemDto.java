package ru.practicum.shareit.item.dto;

import lombok.*;

/**
 *
 */

@Value
@Builder
public class ItemDto {
    private long id;
    private String name;
    private String description;
    private Boolean available;
}
