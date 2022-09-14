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
    Long id;
    @NonNull
    String description;
    LocalDateTime created;
    List<ItemInputDto> items;
}