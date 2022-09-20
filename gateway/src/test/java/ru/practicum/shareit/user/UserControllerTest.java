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
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mvc;
    @MockBean
    private UserClient client;

    @Test
    void createUser() throws Exception {
        final UserDto userDto = UserDto.builder()
                .id(1L)
                .name("UserName")
                .email("user@mail.ru")
                .build();
        mvc.perform(createContentFromUserDto(post("/users"), userDto))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnErrorWhenCreateUserWithInvalidEmail() throws Exception {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("UserName")
                .email("usermail.ru")
                .build();
        mvc.perform(createContentFromUserDto(post("/users"), userDto))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void shouldReturnErrorWhenCreateUserWithoutEmail() throws Exception {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("UserName")
                .email("")
                .build();
        mvc.perform(createContentFromUserDto(post("/users"), userDto))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void shouldReturnErrorWhenCreateUserWithoutName() throws Exception {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("")
                .email("user@mail.ru")
                .build();
        mvc.perform(createContentFromUserDto(post("/users"), userDto))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void updateUserName() throws Exception {
        final UserDto userDto = UserDto.builder()
                .id(1L)
                .name("UserName")
                .email("user@mail.ru")
                .build();
        UpdateUserDto inputDto = UpdateUserDto.builder()
                .name("UserNameUpdated")
                .build();

        mvc.perform(updateContentFromUserDto(patch("/users/" + userDto.getId()), inputDto))
                .andExpect(status().isOk());
    }

    @Test
    void updateUserEmail() throws Exception {
        final UserDto userDto = UserDto.builder()
                .id(1L)
                .name("UserName")
                .email("user@mail.ru")
                .build();
        UpdateUserDto inputDto = UpdateUserDto.builder()
                .email("update@mail.ru")
                .build();

        mvc.perform(updateContentFromUserDto(patch("/users/" + userDto.getId()), inputDto))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnErrorWhenUpdateUserWithoutInvalidEmail() throws Exception {
        final UserDto userDto = UserDto.builder()
                .id(1L)
                .name("UserName")
                .email("user@mail.ru")
                .build();
        UpdateUserDto inputDto = UpdateUserDto.builder()
                .email("usermail.ru")
                .build();

        mvc.perform(updateContentFromUserDto(patch("/users/" + userDto.getId()), inputDto))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getUserById() throws Exception {
        mvc.perform(get("/users/" + 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getAllUsers() throws Exception {
        mvc.perform(get("/users/"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteUser() throws Exception {
        mvc.perform(delete("/users/" + 1L))
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

    private MockHttpServletRequestBuilder updateContentFromUserDto(MockHttpServletRequestBuilder builder,
                                                                   UpdateUserDto userDto) throws JsonProcessingException {
        return builder
                .content(mapper.writeValueAsString(userDto))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
    }
}