package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exceptions.BadRequestException;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserUnitTests {
    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private UserRepository userRepository;

    private final User user = User.builder()
            .id(1L)
            .name("UserName")
            .email("user@mail.ru")
            .build();
    private final User user2 = User.builder()
            .id(2L)
            .name("UserName2")
            .email("user2@mail.ru")
            .build();
    private final UserDto userDto = UserDto.builder()
            .id(1L)
            .name("UserName")
            .email("user@mail.ru")
            .build();

    @Test
    void testMapperFromUsertoUserDto() {
        UserDto result = UserMapper.toUserDto(user);
        assertEquals(result.getId(), user.getId());
        assertEquals(result.getName(), user.getName());
        assertEquals(result.getEmail(), user.getEmail());
    }

    @Test
    void testMapperFromUserDtotoUser() {
        User result = UserMapper.fromUserDto(userDto);
        assertEquals(result.getId(), userDto.getId());
        assertEquals(result.getName(), userDto.getName());
        assertEquals(result.getEmail(), userDto.getEmail());
    }

    @Test
    void testCreateUser() {
        when(userRepository.save(any())).thenReturn(user);
        UserDto result = userService.createUser(userDto);
        assertEquals(result, userDto);
        verify(userRepository, times(1)).save(any());
    }

    @Test
    void testCreateUserWithNoEmail_shouldThrowException() {
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> userService.createUser(UserDto.builder()
                        .name("UserNameUpdate").build()));
        assertEquals("Не указана почта пользователя.", exception.getMessage());
    }

    @Test
    void updateUserName() {
        when(userRepository.save(any())).thenReturn(User.builder()
                .id(1L)
                .name("UserNameUpdate")
                .email("user@mail.ru")
                .build());
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        UserDto result = userService.updateUser(1L, UserDto.builder()
                .name("UserNameUpdate").build());
        assertEquals(result.getName(), "UserNameUpdate");
        assertEquals(result.getEmail(), "user@mail.ru");
        verify(userRepository, times(1)).save(any());
    }

    @Test
    void updateUserEmail() {
        when(userRepository.save(any())).thenReturn(User.builder()
                .id(1L)
                .name("UserName")
                .email("userUpdate@mail.ru")
                .build());
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        UserDto result = userService.updateUser(1L, UserDto.builder()
                .email("userUpdate@mail.ru").build());
        assertEquals(result.getName(), "UserName");
        assertEquals(result.getEmail(), "userUpdate@mail.ru");
        verify(userRepository, times(1)).save(any());
    }

    @Test
    void getUserById() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        UserDto result = userService.getUserById(anyLong());
        assertEquals(result.getName(), user.getName());
        assertEquals(result.getEmail(), user.getEmail());
        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    void getUserByIdWithUnknownId_shouldThrowException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> userService.getUserById(11L));
        assertEquals("Пользователь c id=11 не найден.", exception.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    void getAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user, user2));
        List<UserDto> result = userService.getAllUsers();
        assertEquals(user.getId(), result.get(0).getId());
        assertEquals(user2.getId(), result.get(1).getId());
        assertEquals(result.size(), 2);
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void deleteUser() {
        userService.deleteUser(1L);
        verify(userRepository, times(1)).deleteById(anyLong());
    }
}