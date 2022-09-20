package ru.practicum.shareit.requests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.NestedServletException;
import ru.practicum.shareit.requests.dto.ItemRequestDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mvc;
    @MockBean
    private ItemRequestClient itemRequestClient;
    private static final String USERID = "X-Sharer-User-Id";

    private final ItemRequestDto itemRequest = ItemRequestDto.builder()
            .description("itemRequestDescription")
            .created(LocalDateTime.now())
            .build();

    @Test
    void createRequest() throws Exception {
        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(itemRequest))
                        .header(USERID, 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void createRequestWithoutUserIdHeader() throws Exception {
        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(itemRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRequestWithInvalidInputRequest() throws Exception {
        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(null))
                        .header(USERID, 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRequestById() throws Exception {
        mvc.perform(get("/requests/" + 1L)
                        .header(USERID, 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getRequestByIdWithoutUserIdHeader() throws Exception {
        mvc.perform(get("/requests/" + 1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllRequestsFromRequester() throws Exception {
        mvc.perform(get("/requests")
                        .header(USERID, 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getAllRequestsFromRequesterWithoutUserIdHeader() throws Exception {
        mvc.perform(get("/requests"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllRequests() throws Exception {
        mvc.perform(get("/requests/all")
                        .header(USERID, 1L)
                        .param("from", "0")
                        .param("size", "2"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllRequestsWithoutParam() throws Exception {
        mvc.perform(get("/requests/all")
                        .header(USERID, 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getAllRequestsWithoutUserIdHeader() throws Exception {
        mvc.perform(get("/requests/all"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnErrorWhenGetAllRequestsWithWrongFromParam() {
        NestedServletException exception = assertThrows(NestedServletException.class,
                () -> mvc.perform(get("/requests/all")
                        .header(USERID, 1L)
                        .param("from", "-1")
                        .param("size", "2")));
        assertTrue(exception.getMessage().contains("must be greater than or equal to 0"));
    }

    @Test
    void shouldReturnErrorWhenGetAllRequestsWithWrongSizeParam() throws Exception {
        NestedServletException exception = assertThrows(NestedServletException.class,
                () -> mvc.perform(get("/requests/all")
                        .header(USERID, 1L)
                        .param("from", "0")
                        .param("size", "0")));
        assertTrue(exception.getMessage().contains("must be greater than 0"));
    }
}