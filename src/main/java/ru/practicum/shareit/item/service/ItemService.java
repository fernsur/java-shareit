package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;
    private final ItemMapper itemMapper;

    @Autowired
    public ItemService(ItemStorage itemStorage, UserStorage userStorage, ItemMapper itemMapper) {
        this.itemStorage = itemStorage;
        this.userStorage = userStorage;
        this.itemMapper = itemMapper;
    }

    public ItemDto itemById(int id) {
        return itemMapper.toItemDto(itemStorage.getById(id));
    }

    public List<ItemDto> allItemsByOwner(int id) {
        return itemStorage.getAllItemsByOwner(id)
                .stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    public List<ItemDto> searchItem(String text) {
        text = text.toLowerCase();
        return itemStorage.searchItem(text)
                .stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    public ItemDto createItem(ItemDto itemDto, int ownerId) {
        Item item = itemMapper.toItem(itemDto, ownerId);
        checkUserId(ownerId);
        return itemMapper.toItemDto(itemStorage.createItem(item));
    }

    public ItemDto updateItem(ItemDto itemDto, int itemId, int ownerId) {
        itemDto.setId(itemId);
        checkUserId(ownerId);
        Item item = itemMapper.toItem(itemDto, ownerId);
        return itemMapper.toItemDto(itemStorage.updateItem(item));
    }

    private void checkUserId(int ownerId) {
        if (!userStorage.checkUser(ownerId)) {
            String warning = "Передан неверный идентификатор владельца.";
            log.warn(warning);
            throw new UserNotFoundException(warning);
        }
    }
}
