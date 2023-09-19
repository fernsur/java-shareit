package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {
    Item getById(int id);

    List<Item> getAllItemsByOwner(int id);

    List<Item> searchItem(String text);

    Item createItem(Item item);

    Item updateItem(Item item);
}
