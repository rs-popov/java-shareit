package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User createUser(User user);

    List<User> getAllUsers();

    Optional<User> getUserById(long id);

    User updateUser(User user);

    void deleteUser(long id);
}