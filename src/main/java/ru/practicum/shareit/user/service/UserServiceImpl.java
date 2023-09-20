package ru.practicum.shareit.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.exception.UserAlreadyExistsException;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.storage.UserStorage;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;
    private final ItemStorage itemStorage;

    @Autowired
    public UserServiceImpl(UserStorage userStorage, ItemStorage itemStorage) {
        this.userStorage = userStorage;
        this.itemStorage = itemStorage;
    }

    @Override
    public UserDto userById(int id) {
        return UserMapper.toUserDto(userStorage.getById(id));
    }

    @Override
    public List<UserDto> allUsers() {
        return userStorage.getAllUsers()
                .stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        checkEmail(user.getEmail(), 0);
        return UserMapper.toUserDto(userStorage.createUser(user));
    }

    @Override
    public UserDto updateUser(UserDto userDto, int id) {
        userDto.setId(id);
        User newUser = UserMapper.toUser(userDto);
        User user = userStorage.getById(id);

        if (newUser.getEmail() != null) {
            String email = newUser.getEmail();
            checkEmail(email, id);
            user.setEmail(email);
        }
        if (newUser.getName() != null) {
            String name = newUser.getName();
            user.setName(name);
        }

        return UserMapper.toUserDto(userStorage.updateUser(user));
    }

    @Override
    public void deleteUser(int id) {
        userStorage.deleteUser(id);
        itemStorage.deleteItemByOwner(id);
    }

    private void checkEmail(String email, int id) {
        List<User> userFilter = userStorage.getAllUsers().stream()
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
