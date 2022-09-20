package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.time.LocalDateTime;

@Data
@Builder
public class InputBookingDto {
    private Long id;

    @NonNull
    private Long itemId;

    @NonNull
    private LocalDateTime start;

    @NonNull
    private LocalDateTime end;
}
