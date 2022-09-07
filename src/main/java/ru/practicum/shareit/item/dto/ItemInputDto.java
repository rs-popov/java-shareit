package ru.practicum.shareit.item.dto;

import lombok.*;

@Data
@Builder
public class ItemInputDto {
    Long id;
    String name;
    String description;
    Boolean available;
}
