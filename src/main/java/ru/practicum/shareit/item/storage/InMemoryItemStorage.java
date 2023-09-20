package ru.practicum.shareit.item.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InMemoryItemStorage implements ItemStorage {
    private final Map<Integer, Item> items = new HashMap<>();
    private int nextId = 1;

    @Override
    public Item getById(int id) {
        if (!items.containsKey(id)) {
            String warning = "Невозможно получить. Такой вещи нет.";
            log.warn(warning);
            throw new ItemNotFoundException(warning);
        }
        return items.get(id);
    }

    @Override
    public List<Item> getAllItemsByOwner(int id) {
        return items.values().stream()
                .filter(item -> item.getOwnerId() == id)
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> searchItem(String text) {
        List<Item> searchItems = new ArrayList<>();
        if (!text.isBlank()) {
            searchItems = items.values().stream()
                    .filter(Item::getAvailable)
                    .filter(item -> item.getName().toLowerCase().contains(text) ||
                            item.getDescription().toLowerCase().contains(text))
                    .collect(Collectors.toList());
        }
        return searchItems;
    }

    @Override
    public Item createItem(Item item) {
        item.setId(nextId++);
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item updateItem(Item item) {
        return items.put(item.getId(), item);
    }

    @Override
    public boolean checkItem(int id) {
        return items.containsKey(id);
    }

    @Override
    public void deleteItemByOwner(int id) {
        for (Item item: items.values()) {
           if (item.getOwnerId() == id) {
               int itemId = item.getId();
               items.remove(itemId);
           }
        }
    }
}
