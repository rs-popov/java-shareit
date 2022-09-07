package ru.practicum.shareit.item.dto;

import lombok.*;

@Value
@Builder
public class ItemInputDto {
    Long id;
    String name;
    String description;
    Boolean available;
}
