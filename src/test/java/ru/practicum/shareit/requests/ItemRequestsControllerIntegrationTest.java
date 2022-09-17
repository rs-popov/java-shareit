package ru.practicum.shareit.requests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.requests.dto.ItemRequestOutputDto;
import ru.practicum.shareit.requests.model.ItemRequest;
import ru.practicum.shareit.requests.service.ItemRequestService;
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

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestsControllerIntegrationTest {
    private final EntityManager entityManager;
    private final UserService userService;
    private final ItemRequestService itemRequestService;

    private final User owner = User.builder()
            .id(1L)
            .name("UserName")
            .email("user@mail.ru").build();
    private final User requester = User.builder()
            .id(2L)
            .name("BookerName")
            .email("booker@mail.ru").build();
    private final ItemRequestDto itemRequest = ItemRequestDto.builder()
            .id(1L)
            .description("itemRequestDescription")
            .created(LocalDateTime.now())
            .build();

    private final ItemRequestDto anotherItemRequest = ItemRequestDto.builder()
            .id(2L)
            .description("anotherItemRequestDescription")
            .created(LocalDateTime.now())
            .build();

    @Test
    void createRequest() {
        UserDto requesterCreated = userService.createUser(UserMapper.toUserDto(requester));
        ItemRequestDto createdItemRequest = itemRequestService.createRequest(requesterCreated.getId(), itemRequest);

        TypedQuery<ItemRequest> query = entityManager.createQuery(
                "select ir from ItemRequest ir where ir.id = : id", ItemRequest.class);
        ItemRequest itemRequest1 = query.setParameter("id", createdItemRequest.getId())
                .getSingleResult();

        assertThat(itemRequest1.getId(), notNullValue());
        assertThat(itemRequest1.getDescription(), equalTo(itemRequest.getDescription()));
    }

    @Test
    void findRequestById() {
        UserDto requesterCreated = userService.createUser(UserMapper.toUserDto(requester));
        ItemRequestDto createdItemRequest = itemRequestService.createRequest(requesterCreated.getId(), itemRequest);
        ItemRequestOutputDto createdItemRequestFromGet = itemRequestService.findRequestById(createdItemRequest.getId(), requesterCreated.getId());

        assertThat(createdItemRequestFromGet.getId(), notNullValue());
        assertThat(createdItemRequestFromGet.getDescription(), equalTo(itemRequest.getDescription()));
    }

    @Test
    void findAllRequestFromRequester() {
        UserDto requesterCreated = userService.createUser(UserMapper.toUserDto(requester));
        itemRequestService.createRequest(requesterCreated.getId(), itemRequest);
        itemRequestService.createRequest(requesterCreated.getId(), anotherItemRequest);
        List<ItemRequestOutputDto> requests = itemRequestService.findAllRequestFromRequester(requesterCreated.getId());

        assertThat(requests, notNullValue());
        assertThat(requests.size(), equalTo(2));
    }

    @Test
    void findAllRequest() {
        UserDto requesterCreated = userService.createUser(UserMapper.toUserDto(requester));
        UserDto userCreated = userService.createUser(UserMapper.toUserDto(owner));
        itemRequestService.createRequest(requesterCreated.getId(), itemRequest);
        itemRequestService.createRequest(userCreated.getId(), anotherItemRequest);
        List<ItemRequestOutputDto> requests = itemRequestService.findAllRequestFromRequester(requesterCreated.getId());

        assertThat(requests, notNullValue());
        assertThat(requests.get(0).getDescription(), equalTo("itemRequestDescription"));
    }
}