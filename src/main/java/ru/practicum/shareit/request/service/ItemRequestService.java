package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto createItemRequest(ItemRequestDto requestDto, int requesterId);

    List<ItemRequestDto> getItemRequestsByOwnerId(int requesterId);

    List<ItemRequestDto> allItemRequests(int requesterId, int from, int size);

    ItemRequestDto itemRequestById(int requestId, int requesterId);
}
