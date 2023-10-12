package ru.practicum.shareit.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.exception.UserAlreadyExistsException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.item.repository.ItemRepository;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, ItemRepository itemRepository) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    public UserDto userById(int id) {
        return UserMapper.toUserDto(findUserById(id));
    }

    @Override
    public List<UserDto> allUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        try {
            User user = UserMapper.toUser(userDto);
            return UserMapper.toUserDto(userRepository.save(user));
        } catch (DataIntegrityViolationException e) {
            String warning = "Пользователь с email = " + userDto.getEmail() + " уже существует";
            log.warn(warning);
            throw new UserAlreadyExistsException(warning);
        }
    }

    @Override
    public UserDto updateUser(UserDto userDto, int id) {
        userDto.setId(id);
        User newUser = UserMapper.toUser(userDto);
        User user = findUserById(id);

        if (newUser.getEmail() != null) {
            String email = newUser.getEmail();
            checkEmail(email, id);
            user.setEmail(email);
        }
        if (newUser.getName() != null) {
            String name = newUser.getName();
            user.setName(name);
        }

        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Override
    public void deleteUser(int id) {
        userRepository.deleteById(id);
        itemRepository.deleteAllByOwnerId(id);
    }

    private User findUserById(int id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Невозможно найти. Такого пользователя нет."));
    }

    private void checkEmail(String email, int id) {
        List<User> userFilter = userRepository.findAllByEmail(email)
                .stream()
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
