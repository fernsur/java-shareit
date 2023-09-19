package ru.practicum.shareit.user.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import ru.practicum.shareit.exception.UserAlreadyExistsException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    @Override
    public User getById(int id) {
        if (!users.containsKey(id)) {
            String warning = "Невозможно получить. Такого пользователя нет.";
            log.warn(warning);
            throw new UserNotFoundException(warning);
        }
        return users.get(id);
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User createUser(User user) {
        checkEmail(user.getEmail(), 0);
        user.setId(nextId++);
        users.put(user.getId(),user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        Integer id = user.getId();
        if (!users.containsKey(id)) {
            log.warn("Пользователь с id = {} не существует", id);
            throw new UserNotFoundException("Такого пользователя нет.");
        }

        if (user.getEmail() != null) {
            String email = user.getEmail();
            checkEmail(email, id);
            users.get(id).setEmail(email);
        }
        if (user.getName() != null) {
            String name = user.getName();
            users.get(id).setName(name);
        }

        return users.get(id);
    }

    @Override
    public void deleteUser(int id) {
        if (!users.containsKey(id)) {
            String warning = "Невозможно удалить. Такого пользователя нет.";
            log.warn(warning);
            throw new UserNotFoundException(warning);
        }
        users.remove(id);
    }

    @Override
    public boolean checkUser(int id) {
        return users.containsKey(id);
    }

    private void checkEmail(String email, int id) {
        List<User> userFilter = users.values().stream()
                .filter(user -> user.getEmail().equals(email))
                .filter(user -> user.getId() != id)
                .collect(Collectors.toList());

        if (!userFilter.isEmpty()) {
            String warning = "Пользователь с email = " + email + " уже существует";
            log.warn(warning);
            throw new UserAlreadyExistsException(warning);
        }
    }
}
