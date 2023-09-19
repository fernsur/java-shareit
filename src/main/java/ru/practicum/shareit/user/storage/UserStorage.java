package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserStorage {
    User getById(int id);

    List<User> getAllUsers();

    User createUser(User user);

    User updateUser(User user);

    void deleteUser(int id);

    boolean checkUser(int id);
}
