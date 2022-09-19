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
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Пользователь c id=" + id + " не найден."));
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        if (userDto.getEmail() == null || userDto.getEmail().isEmpty()) {
            log.warn("При создании профиля не была указана почта пользователя.");
            throw new BadRequestException("Не указана почта пользователя.");
        }
        User user = userRepository.save(UserMapper.fromUserDto(userDto));
        log.info("Создан профиль пользователя {}, id={}", user.getName(), user.getId());
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto updateUser(Long id, UserDto userDto) {
        User userUpd = userRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Пользователь c id=" + id + "не найден."));
        if (userDto.getName() != null) {
            userUpd.setName(userDto.getName());
        }
        if (userDto.getEmail() != null && validateEmail(userDto.getEmail())) {
            userUpd.setEmail(userDto.getEmail());
        }
        UserDto userDtoUpd = UserMapper.toUserDto(userRepository.save(userUpd));
        log.info("Изменен профиль пользователя {}, id={}", userDtoUpd.getName(), userDtoUpd.getId());
        return userDtoUpd;
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
        log.info("Удален  профиль пользователя , id={}", id);
    }

    private boolean validateEmail(String email) {
        if (userRepository.findAll().stream()
                .anyMatch(u -> u.getEmail().toLowerCase(Locale.ROOT).equals(email.toLowerCase()))) {
            throw new ValidationException("Пользователь с почтой " + email + " уже добавлен.");
        } else return true;
    }
}