package ru.practicum.shareit.item;

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
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemInputDto;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mvc;
    @MockBean
    private ItemClient itemClient;
    private static final String USERID = "X-Sharer-User-Id";

    private final ItemInputDto item = ItemInputDto.builder()
            .name("ItemName")
            .description("ItemDesc")
            .available(true)
            .build();

    @Test
    void createItem() throws Exception {
        mvc.perform(createContentFromItemInputDto(post("/items"), item, 1L))
                .andExpect(status().isOk());
    }

    @Test
    void createItemWithoutUserIdHeader() throws Exception {
        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(item))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItemName() throws Exception {
        mvc.perform(createContentFromItemInputDto(patch("/items/" + 1), item, 1L))
                .andExpect(status().isOk());
    }

    @Test
    void updateItemWithoutUserIdHeader() throws Exception {
        mvc.perform(patch("/items/" + 1)
                        .content(mapper.writeValueAsString(item))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteItem() throws Exception {
        mvc.perform(delete("/items/" + 1))
                .andExpect(status().isOk());
    }

    @Test
    void getAllItemsByOwner() throws Exception {
        mvc.perform(get("/items")
                        .header(USERID, 1L)
                        .param("from", "0")
                        .param("size", "2"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnErrorWhenGetAllItemsByOwnerWithWrongFromParam() {
        NestedServletException exception = assertThrows(NestedServletException.class,
                () -> mvc.perform(createRequestWithPagination(get("/items"),
                        1L,
                        "-1",
                        "10")));
        assertTrue(exception.getMessage().contains("must be greater than or equal to 0"));
    }

    @Test
    void shouldReturnErrorWhenGetAllItemsByOwnerWithWrongSizeParam() {
        NestedServletException exception = assertThrows(NestedServletException.class,
                () -> mvc.perform(createRequestWithPagination(get("/items"),
                        1L,
                        "0",
                        "-10")));
        assertTrue(exception.getMessage().contains("must be greater than 0"));
    }

    @Test
    void getItemById() throws Exception {
        mvc.perform(get("/items/" + 1)
                        .header(USERID, 1L))
                .andExpect(status().isOk());
    }

    @Test
    void searchItems() throws Exception {
        mvc.perform(get("/items/search")
                        .header(USERID, 1L)
                        .param("text", "Item")
                        .param("from", "0")
                        .param("size", "2"))
                .andExpect(status().isOk());
    }

    @Test
    void createComment() throws Exception {
        CommentDto comment = CommentDto.builder()
                .text("this is comment")
                .authorName("authorName")
                .build();

        mvc.perform(post("/items/" + 1 + "/comment")
                        .content(mapper.writeValueAsString(comment))
                        .header(USERID, 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    private MockHttpServletRequestBuilder createContentFromItemInputDto(MockHttpServletRequestBuilder builder,
                                                                        ItemInputDto itemInputDto,
                                                                        Long id) throws JsonProcessingException {
        return builder
                .content(mapper.writeValueAsString(itemInputDto))
                .header(USERID, id)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
    }

    private MockHttpServletRequestBuilder createRequestWithPagination(MockHttpServletRequestBuilder builder,
                                                                      Long id,
                                                                      String from,
                                                                      String size) throws JsonProcessingException {
        return builder
                .header(USERID, id)
                .param("from", from)
                .param("size", size);
    }
}


