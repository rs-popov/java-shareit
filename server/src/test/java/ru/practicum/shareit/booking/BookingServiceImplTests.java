package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.InputBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.StatusType;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exceptions.BadRequestException;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceImplTests {
    @InjectMocks
    private BookingServiceImpl bookingService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private BookingRepository bookingRepository;

    private final LocalDateTime date = LocalDateTime.now();
    private final User owner = User.builder()
            .id(1L)
            .name("UserName")
            .email("user@mail.ru").build();
    private final User booker = User.builder()
            .id(2L)
            .name("BookerName")
            .email("booker@mail.ru").build();
    private final Item item = Item.builder()
            .id(1L)
            .name("ItemName")
            .description("ItemDesc")
            .owner(owner)
            .available(true)
            .build();
    private final Item anotherItem = Item.builder()
            .id(2L)
            .name("ItemName2")
            .description("ItemDesc2")
            .owner(owner)
            .available(false)
            .build();
    private final Booking booking = Booking.builder()
            .id(1L)
            .start(date.minusDays(1))
            .end(date.minusHours(1))
            .item(item)
            .booker(booker)
            .status(StatusType.APPROVED)
            .build();
    private final InputBookingDto bookingInputDto = InputBookingDto.builder()
            .id(booking.getId())
            .start(booking.getStart())
            .end(booking.getEnd())
            .itemId(booking.getItem().getId())
            .build();
    private final Booking notApproveBooking = Booking.builder()
            .id(2L)
            .start(date.minusDays(1))
            .end(date.minusHours(1))
            .item(item)
            .booker(booker)
            .status(StatusType.WAITING)
            .build();

    @Test
    void addBooking() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.save(any())).thenReturn(booking);

        BookingDto bookingCreated = bookingService.addBooking(2L, bookingInputDto);
        assertNotEquals(bookingCreated, null);
        assertEquals(booking.getId(), bookingCreated.getId());
        assertEquals(booking.getItem().getId(), bookingCreated.getItem().getId());
        assertEquals(booking.getStart(), bookingCreated.getStart());
        assertEquals(booking.getEnd(), bookingCreated.getEnd());

        verify(bookingRepository, times(1)).save(any());
    }

    @Test
    void addBookingWhenItemUnknownShouldThrowException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> bookingService.addBooking(2L, bookingInputDto));
        assertEquals("Предмет c id=" + bookingInputDto.getItemId() + " не найден.", exception.getMessage());
    }

    @Test
    void addBookingWhenBookerUnknownShouldThrowException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> bookingService.addBooking(2L, bookingInputDto));
        assertEquals("Пользователь c id=" + 2 + " не найден.", exception.getMessage());
    }

    @Test
    void addBookingWhenBookerIsOwnerShouldThrowException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> bookingService.addBooking(1L, bookingInputDto));
        assertEquals("Пользователь c id=" + 1 + " является владельцем предмета.", exception.getMessage());
    }

    @Test
    void addBookingWhenItemNotAvailableShouldThrowException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(anotherItem));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> bookingService.addBooking(1L, bookingInputDto));
        assertEquals("Неверные параметры бронирования.", exception.getMessage());
    }

    @Test
    void approveBooking() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(notApproveBooking));
        when(bookingRepository.save(any())).thenReturn(notApproveBooking);

        BookingDto bookingApproved = bookingService.approveBooking(1L, 2L, true);
        assertEquals(notApproveBooking.getId(), bookingApproved.getId());
        assertEquals(notApproveBooking.getItem().getId(), bookingApproved.getItem().getId());
        assertEquals(notApproveBooking.getStart(), bookingApproved.getStart());
        assertEquals(notApproveBooking.getEnd(), bookingApproved.getEnd());
        assertEquals(StatusType.APPROVED, bookingApproved.getStatus());

        verify(bookingRepository, times(1)).save(any());
    }

    @Test
    void approveBookingWhenUnknownBookingShouldThrowException() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> bookingService.approveBooking(1L, 1L, true));
        assertEquals("Бронирование c id=" + bookingInputDto.getId() + "не найдено.", exception.getMessage());
    }

    @Test
    void approveBookingByNonOwnerShouldThrowException() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(notApproveBooking));

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> bookingService.approveBooking(2L, 2L, true));
        assertEquals("Пользователь c id=" + 2 + " не является хозяином предмета.", exception.getMessage());
    }

    @Test
    void approveBookingWhenStatusIsNotWaitingShouldThrowException() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> bookingService.approveBooking(1L, 1L, true));
        assertEquals("Бронирование не нуждается в изменении статуса.", exception.getMessage());
    }

    @Test
    void getBookingByIdByOwner() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        BookingDto booking = bookingService.getById(1L, 1L);
        assertNotEquals(booking, null);
        assertEquals(booking.getItem().getId(), item.getId());
        assertEquals(booking.getBooker().getId(), booker.getId());

        verify(bookingRepository, times(1)).findById(any());
    }

    @Test
    void getBookingByIdByBooker() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        BookingDto booking = bookingService.getById(2L, 1L);
        assertNotEquals(booking, null);
        assertEquals(booking.getItem().getId(), item.getId());
        assertEquals(booking.getBooker().getId(), booker.getId());

        verify(bookingRepository, times(1)).findById(any());
    }

    @Test
    void getBookingByIdByNonOwnerOrNonBookerShouldThrowException() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> bookingService.getById(3L, 1L));
        assertEquals("Пользователь c id=" + 3 + " не имеет отношение к предмету.", exception.getMessage());
    }

    @Test
    void getBookingByIdWithUnknownBookingShouldThrowException() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> bookingService.approveBooking(1L, 1L, true));
        assertEquals("Бронирование c id=" + bookingInputDto.getId() + "не найдено.", exception.getMessage());
    }

    @Test
    void getAllByBooker() {
        when(bookingRepository.findAllByBookerId(anyLong(), any()))
                .thenReturn(new PageImpl<Booking>(List.of(booking, notApproveBooking)));

        List<BookingDto> bookings = bookingService.getAllByBookerId(2L, "ALL", 0, 2);
        assertNotEquals(bookings, null);
        assertEquals(bookings.size(), 2);
        assertEquals(bookings.get(0).getStatus(), StatusType.APPROVED);
        assertEquals(bookings.get(1).getStatus(), StatusType.WAITING);

        verify(bookingRepository, times(1)).findAllByBookerId(anyLong(), any());
    }

    @Test
    void getAllByBookerWithoutBookings() {
        when(bookingRepository.findAllByBookerId(anyLong(), any()))
                .thenReturn(new PageImpl<Booking>(Collections.emptyList()));

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> bookingService.getAllByBookerId(1L, "ALL", 0, 2));
        assertEquals("Бронирований не найдено.", exception.getMessage());
    }

    @Test
    void getAllByBookerWithStateWaiting() {
        when(bookingRepository.findAllByBookerId(anyLong(), any()))
                .thenReturn(new PageImpl<Booking>(List.of(booking, notApproveBooking)));

        List<BookingDto> bookings = bookingService.getAllByBookerId(2L, "WAITING", 0, 2);
        assertNotEquals(bookings, null);
        assertEquals(bookings.size(), 1);
        assertEquals(bookings.get(0).getStatus(), StatusType.WAITING);

        verify(bookingRepository, times(1)).findAllByBookerId(anyLong(), any());
    }

    @Test
    void getAllByBookerWithStateRejected() {
        final Booking rejectedBooking = Booking.builder()
                .id(3L)
                .start(date.minusDays(1))
                .end(date.minusHours(1))
                .item(item)
                .booker(booker)
                .status(StatusType.REJECTED)
                .build();
        when(bookingRepository.findAllByBookerId(anyLong(), any()))
                .thenReturn(new PageImpl<Booking>(List.of(booking, rejectedBooking)));

        List<BookingDto> bookings = bookingService.getAllByBookerId(2L, "REJECTED", 0, 2);
        assertNotEquals(bookings, null);
        assertEquals(bookings.size(), 1);
        assertEquals(bookings.get(0).getStatus(), StatusType.REJECTED);
    }

    @Test
    void getAllByBookerWithStatePast() {
        when(bookingRepository.findAllByBookerId(anyLong(), any()))
                .thenReturn(new PageImpl<Booking>(List.of(booking, notApproveBooking)));

        List<BookingDto> bookings = bookingService.getAllByBookerId(2L, "PAST", 0, 2);
        assertNotEquals(bookings, null);
        assertEquals(bookings.size(), 2);
        assertTrue(bookings.get(0).getEnd().isBefore(date));
        assertTrue(bookings.get(1).getEnd().isBefore(date));

        verify(bookingRepository, times(1)).findAllByBookerId(anyLong(), any());
    }

    @Test
    void getAllByBookerWithStateFuture() {
        when(bookingRepository.findAllByBookerId(anyLong(), any()))
                .thenReturn(new PageImpl<Booking>(List.of(booking, notApproveBooking)));

        List<BookingDto> bookings = bookingService.getAllByBookerId(2L, "FUTURE", 0, 2);
        assertNotEquals(bookings, null);
        assertEquals(bookings.size(), 0);
    }

    @Test
    void getAllByBookerWithStateCurrent() {
        final Booking currentBooking = Booking.builder()
                .id(3L)
                .start(date.minusDays(1))
                .end(date.plusDays(1))
                .item(item)
                .booker(booker)
                .status(StatusType.APPROVED)
                .build();
        when(bookingRepository.findAllByBookerId(anyLong(), any()))
                .thenReturn(new PageImpl<Booking>(List.of(booking, currentBooking)));

        List<BookingDto> bookings = bookingService.getAllByBookerId(2L, "CURRENT", 0, 2);
        assertNotEquals(bookings, null);
        assertEquals(bookings.size(), 1);
        assertTrue(bookings.get(0).getEnd().isAfter(date));
    }

    @Test
    void getAllByBookerWithStateUnknownShouldThrowException() {
        when(bookingRepository.findAllByBookerId(anyLong(), any()))
                .thenReturn(new PageImpl<Booking>(List.of(booking, notApproveBooking)));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> bookingService.getAllByBookerId(2L, "OLD", 0, 2));
        assertEquals("Unknown state: UNSUPPORTED_STATUS", exception.getMessage());
    }

    @Test
    void getAllByOwnerId() {
        when(bookingRepository.getAllByOwnerId(anyLong(), any()))
                .thenReturn(new PageImpl<Booking>(List.of(booking, notApproveBooking)));

        List<BookingDto> bookings = bookingService.getAllByOwnerId(1L, "ALL", 0, 2);
        assertNotEquals(bookings, null);
        assertEquals(bookings.size(), 2);
        assertEquals(bookings.get(0).getStatus(), StatusType.APPROVED);
        assertEquals(bookings.get(1).getStatus(), StatusType.WAITING);

        verify(bookingRepository, times(1)).getAllByOwnerId(anyLong(), any());
    }

    @Test
    void getAllByOwnerWithoutItemsShouldThrowException() {
        when(bookingRepository.getAllByOwnerId(anyLong(), any()))
                .thenReturn(new PageImpl<Booking>(Collections.emptyList()));

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> bookingService.getAllByOwnerId(2L, "ALL", 0, 2));
        assertEquals("Бронирований не найдено.", exception.getMessage());
    }
}