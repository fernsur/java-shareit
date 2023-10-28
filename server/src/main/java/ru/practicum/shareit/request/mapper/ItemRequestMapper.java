package ru.practicum.shareit.request.mapper;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

public class ItemRequestMapper {

    public static ItemRequestDto toItemRequestDto(ItemRequest request, List<ItemDto> items) {
        return new ItemRequestDto(request.getId(),
                request.getDescription(),
                request.getCreated(),
                items);
    }

    public static ItemRequest toItemRequest(ItemRequestDto requestDto, User requester) {
        return new ItemRequest(requestDto.getId(),
                requestDto.getDescription(),
                requester,
                LocalDateTime.now());
    }
}
