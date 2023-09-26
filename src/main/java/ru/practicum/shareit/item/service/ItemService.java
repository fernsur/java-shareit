package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto itemById(int id);

    List<ItemDto> allItemsByOwner(int id);

    List<ItemDto> searchItem(String text);

    ItemDto createItem(ItemDto itemDto, int ownerId);

    ItemDto updateItem(ItemDto itemDto, int ownerId);
}
