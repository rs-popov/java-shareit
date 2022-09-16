package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.InputBookingDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.dto.ItemInputDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.dto.ItemOutputDto;
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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemIntegrationTests {
    private final EntityManager entityManager;
    private final ItemService itemService;
    private final UserService userService;
    private final BookingService bookingService;

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
            .id(10L)
            .name("ItemName2")
            .description("ItemDesc2")
            .owner(owner)
            .available(true)
            .build();

    @Test
    public void createItem() {
        UserDto userCreated = userService.createUser(UserMapper.toUserDto(owner));
        ItemInputDto createdItem = itemService.createItem(ItemMapper.toItemDto(item), userCreated.getId());
        TypedQuery<Item> query = entityManager.createQuery(
                "select i from Item i where i.id = : id", Item.class);
        Item item1 = query.setParameter("id", createdItem.getId())
                .getSingleResult();
        assertThat(item1.getId(), notNullValue());
        assertThat(item1.getName(), equalTo(createdItem.getName()));
        assertThat(item1.getDescription(), equalTo(createdItem.getDescription()));
    }

    @Test
    public void deleteItem() {
        UserDto userCreated = userService.createUser(UserMapper.toUserDto(owner));
        ItemInputDto createdItem = itemService.createItem(ItemMapper.toItemDto(item), userCreated.getId());
        itemService.deleteItem(createdItem.getId());
        Item item1 = entityManager.find(Item.class, createdItem.getId());
        assertThat(item1, nullValue());
    }

    @Test
    void getItemById() {
        UserDto userCreated = userService.createUser(UserMapper.toUserDto(owner));
        ItemInputDto createdItem = itemService.createItem(ItemMapper.toItemDto(item), userCreated.getId());
        ItemOutputDto createdItemFromGet = itemService.getItemById(createdItem.getId(), userCreated.getId());
        assertThat(createdItemFromGet, notNullValue());
        assertThat(createdItemFromGet.getName(), equalTo(item.getName()));
        assertThat(createdItemFromGet.getDescription(), equalTo(item.getDescription()));
    }

    @Test
    void updateItem() {
        UserDto userCreated = userService.createUser(UserMapper.toUserDto(owner));
        ItemInputDto createdItem = itemService.createItem(ItemMapper.toItemDto(item), userCreated.getId());
        assertThat(createdItem, notNullValue());

        ItemInputDto updatedItem = itemService.updateItem(createdItem.getId(),
                ItemInputDto.builder().name("UpdatedName").build(), userCreated.getId());
        ItemOutputDto updatedItemFromGet = itemService.getItemById(updatedItem.getId(), userCreated.getId());
        assertThat(updatedItemFromGet, notNullValue());
        TypedQuery<Item> query = entityManager.createQuery(
                "select i from Item i where i.id = : id", Item.class);
        Item item1 = query.setParameter("id", createdItem.getId())
                .getSingleResult();
        assertThat(item1.getId(), notNullValue());
        assertThat(item1.getName(), equalTo("UpdatedName"));
    }

    @Test
    void getAllItems() {
        UserDto userCreated = userService.createUser(UserMapper.toUserDto(owner));
        itemService.createItem(ItemMapper.toItemDto(item), userCreated.getId());
        itemService.createItem(ItemMapper.toItemDto(anotherItem), userCreated.getId());
        List<ItemOutputDto> items = itemService.getAllItemsByOwner(userCreated.getId(), 0, 20);
        assertThat(items, hasSize(2));
        assertThat(items.get(0).getName(), equalTo(item.getName()));
        assertThat(items.get(1).getName(), equalTo(anotherItem.getName()));
    }


    @Test
    public void createComment() {
        User booker = User.builder()
                .id(10L)
                .name("BookerName")
                .email("Booker@mail.ru").build();
        CommentDto comment = CommentDto.builder()
                .id(1L)
                .text("this is comment")
                .authorName(booker.getName())
                .created(LocalDateTime.now().plusDays(1))
                .build();
        UserDto ownerCreated = userService.createUser(UserMapper.toUserDto(owner));
        ItemInputDto createdItem = itemService.createItem(ItemMapper.toItemDto(item), ownerCreated.getId());
        UserDto bookerCreated = userService.createUser(UserMapper.toUserDto(booker));
        BookingDto bookingCreated = bookingService.addBooking(bookerCreated.getId(), InputBookingDto.builder()
                .id(1L)
                .itemId(createdItem.getId())
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now().minusHours(1))
                .build());
        bookingService.approveBooking(ownerCreated.getId(), bookingCreated.getId(), true);
        CommentDto commentCreated = itemService.createComment(bookerCreated.getId(),
                createdItem.getId(), comment);
        assertThat(commentCreated, notNullValue());
        TypedQuery<Comment> query = entityManager.createQuery(
                "select c from Comment c where c.id = : id", Comment.class);
        Comment comment1 = query.setParameter("id", commentCreated.getId())
                .getSingleResult();
        assertThat(comment1.getId(), notNullValue());
        assertThat(comment1.getText(), equalTo("this is comment"));
    }
}
