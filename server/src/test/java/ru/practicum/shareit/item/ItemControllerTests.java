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
import ru.practicum.shareit.exceptions.ForbiddenAccessException;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.ItemInputDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
public class ItemControllerTests {
    @MockBean
    private ItemService itemService;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mvc;

    private final User owner = User.builder()
            .id(1L)
            .name("UserName")
            .email("user@mail.ru").build();

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

    @Test
    void createItem() throws Exception {
        ItemInputDto itemInputDto = ItemMapper.toItemDto(item);
        when(itemService.createItem(itemInputDto, 1L)).thenReturn(itemInputDto);

        mvc.perform(createContentFromItemInputDto(post("/items"), itemInputDto, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemInputDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemInputDto.getName())))
                .andExpect(jsonPath("$.description", is(itemInputDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemInputDto.getAvailable())));
    }

    @Test
    void createItemWithoutUserIdHeader() throws Exception {
        ItemInputDto itemInputDto = ItemMapper.toItemDto(item);
        when(itemService.createItem(itemInputDto, 1L)).thenReturn(itemInputDto);

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemInputDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItemName() throws Exception {
        ItemInputDto itemDto = ItemMapper.toItemDto(item);
        ItemInputDto itemInputDto = ItemInputDto.builder().name("NameUpdated").build();
        ItemInputDto itemOutput = ItemInputDto.builder()
                .id(1L)
                .name("NameUpdated")
                .description("ItemDesc")
                .available(true)
                .build();
        when(itemService.updateItem(itemDto.getId(), itemInputDto, 1L)).thenReturn(itemOutput);

        mvc.perform(createContentFromItemInputDto(patch("/items/" + itemDto.getId()), itemInputDto, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemOutput.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemOutput.getName())))
                .andExpect(jsonPath("$.description", is(itemOutput.getDescription())))
                .andExpect(jsonPath("$.available", is(itemOutput.getAvailable())));
    }

    @Test
    void updateItemNameByNonOwnerShouldThrowException() throws Exception {
        ItemInputDto itemDto = ItemMapper.toItemDto(item);
        ItemInputDto itemInputDto = ItemInputDto.builder().name("NameUpdated").build();
        ItemInputDto.builder()
                .id(1L)
                .name("NameUpdated")
                .description("ItemDesc")
                .available(true)
                .build();
        when(itemService.updateItem(itemDto.getId(), itemInputDto, 11L))
                .thenThrow(new ForbiddenAccessException("Редактировать вещь может только её владелец."));

        mvc.perform(createContentFromItemInputDto(patch("/items/" + itemDto.getId()), itemInputDto, 11L))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateItemWithoutUserIdHeader() throws Exception {
        ItemInputDto itemDto = ItemMapper.toItemDto(item);
        ItemInputDto itemInputDto = ItemInputDto.builder().name("NameUpdated").build();
        ItemInputDto itemOutput = ItemInputDto.builder()
                .id(1L)
                .name("NameUpdated")
                .description("ItemDesc")
                .available(true)
                .build();
        when(itemService.updateItem(itemDto.getId(), itemInputDto, 1L)).thenReturn(itemOutput);

        mvc.perform(patch("/items/" + itemDto.getId())
                        .content(mapper.writeValueAsString(itemInputDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteItem() throws Exception {
        long id = 1L;
        mvc.perform(delete("/items/" + id))
                .andExpect(status().isOk());
    }

    @Test
    void getAllItemsByOwner() throws Exception {
        when(itemService.getAllItemsByOwner(1L, 0, 2))
                .thenReturn(List.of(ItemMapper.toItemOutputDto(item), ItemMapper.toItemOutputDto(anotherItem)));

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id", is(item.getId()), Long.class))
                .andExpect(jsonPath("$[1].id", is(anotherItem.getId()), Long.class));
    }

    @Test
    void getItemById() throws Exception {
        when(itemService.getItemById(anyLong(), anyLong())).thenReturn(ItemMapper.toItemOutputDto(item));

        mvc.perform(get("/items/" + item.getId())
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(item.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(item.getName())))
                .andExpect(jsonPath("$.description", is(item.getDescription())))
                .andExpect(jsonPath("$.available", is(item.getAvailable())));
    }

    @Test
    void searchItems() throws Exception {
        when(itemService.searchItems(anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(ItemMapper.toItemDto(item), ItemMapper.toItemDto(anotherItem)));

        mvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1L)
                        .param("text", "Item")
                        .param("from", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id", is(item.getId()), Long.class))
                .andExpect(jsonPath("$[1].id", is(anotherItem.getId()), Long.class));
    }

    @Test
    void createComment() throws Exception {
        CommentDto comment = CommentDto.builder()
                .id(1L)
                .text("this is comment")
                .authorName("authorName")
                .build();
        when(itemService.createComment(anyLong(), anyLong(), any()))
                .thenReturn(comment);

        mvc.perform(post("/items/" + item.getId() + "/comment")
                        .content(mapper.writeValueAsString(comment))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(comment.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(comment.getText())))
                .andExpect(jsonPath("$.authorName", is(comment.getAuthorName())));
    }

    private MockHttpServletRequestBuilder createContentFromItemInputDto(MockHttpServletRequestBuilder builder,
                                                                        ItemInputDto itemInputDto,
                                                                        Long id) throws JsonProcessingException {
        return builder
                .content(mapper.writeValueAsString(itemInputDto))
                .header("X-Sharer-User-Id", id)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
    }
}