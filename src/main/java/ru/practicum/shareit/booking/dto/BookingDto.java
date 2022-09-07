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
    private Long id;

    @Future
    private LocalDateTime start;

    @Future
    private LocalDateTime end;

    private ItemInputDto item;
    private UserDto booker;
    private StatusType status;
}