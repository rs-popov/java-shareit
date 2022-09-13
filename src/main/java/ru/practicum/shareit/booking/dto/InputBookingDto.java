package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.Future;
import java.time.LocalDateTime;

@Data
@Builder
public class InputBookingDto {
    private Long id;

    @NonNull
    private Long itemId;

    @NonNull
    @Future
    private LocalDateTime start;

    @NonNull
    @Future
    private LocalDateTime end;
}
