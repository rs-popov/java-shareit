package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.dto.InputBookingDto;
import ru.practicum.shareit.exceptions.BadRequestException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Slf4j
@Validated
@Controller
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingClient bookingClient;
    private static final String USERID = "X-Sharer-User-Id";

    @GetMapping
    public ResponseEntity<Object> getAllByBookerId(@RequestHeader(USERID) Long bookerId,
                                                   @RequestParam(name = "state", defaultValue = "all") String state,
                                                   @PositiveOrZero
                                                   @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                   @Positive
                                                   @RequestParam(name = "size", defaultValue = "10") Integer size) {
        BookingState bookingState = BookingState.from(state)
                .orElseThrow(() -> new BadRequestException("Unknown state: " + state));
        log.info("Get booking with state {}, userId={}, from={}, size={}", state, bookerId, from, size);
        return bookingClient.getAllByBookerId(bookerId, bookingState, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getAllByOwnerId(@RequestHeader(USERID) Long ownerId,
                                                  @RequestParam(name = "state", defaultValue = "all") String state,
                                                  @PositiveOrZero
                                                  @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                  @Positive
                                                  @RequestParam(name = "size", defaultValue = "10") Integer size) {
        BookingState bookingState = BookingState.from(state)
                .orElseThrow(() -> new BadRequestException("Unknown state: " + state));
        log.info("Get booking by owner with state {}, userId={}, from={}, size={}", state, ownerId, from, size);
        return bookingClient.getAllByOwnerId(ownerId, bookingState, from, size);
    }

    @PostMapping
    public ResponseEntity<Object> addBooking(@RequestHeader(USERID) Long userId,
                                             @RequestBody @Valid @NotNull InputBookingDto inputBookingDto) {
        log.info("Creating booking {}, userId={}", inputBookingDto, userId);
        return bookingClient.addBooking(userId, inputBookingDto);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getById(@RequestHeader(USERID) Long userId,
                                          @PathVariable Long bookingId) {
        log.info("Get booking {}, userId={}", bookingId, userId);
        return bookingClient.getById(userId, bookingId);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approveBooking(@RequestHeader(USERID) Long ownerId,
                                                 @PathVariable Long bookingId,
                                                 @RequestParam Boolean approved) {
        log.info("Approve booking {}, userId={}", bookingId, ownerId);
        return bookingClient.approveBooking(ownerId, bookingId, approved);
    }
}