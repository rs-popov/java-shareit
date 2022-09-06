package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import ru.practicum.shareit.booking.model.StatusType;

import javax.validation.constraints.Future;
import java.time.LocalDateTime;

@Value
@Builder
public class InputBookingDto {
    Long id;

    @NonNull
    Long itemId;

    @NonNull
    @Future
    LocalDateTime start;

    @NonNull
    @Future
    LocalDateTime end;

    StatusType statusType;
}
