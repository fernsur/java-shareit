package ru.practicum.shareit.user.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.storage.UserStorage;

@Service
public class UserService {
    private final UserStorage userStorage;
    private final UserMapper userMapper;

    @Autowired
    public UserService(UserStorage userStorage, UserMapper userMapper) {
        this.userStorage = userStorage;
        this.userMapper = userMapper;
    }

    public UserDto userById(int id) {
        return userMapper.toUserDto(userStorage.getById(id));
    }

    public List<UserDto> allUsers() {
        return userStorage.getAllUsers()
                .stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
    }

    public UserDto createUser(UserDto userDto) {
        User user = userMapper.toUser(userDto);
        return userMapper.toUserDto(userStorage.createUser(user));
    }

    public UserDto updateUser(UserDto userDto, int id) {
        userDto.setId(id);
        User user = userMapper.toUser(userDto);
        return userMapper.toUserDto(userStorage.updateUser(user));
    }

    public void deleteUser(int id) {
        userStorage.deleteUser(id);
    }
}
