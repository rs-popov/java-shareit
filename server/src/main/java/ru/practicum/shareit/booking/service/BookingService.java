package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.InputBookingDto;

import java.util.List;

public interface BookingService {
    BookingDto addBooking(Long bookerId, InputBookingDto inputBookingDto);

    BookingDto approveBooking(Long ownerId, Long bookingId, Boolean isApprove);

    BookingDto getById(Long userId, Long bookingId);

    List<BookingDto> getAllByBookerId(Long bookerId, String state, Integer from, Integer size);

    List<BookingDto> getAllByOwnerId(Long ownerId, String state, Integer from, Integer size);
}