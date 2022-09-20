package ru.practicum.shareit.requests.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import ru.practicum.shareit.item.dto.ItemInputDto;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ItemRequestOutputDto {
    private Long id;
    @NonNull
    private String description;
    private LocalDateTime created;
    private List<ItemInputDto> items;
}