package ru.practicum.shareit.user.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final Map<Long, User> users;
    private static Integer globalId = 1;

    private static Integer getNextId() {
        return globalId++;
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public Optional<User> getUserById(long id) {
        if (users.containsKey(id)) {
            return Optional.ofNullable(users.get(id));
        } else {
            log.warn("Пользователь с id={}  не найден.", id);
            throw new ObjectNotFoundException("Пользователь не найден.");
        }
    }

    @Override
    public User createUser(User user) {
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Добавлен новый пользователь {}.", user.getName());
        return user;
    }

    @Override
    public User updateUser(User user) {
        users.put(user.getId(), user);
        log.info("Изменен профиль пользователя {}.", user.getName());
        return user;
    }

    @Override
    public void deleteUser(long id) {
        if (users.containsKey(id)) {
            users.remove(id);
            log.info("Удален профиль пользователя с id {}.", id);
        } else {
            throw new ObjectNotFoundException("Пользователь не найден.");
        }
    }
}