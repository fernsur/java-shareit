package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto userById(int id);

    List<UserDto> allUsers();

    UserDto createUser(UserDto userDto);

    UserDto updateUser(UserDto userDto, int id);

    void deleteUser(int id);
}
