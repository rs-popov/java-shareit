package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.InputBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.model.StatusType;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.BadRequestException;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public BookingDto addBooking(Long bookerId, InputBookingDto inputBookingDto) {
        User booker = userRepository.findById(bookerId)
                .orElseThrow(() -> new ObjectNotFoundException("Пользователь c id=" + bookerId + " не найден."));
        Item item = itemRepository.findById(inputBookingDto.getItemId())
                .orElseThrow(() -> new ObjectNotFoundException("Предмет c id=" + inputBookingDto.getItemId() + " не найден."));
        if (booker.getId().equals(item.getOwner().getId())) {
            log.warn("Пользователь c id={} является владельцем предмета.", bookerId);
            throw new ObjectNotFoundException("Пользователь c id=" + bookerId + " является владельцем предмета.");
        }
        Booking booking = BookingMapper.fromInputBookingDto(inputBookingDto, item, booker);
        if (booking.getItem().getAvailable() && booking.getStart().isBefore(booking.getEnd())) {
            BookingDto bookingDto = BookingMapper.toBookingDto(bookingRepository.save(booking));
            log.info("Добавлено бронирование с id={}", bookingDto.getId());
            return bookingDto;
        } else {
            log.warn("Предмет не доступен для бронирования или указаны неверные параметры бронирования.");
            throw new BadRequestException("Неверные параметры бронирования.");
        }
    }

    @Override
    public BookingDto approveBooking(Long ownerId, Long bookingId, Boolean isApprove) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ObjectNotFoundException("Бронирование c id=" + bookingId + "не найдено."));
        if (!Objects.equals(booking.getItem().getOwner().getId(), ownerId)) {
            throw new ObjectNotFoundException("Пользователь c id=" + ownerId + " не является хозяином предмета.");
        }
        if (booking.getStatus().equals(StatusType.WAITING)) {
            booking.setStatus(isApprove ? StatusType.APPROVED : StatusType.REJECTED);
        } else {
            log.warn("Статус бронирования id={} отличается от WAITING", booking.getId());
            throw new BadRequestException("Бронирование не нуждается в изменении статуса.");
        }
        BookingDto bookingDto = BookingMapper.toBookingDto(bookingRepository.save(booking));
        log.info("Изменено бронирование с id={}", bookingDto.getId());
        return bookingDto;
    }

    @Override
    public BookingDto getById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ObjectNotFoundException("Бронирование c id=" + bookingId + "не найдено."));
        if (booking.getBooker().getId().equals(userId) || booking.getItem().getOwner().getId().equals(userId)) {
            return BookingMapper.toBookingDto(booking);
        } else {
            log.warn("Пользователь c id=" + userId + " не имеет отношение к предмету.");
            throw new ObjectNotFoundException("Пользователь c id=" + userId + " не имеет отношение к предмету.");
        }
    }

    @Override
    public List<BookingDto> getAllByBookerId(Long bookerId, String stateString, Integer from, Integer size) {
        int page = from < size ? 0 : from / size;
        Page<Booking> result = bookingRepository.findAllByBookerId(bookerId, PageRequest.of(page, size));
        if (result.isEmpty()) {
            log.info("Пользователь id={} не имеет бронирований", bookerId);
            throw new ObjectNotFoundException("Бронирований не найдено.");
        } else {
            return filterByState(result.toList(), toState(stateString)).stream()
                    .map(BookingMapper::toBookingDto)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<BookingDto> getAllByOwnerId(Long ownerId, String stateString, Integer from, Integer size) {
        int page = from < size ? 0 : from / size;
        Page<Booking> result = bookingRepository.getAllByOwnerId(ownerId, PageRequest.of(page, size));
        if (result.isEmpty()) {
            log.info("Пользователь id={} не имеет бронирований", ownerId);
            throw new ObjectNotFoundException("Бронирований не найдено.");
        } else {
            return filterByState(result.toList(), toState(stateString)).stream()
                    .map(BookingMapper::toBookingDto)
                    .collect(Collectors.toList());
        }
    }

    private State toState(String stateString) {
        try {
            return State.valueOf(stateString.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unknown state: UNSUPPORTED_STATUS");
        }
    }

    private List<Booking> filterByState(List<Booking> bookings, State state) {
        switch (state) {
            case ALL:
                return bookings;
            case WAITING:
                return bookings.stream()
                        .filter(booking -> booking.getStatus().equals(StatusType.WAITING))
                        .collect(Collectors.toList());
            case REJECTED:
                return bookings.stream()
                        .filter(booking -> booking.getStatus().equals(StatusType.REJECTED))
                        .collect(Collectors.toList());
            case PAST:
                return bookings.stream()
                        .filter(booking -> booking.getEnd().isBefore(LocalDateTime.now()))
                        .collect(Collectors.toList());
            case FUTURE:
                return bookings.stream()
                        .filter(booking -> booking.getStart().isAfter(LocalDateTime.now()))
                        .collect(Collectors.toList());
            case CURRENT:
                return bookings.stream()
                        .filter(booking -> booking.getStart().isBefore(LocalDateTime.now()) &&
                                booking.getEnd().isAfter(LocalDateTime.now()))
                        .collect(Collectors.toList());
            default:
                throw new BadRequestException("Unknown state: UNSUPPORTED_STATUS");
        }
    }
}
