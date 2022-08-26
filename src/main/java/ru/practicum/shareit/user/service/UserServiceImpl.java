package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.BadRequestException;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.getAllUsers().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(long id) {
        User user = userRepository.getUserById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Пользователь не найден."));
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        validate(userDto);
        User user = userRepository.createUser(UserMapper.fromUserDto(userDto));
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto updateUser(long id, UserDto userDto) {
        User userUpd = userRepository.getUserById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Пользователь не найден."));
        if (userDto.getName() != null) {
            userUpd.setName(userDto.getName());
        }
        if (userDto.getEmail() != null && validateEmail(userDto.getEmail())) {
            userUpd.setEmail(userDto.getEmail());
        }
        return UserMapper.toUserDto(userRepository.updateUser(userUpd));
    }

    @Override
    public void deleteUser(long id) {
        userRepository.deleteUser(id);
    }

    private void validate(UserDto userDto) {
        if (userDto.getEmail() == null || userDto.getEmail().isEmpty()) {
            throw new BadRequestException("Не указана почта пользователя.");
        }
        validateEmail(userDto.getEmail());
        if (getAllUsers().contains(userDto)) {
            throw new ValidationException("Пользователь уже добавлен.");
        }
    }

    private boolean validateEmail(String email) {
        if (userRepository.getAllUsers().stream()
                .anyMatch(u -> u.getEmail().toLowerCase(Locale.ROOT).equals(email.toLowerCase()))) {
            throw new ValidationException("Пользователь с почтой " + email + " уже добавлен.");
        } else return true;
    }
}