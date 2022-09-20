package ru.practicum.shareit.booking;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.util.NestedServletException;
import ru.practicum.shareit.booking.dto.InputBookingDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mvc;
    @MockBean
    private BookingClient bookingClient;
    private final LocalDateTime date = LocalDateTime.now();
    private static final String USERID = "X-Sharer-User-Id";

    @Test
    void addBooking() throws Exception {
        mvc.perform(createContentFromInputBookingDto(post("/bookings"),
                        getBookingDto(date.plusDays(1), date.plusDays(2)), 2L))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnErrorWhenAddBookingWithoutUserIdHeader() throws Exception {
        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(getBookingDto(date.plusDays(1), date.plusDays(2))))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnErrorWhenAddBookingWithWrongStartDate() throws Exception {
        mvc.perform(createContentFromInputBookingDto(post("/bookings"),
                        getBookingDto(date.minusDays(1), date.plusDays(1)), 2L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnErrorWhenAddBookingWithWrongEndDate() throws Exception {
        mvc.perform(createContentFromInputBookingDto(post("/bookings"),
                        getBookingDto(date.plusDays(1), date.minusDays(1)), 2L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approveBooking() throws Exception {
        mvc.perform(patch("/bookings/" + 1L)
                        .header(USERID, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk());
    }

    @Test
    void approveBookingWithoutUserIdHeader() throws Exception {
        mvc.perform(patch("/bookings/" + 1L)
                        .param("approved", "true"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getById() throws Exception {
        mvc.perform(get("/bookings/" + 1L)
                        .header(USERID, 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getAllByBookerId() throws Exception {
        mvc.perform(createRequestWithPagination(get("/bookings"),
                        2L,
                        "ALL",
                        "0",
                        "10"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllByBookerIdWithoutParam() throws Exception {
        mvc.perform(get("/bookings")
                        .header(USERID, 2L))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnErrorWhenGetAllByBookerIdWithWrongSizeParam() {
        NestedServletException exception = assertThrows(NestedServletException.class,
                () -> mvc.perform(createRequestWithPagination(get("/bookings"),
                        2L,
                        "ALL",
                        "0",
                        "-10")));
        assertTrue(exception.getMessage().contains("must be greater than 0"));
    }

    @Test
    void shouldReturnErrorWhenGetAllByBookerIdWithWrongFromParam() {
        NestedServletException exception = assertThrows(NestedServletException.class,
                () -> mvc.perform(createRequestWithPagination(get("/bookings"),
                        2L,
                        "ALL",
                        "-1",
                        "10")));
        assertTrue(exception.getMessage().contains("must be greater than or equal to 0"));
    }

    @Test
    void shouldReturnErrorWhenGetAllByBookerIdWithWrongStatusParam() throws Exception {
        mvc.perform(createRequestWithPagination(get("/bookings"),
                        2L,
                        "OLD",
                        "0",
                        "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllByOwnerId() throws Exception {
        mvc.perform(createRequestWithPagination(get("/bookings/owner"),
                        2L,
                        "ALL",
                        "0",
                        "10"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnErrorWhenGetAllByOwnerWithWrongSizeParam() {
        NestedServletException exception = assertThrows(NestedServletException.class,
                () -> mvc.perform(createRequestWithPagination(get("/bookings/owner"),
                        2L,
                        "ALL",
                        "0",
                        "-10")));
        assertTrue(exception.getMessage().contains("must be greater than 0"));
    }

    @Test
    void shouldReturnErrorWhenGetAllByOwnerWithWrongFromParam() {
        NestedServletException exception = assertThrows(NestedServletException.class,
                () -> mvc.perform(createRequestWithPagination(get("/bookings/owner"),
                        2L,
                        "ALL",
                        "-1",
                        "10")));
        assertTrue(exception.getMessage().contains("must be greater than or equal to 0"));
    }

    @Test
    void getAllByOwnerIdWithoutParam() throws Exception {
        mvc.perform(get("/bookings/owner")
                        .header(USERID, 1L)
                )
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnErrorWhenGetAllByByOwnerWithWrongStatusParam() throws Exception {
        mvc.perform(createRequestWithPagination(get("/bookings/owner"),
                        2L,
                        "OLD",
                        "0",
                        "10"))
                .andExpect(status().isBadRequest());
    }

    private MockHttpServletRequestBuilder createContentFromInputBookingDto(MockHttpServletRequestBuilder builder,
                                                                           InputBookingDto inputBookingDto,
                                                                           Long id) throws JsonProcessingException {
        return builder
                .content(mapper.writeValueAsString(inputBookingDto))
                .header(USERID, id)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
    }

    private MockHttpServletRequestBuilder createRequestWithPagination(MockHttpServletRequestBuilder builder,
                                                                      Long id,
                                                                      String state,
                                                                      String from,
                                                                      String size) throws JsonProcessingException {
        return builder
                .header(USERID, id)
                .param("state", state)
                .param("from", from)
                .param("size", size);
    }

    private InputBookingDto getBookingDto(LocalDateTime start, LocalDateTime end) {
        return InputBookingDto.builder()
                .start(start)
                .end(end)
                .itemId(1L)
                .build();
    }
}