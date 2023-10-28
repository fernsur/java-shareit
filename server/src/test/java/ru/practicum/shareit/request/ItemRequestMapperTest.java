package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
public class ItemRequestMapperTest {

    @Test
    public void shouldUserToUserDto() {
        User user = new User(
                1,
                "Test",
                "test@mail.ru");

        LocalDateTime date = LocalDateTime.now();
        ItemRequest itemRequest = new ItemRequest(
                1,
                "test",
                user,
                date);

        ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequest, Collections.emptyList());

        assertEquals(itemRequest.getId(), itemRequestDto.getId());
        assertEquals(itemRequest.getDescription(), itemRequestDto.getDescription());
    }

    @Test
    public void shouldUserDtoToUser() {
        User user = new User(
                1,
                "Test",
                "test@mail.ru");

        LocalDateTime date = LocalDateTime.now();
        ItemRequestDto itemRequestDto = new ItemRequestDto(
                1,
                "test",
                date,
                Collections.emptyList());

        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestDto, user);

        assertEquals(itemRequestDto.getId(), itemRequest.getId());
        assertEquals(itemRequestDto.getDescription(), itemRequest.getDescription());
    }
}
