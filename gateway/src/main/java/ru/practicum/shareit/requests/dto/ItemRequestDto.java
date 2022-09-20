package ru.practicum.shareit.requests.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.time.LocalDateTime;

@Data
@Builder
public class ItemRequestDto {
    @NonNull String description;
    private LocalDateTime created;
}