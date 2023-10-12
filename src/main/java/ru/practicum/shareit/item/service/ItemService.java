package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    ItemDto itemById(int id, int userId);

    List<ItemDto> allItemsByOwner(int id);

    List<ItemDto> searchItem(String text);

    ItemDto createItem(ItemDto itemDto, int ownerId);

    ItemDto updateItem(ItemDto itemDto, int ownerId);

    CommentDto addComment(CommentDto commentDto, int itemId, int userId);

    Item findItemById(int id);
}
