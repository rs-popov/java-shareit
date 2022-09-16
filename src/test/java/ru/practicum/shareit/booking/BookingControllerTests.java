package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.NestedServletException;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.InputBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.StatusType;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTests {
    @MockBean
    private BookingService bookingService;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mvc;

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
    private final Booking booking = Booking.builder()
            .id(1L)
            .start(LocalDateTime.now().plusDays(1))
            .end(LocalDateTime.now().plusDays(1))
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
            .start(LocalDateTime.now().minusDays(1))
            .end(LocalDateTime.now().minusHours(1))
            .item(item)
            .booker(booker)
            .status(StatusType.WAITING)
            .build();

    @Test
    void addBooking() throws Exception {
        when(bookingService.addBooking(anyLong(), any())).thenReturn(BookingMapper.toBookingDto(booking));
        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingInputDto))
                        .header("X-Sharer-User-Id", 2L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(booking.getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(booking.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.status", is(booking.getStatus().toString())));
    }

    @Test
    void addBookingWithNonValidBooking_shouldThrowException() throws Exception {
        final InputBookingDto nonValidBooking = InputBookingDto.builder()
                .id(booking.getId())
                .start(LocalDateTime.now().minusDays(1))
                .end(booking.getEnd())
                .itemId(booking.getItem().getId())
                .build();
        when(bookingService.addBooking(anyLong(), any())).thenReturn(BookingMapper.toBookingDto(booking));
        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(nonValidBooking))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addBookingWithoutUserIdHeader() throws Exception {
        when(bookingService.addBooking(anyLong(), any())).thenReturn(BookingMapper.toBookingDto(booking));
        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingInputDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approveBooking() throws Exception {
        when(bookingService.approveBooking(owner.getId(), booking.getId(), true))
                .thenReturn(BookingMapper.toBookingDto(booking));
        mvc.perform(patch("/bookings/" + booking.getId())
                        .header("X-Sharer-User-Id", owner.getId())
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(booking.getStatus().toString())));
    }

    @Test
    void approveBookingWithUnknownBooking() throws Exception {
        when(bookingService.approveBooking(owner.getId(), booking.getId(), true))
                .thenThrow(new ObjectNotFoundException(""));
        mvc.perform(patch("/bookings/" + booking.getId())
                        .header("X-Sharer-User-Id", owner.getId())
                        .param("approved", "true"))
                .andExpect(status().isNotFound());
    }

    @Test
    void approveBookingWithoutUserIdHeader() throws Exception {
        when(bookingService.approveBooking(owner.getId(), booking.getId(), true))
                .thenReturn(BookingMapper.toBookingDto(booking));
        mvc.perform(patch("/bookings/" + booking.getId())
                        .param("approved", "true"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getById() throws Exception {
        when(bookingService.getById(owner.getId(), booking.getId()))
                .thenReturn(BookingMapper.toBookingDto(booking));
        mvc.perform(get("/bookings/" + booking.getId())
                        .header("X-Sharer-User-Id", owner.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.id", is(booking.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.status", is(booking.getStatus().toString())));
    }

    @Test
    void getAllByBookerId() throws Exception {
        when(bookingService.getAllByBookerId(booker.getId(), "ALL", 0, 10))
                .thenReturn(List.of(BookingMapper.toBookingDto(booking), BookingMapper.toBookingDto(notApproveBooking)));
        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", booker.getId())
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status", is(booking.getStatus().toString())))
                .andExpect(jsonPath("$[1].status", is(notApproveBooking.getStatus().toString())));
    }

    @Test
    void getAllByBookerIdWithoutParam() throws Exception {
        when(bookingService.getAllByBookerId(booker.getId(), "ALL", 0, 20))
                .thenReturn(List.of(BookingMapper.toBookingDto(booking), BookingMapper.toBookingDto(notApproveBooking)));
        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", booker.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status", is(booking.getStatus().toString())))
                .andExpect(jsonPath("$[1].status", is(notApproveBooking.getStatus().toString())));
    }

    @Test
    void getAllByBookerIdWithWrongParam() {
        Exception e = assertThrows(NestedServletException.class, () -> {
            mvc.perform(get("/bookings")
                    .header("X-Sharer-User-Id", booker.getId())
                    .param("state", "ALL")
                    .param("from", "-7")
                    .param("size", "10"));
        });
        assertTrue(e.getCause().getLocalizedMessage().contains("must be greater than or equal to 0"));
    }

    @Test
    void getAllByOwnerId() throws Exception {
        when(bookingService.getAllByOwnerId(owner.getId(), "ALL", 0, 10))
                .thenReturn(List.of(BookingMapper.toBookingDto(booking), BookingMapper.toBookingDto(notApproveBooking)));
        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", owner.getId())
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status", is(booking.getStatus().toString())))
                .andExpect(jsonPath("$[1].status", is(notApproveBooking.getStatus().toString())));
    }

    @Test
    void getAllByOwnerIdWithWrongParam() {
        Exception e = assertThrows(NestedServletException.class, () -> {
            mvc.perform(get("/bookings/owner")
                    .header("X-Sharer-User-Id", owner.getId())
                    .param("state", "ALL")
                    .param("from", "-5")
                    .param("size", "-1"));
        });
        assertTrue(e.getCause().getLocalizedMessage().contains("must be greater than or equal to 0"));
    }

    @Test
    void getAllByOwnerIdWithoutParam() throws Exception {
        when(bookingService.getAllByOwnerId(owner.getId(), "ALL", 0, 20))
                .thenReturn(List.of(BookingMapper.toBookingDto(booking), BookingMapper.toBookingDto(notApproveBooking)));
        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", owner.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status", is(booking.getStatus().toString())))
                .andExpect(jsonPath("$[1].status", is(notApproveBooking.getStatus().toString())));
    }
}