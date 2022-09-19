package ru.practicum.shareit.item.dto;

import lombok.*;

@Data
@Builder
public class ItemInputDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private Long requestId;
}
