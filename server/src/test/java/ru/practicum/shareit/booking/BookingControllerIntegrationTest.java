package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.InputBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.StatusType;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemInputDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingControllerIntegrationTest {
    private final EntityManager entityManager;
    private final ItemService itemService;
    private final UserService userService;
    private final BookingService bookingService;
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
            .available(true)
            .build();
    private UserDto ownerCreated;
    private UserDto bookerCreated;
    private ItemInputDto createdItem;
    private ItemInputDto createdAnotherItem;

    @BeforeEach
    void doBeforeEach() {
        ownerCreated = userService.createUser(UserMapper.toUserDto(owner));
        bookerCreated = userService.createUser(UserMapper.toUserDto(booker));
        createdItem = itemService.createItem(ItemMapper.toItemDto(item), ownerCreated.getId());
        createdAnotherItem = itemService.createItem(ItemMapper.toItemDto(anotherItem), ownerCreated.getId());
    }

    @Test
    void addBooking() {
        InputBookingDto bookingInputDto = InputBookingDto.builder()
                .start(date.plusDays(1))
                .end(date.plusDays(2))
                .itemId(createdItem.getId())
                .build();
        BookingDto createdBooking = bookingService.addBooking(bookerCreated.getId(), bookingInputDto);
        TypedQuery<Booking> query = entityManager.createQuery(
                "select b from Booking b where b.id = : id", Booking.class);
        Booking booking1 = query.setParameter("id", createdBooking.getId())
                .getSingleResult();

        assertThat(booking1.getId(), notNullValue());
        assertThat(booking1.getStatus(), equalTo(StatusType.WAITING));
        assertThat(booking1.getItem().getId(), equalTo(createdItem.getId()));
    }

    @Test
    void approveBooking() {
        InputBookingDto bookingInputDto = InputBookingDto.builder()
                .start(date.plusDays(1))
                .end(date.plusDays(2))
                .itemId(createdItem.getId())
                .build();
        BookingDto createdBooking = bookingService.addBooking(bookerCreated.getId(), bookingInputDto);
        BookingDto approveBooking = bookingService.approveBooking(ownerCreated.getId(), createdBooking.getId(), true);

        TypedQuery<Booking> query = entityManager.createQuery(
                "select b from Booking b where b.id = : id", Booking.class);
        Booking booking1 = query.setParameter("id", approveBooking.getId())
                .getSingleResult();

        assertThat(booking1.getId(), notNullValue());
        assertThat(booking1.getStatus(), equalTo(StatusType.APPROVED));
        assertThat(booking1.getItem().getId(), equalTo(createdItem.getId()));
    }

    @Test
    void getAllByBookerId() {
        InputBookingDto bookingInputDto = InputBookingDto.builder()
                .start(date.plusDays(1))
                .end(date.plusDays(2))
                .itemId(createdItem.getId())
                .build();
        BookingDto createdBooking = bookingService.addBooking(bookerCreated.getId(), bookingInputDto);
        InputBookingDto anotherBookingInputDto = InputBookingDto.builder()
                .start(date.plusDays(1))
                .end(date.plusDays(2))
                .itemId(createdAnotherItem.getId())
                .build();
        BookingDto createdBooking2 = bookingService.addBooking(bookerCreated.getId(), anotherBookingInputDto);
        List<BookingDto> bookings = bookingService.getAllByBookerId(bookerCreated.getId(), "ALL", 0, 2);

        assertThat(bookings.size(), equalTo(2));
        assertTrue(bookings.contains(createdBooking));
        assertTrue(bookings.contains(createdBooking2));
    }

    @Test
    void getAllByOwnerId() {
        InputBookingDto bookingInputDto = InputBookingDto.builder()
                .start(date.plusDays(1))
                .end(date.plusDays(2))
                .itemId(createdItem.getId())
                .build();
        BookingDto createdBooking = bookingService.addBooking(bookerCreated.getId(), bookingInputDto);
        InputBookingDto anotherBookingInputDto = InputBookingDto.builder()
                .start(date.plusDays(1))
                .end(date.plusDays(2))
                .itemId(createdAnotherItem.getId())
                .build();
        BookingDto createdBooking2 = bookingService.addBooking(bookerCreated.getId(), anotherBookingInputDto);
        List<BookingDto> bookings = bookingService.getAllByOwnerId(ownerCreated.getId(), "ALL", 0, 2);

        assertThat(bookings.size(), equalTo(2));
        assertTrue(bookings.contains(createdBooking));
        assertTrue(bookings.contains(createdBooking2));
    }
}


