package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.UserAlreadyExistsException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    private UserService userService;

    private UserDto userDto;

    @BeforeEach
    void setUp() {
        userDto = new UserDto(
                1,
                "Test",
                "test@mail.ru");

        userService = new UserServiceImpl(userRepository, itemRepository);
    }

    @Test
    public void shouldCreateUser() {
        when(userRepository.save(any(User.class)))
                .thenReturn(UserMapper.toUser(userDto));

        UserDto createdUser = userService.createUser(userDto);

        assertEquals(userDto.getName(), createdUser.getName());
        assertEquals(userDto.getEmail(), createdUser.getEmail());
    }

    @Test
    public void shouldNotCreateUser() {
        when(userRepository.save(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("Пользователь с email = " + userDto.getEmail()
                        + " уже существует"));

        assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(userDto));
    }

    @Test
    public void shouldGetUser() {
        when(userRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(UserMapper.toUser(userDto)));
        UserDto saveUser = userService.userById(1);

        verify(userRepository, times(1))
                .findById(1);
        assertEquals(userDto.getId(), saveUser.getId());
        assertEquals(userDto.getName(), saveUser.getName());
        assertEquals(userDto.getEmail(), saveUser.getEmail());
    }

    @Test
    public void shouldGetAllUsers() {
        when(userRepository.findAll())
                .thenReturn(List.of(UserMapper.toUser(userDto)));
        List<UserDto> saveUsers = userService.allUsers();

        verify(userRepository, times(1))
                .findAll();
        assertEquals(1, saveUsers.size());
    }

    @Test
    public void shouldDeleteUser() {
        userService.deleteUser(1);
        Optional<User> deletedUser = userRepository.findById(1);
        assertFalse(deletedUser.isPresent());
    }

    @Test
    public void shouldUpdateUser() {
        when(userRepository.save(any(User.class)))
                .thenReturn(UserMapper.toUser(userDto));
        when(userRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(UserMapper.toUser(userDto)));

        UserDto updatedUser = userService.updateUser(userDto, 1);

        assertEquals(userDto.getName(), updatedUser.getName());
        assertEquals(userDto.getEmail(), updatedUser.getEmail());
    }

    @Test
    public void shouldNotUpdateUser() {
        User user = new User(
                3,
                "Test",
                "test@mail.ru");
        when(userRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(UserMapper.toUser(userDto)));
        when(userRepository.findAllByEmail(any(String.class)))
                .thenReturn(List.of(user));

        assertThrows(UserAlreadyExistsException.class, () -> userService.updateUser(userDto, 1));
    }
}
