package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserControllerIntegrationTest {
    private final EntityManager entityManager;
    private final UserService userService;

    private final UserDto userDto = UserDto.builder()
            .id(1L)
            .name("UserName")
            .email("user@mail.ru")
            .build();
    private final UserDto anotherUserDto = UserDto.builder()
            .id(2L)
            .name("UserName2")
            .email("user2@mail.ru")
            .build();

    @Test
    public void createUser() {
        UserDto createdUser = userService.createUser(userDto);
        TypedQuery<User> query = entityManager.createQuery(
                "select u from User u where u.id = : id", User.class);
        User user = query.setParameter("id", createdUser.getId())
                .getSingleResult();

        assertThat(user.getId(), notNullValue());
        assertThat(user.getName(), equalTo(userDto.getName()));
        assertThat(user.getEmail(), equalTo(userDto.getEmail()));
    }

    @Test
    void getUserById() {
        UserDto createdUser = userService.createUser(userDto);
        UserDto userFromGet = userService.getUserById(createdUser.getId());

        assertThat(userFromGet, notNullValue());
        assertThat(userFromGet.getName(), equalTo(userDto.getName()));
        assertThat(userFromGet.getEmail(), equalTo(userDto.getEmail()));
    }

    @Test
    void getAllUsers() {
        userService.createUser(userDto);
        userService.createUser(anotherUserDto);
        List<UserDto> users = userService.getAllUsers();

        assertThat(users, hasSize(2));
        assertThat(users.get(0).getName(), equalTo(userDto.getName()));
        assertThat(users.get(1).getName(), equalTo(anotherUserDto.getName()));
    }

    @Test
    void updateUser() {
        UserDto oldUser = userService.createUser(userDto);
        UserDto oldUserFromGet = userService.getUserById(oldUser.getId());
        assertThat(oldUserFromGet, notNullValue());
        UserDto updatedUser = userService.updateUser(oldUser.getId(), UserDto.builder()
                .name("nameUpdate").build());
        UserDto updateUserFromGet = userService.getUserById(updatedUser.getId());
        assertThat(updateUserFromGet, notNullValue());

        TypedQuery<User> query = entityManager.createQuery(
                "select u from User u where u.id = : id", User.class);
        User user = query.setParameter("id", updatedUser.getId())
                .getSingleResult();

        assertThat(user.getId(), notNullValue());
        assertThat(user.getName(), equalTo("nameUpdate"));
    }

    @Test
    public void deleteUser() {
        UserDto createdUser = userService.createUser(userDto);
        userService.deleteUser(createdUser.getId());
        User user = entityManager.find(User.class, createdUser.getId());
        assertThat(user, nullValue());
    }
}