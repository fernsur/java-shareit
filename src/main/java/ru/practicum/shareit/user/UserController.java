package ru.practicum.shareit.user;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}")
    public UserDto userById(@PathVariable int userId) {
        log.info("Получен GET-запрос к эндпоинту /users/{userId} на получение пользователя по id.");
        return userService.userById(userId);
    }

    @GetMapping()
    public List<UserDto> allUsers() {
        log.info("Получен GET-запрос к эндпоинту /users на получение всех пользователей.");
        return userService.allUsers();
    }

    @ResponseBody
    @PostMapping()
    public UserDto createUser(@Valid @RequestBody UserDto userDto) {
        log.info("Получен POST-запрос к эндпоинту /users на добавление пользователя.");
        return userService.createUser(userDto);
    }

    @ResponseBody
    @PatchMapping("/{userId}")
    public UserDto updateUser(@RequestBody UserDto userDto, @PathVariable int userId) {
        log.info("Получен PATCH-запрос к эндпоинту /users/{userId} на обновление пользователя по id.");
        return userService.updateUser(userDto, userId);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable int userId) {
        log.info("Получен DELETE-запрос к эндпоинту /users/{userId} на удаление пользователя.");
        userService.deleteUser(userId);
    }
}
