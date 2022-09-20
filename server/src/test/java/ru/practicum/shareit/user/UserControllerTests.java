package ru.practicum.shareit.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import ru.practicum.shareit.exceptions.BadRequestException;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
public class UserControllerTests {
    @MockBean
    private UserService userService;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mvc;

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
    void createUser() throws Exception {
        when(userService.createUser(any())).thenReturn(userDto);

        mvc.perform(createContentFromUserDto(post("/users"), userDto))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));
    }

    @Test
    void testCreateUserWithNoEmailShouldThrowBadRequestException() throws Exception {
        when(userService.createUser(userDto)).thenThrow(new BadRequestException("Не указана почта пользователя."));

        mvc.perform(createContentFromUserDto(post("/users"), userDto))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.error", is("Не указана почта пользователя.")));
    }

    @Test
    void testCreateUserWithDuplicateEmailShouldThrowBadRequestException() throws Exception {
        when(userService.createUser(userDto)).thenThrow(new ValidationException("Пользователь уже добавлен."));

        mvc.perform(createContentFromUserDto(post("/users"), userDto))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.error", is("Пользователь уже добавлен.")));
    }

    @Test
    void updateUserName() throws Exception {
        UserDto inputDto = UserDto.builder()
                .name("UserNameUpdated")
                .build();
        UserDto outputDto = UserDto.builder()
                .id(1L)
                .name("UserNameUpdated")
                .email("user@mail.ru").build();

        when(userService.updateUser(userDto.getId(), inputDto)).thenReturn(outputDto);

        mvc.perform(createContentFromUserDto(patch("/users/" + userDto.getId()), inputDto))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(outputDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(outputDto.getName())))
                .andExpect(jsonPath("$.email", is(outputDto.getEmail())));
    }

    @Test
    void updateUserEmail() throws Exception {
        UserDto inputDto = UserDto.builder()
                .email("userUpdate@mail.ru")
                .build();
        UserDto outputDto = UserDto.builder()
                .id(1L)
                .name("UserName")
                .email("userUpdate@mail.ru").build();
        when(userService.updateUser(userDto.getId(), inputDto)).thenReturn(outputDto);

        mvc.perform(createContentFromUserDto(patch("/users/" + userDto.getId()), inputDto))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(outputDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(outputDto.getName())))
                .andExpect(jsonPath("$.email", is(outputDto.getEmail())));
    }

    @Test
    void updateUserEmailWithDuplicateEmailShouldThrowBadRequestException() throws Exception {
        UserDto inputDto = UserDto.builder()
                .email("user@mail.ru")
                .build();
        when(userService.updateUser(userDto.getId(), inputDto))
                .thenThrow(new ValidationException("Пользователь с почтой email уже добавлен."));

        mvc.perform(createContentFromUserDto(patch("/users/" + userDto.getId()), inputDto))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.error", is("Пользователь с почтой email уже добавлен.")));
    }

    @Test
    void getUserById() throws Exception {
        Long id = 1L;
        when(userService.getUserById(id)).thenReturn(userDto);

        mvc.perform(get("/users/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));
    }

    @Test
    void getUserByIdWithUnknownIdShouldThrowException() throws Exception {
        Long unknownId = 11L;
        when(userService.getUserById(unknownId))
                .thenThrow(new ObjectNotFoundException("Пользователь c id=" + unknownId + "не найден."));

        mvc.perform(get("/users/" + unknownId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllUsers() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(userDto, anotherUserDto));

        mvc.perform(get("/users/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$[1].id", is(anotherUserDto.getId()), Long.class));
    }

    @Test
    void deleteUser() throws Exception {
        long id = 1L;
        mvc.perform(delete("/users/" + id))
                .andExpect(status().isOk());
    }

    private MockHttpServletRequestBuilder createContentFromUserDto(MockHttpServletRequestBuilder builder,
                                                                   UserDto userDto) throws JsonProcessingException {
        return builder
                .content(mapper.writeValueAsString(userDto))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
    }
}