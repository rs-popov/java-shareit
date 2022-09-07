package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BookingDtoForItem {
    Long id;
    Long bookerId;
}
