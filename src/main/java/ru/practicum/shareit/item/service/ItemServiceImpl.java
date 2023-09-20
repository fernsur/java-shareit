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
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Autowired
    public ItemServiceImpl(ItemStorage itemStorage, UserStorage userStorage) {
        this.itemStorage = itemStorage;
        this.userStorage = userStorage;
    }

    @Override
    public ItemDto itemById(int id) {
        return ItemMapper.toItemDto(itemStorage.getById(id));
    }

    @Override
    public List<ItemDto> allItemsByOwner(int id) {
        return itemStorage.getAllItemsByOwner(id)
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItem(String text) {
        text = text.toLowerCase();
        return itemStorage.searchItem(text)
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto createItem(ItemDto itemDto, int ownerId) {
        Item item = ItemMapper.toItem(itemDto, ownerId);
        checkUserId(ownerId);
        return ItemMapper.toItemDto(itemStorage.createItem(item));
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto, int ownerId) {
        int id = itemDto.getId();
        if (!itemStorage.checkItem(id)) {
            log.warn("Вещь с id = {} не существует", id);
            throw new UserNotFoundException("Такой вещи нет.");
        }
        checkUserId(ownerId);

        Item newItem = ItemMapper.toItem(itemDto, ownerId);
        Item item = itemStorage.getById(id);

        if (newItem.getOwnerId() != item.getOwnerId()) {
            log.warn("Id = {} не соответсвует id владельца", newItem.getOwnerId());
            throw new UserNotFoundException("Только владелец имеет право на редактирование.");
        }

        if (newItem.getName() != null) {
            String name = newItem.getName();
            item.setName(name);
        }
        if (newItem.getDescription() != null) {
            String description = newItem.getDescription();
            item.setDescription(description);
        }
        if (newItem.getAvailable() != null) {
            Boolean available = newItem.getAvailable();
            item.setAvailable(available);
        }

        return ItemMapper.toItemDto(itemStorage.updateItem(item));
    }

    private void checkUserId(int ownerId) {
        if (!userStorage.checkUser(ownerId)) {
            String warning = "Передан неверный идентификатор владельца.";
            log.warn(warning);
            throw new UserNotFoundException(warning);
        }
    }
}
