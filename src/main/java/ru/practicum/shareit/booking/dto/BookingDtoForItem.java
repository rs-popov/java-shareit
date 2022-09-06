package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class BookingDtoForItem {
    Long id;
    LocalDateTime start;
    LocalDateTime end;
    Long bookerId;
}
