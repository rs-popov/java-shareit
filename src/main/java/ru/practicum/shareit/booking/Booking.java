package ru.practicum.shareit.booking;

import lombok.Data;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * // TODO .
 *
 */

@Data
public class Booking {
    long id;

    LocalDateTime start;
    LocalDateTime end;
    Item item;
    User booker;
    StatusType status;

}
