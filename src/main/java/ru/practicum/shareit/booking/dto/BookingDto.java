package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.model.StatusType;
import ru.practicum.shareit.item.dto.ItemInputDto;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.constraints.Future;
import java.time.LocalDateTime;

@Data
@Builder
public class BookingDto {
    Long id;
    @Future
    LocalDateTime start;
    @Future
    LocalDateTime end;
    ItemInputDto item;
    UserDto booker;
    StatusType status;
}