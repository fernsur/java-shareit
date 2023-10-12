package ru.practicum.shareit.item;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;
    private static final String USER_ID = "X-Sharer-User-Id";

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/{itemId}")
    public ItemDto itemById(@PathVariable int itemId,
                            @RequestHeader(USER_ID) int userId) {
        log.info("Получен GET-запрос к эндпоинту /items/{itemId} на получение вещи по id.");
        return itemService.itemById(itemId, userId);
    }

    @GetMapping()
    public List<ItemDto> allItemsByOwner(@RequestHeader(USER_ID) int ownerId) {
        log.info("Получен GET-запрос к эндпоинту /items на получение всех вещей владельца по id.");
        return itemService.allItemsByOwner(ownerId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItem(@RequestParam String text) {
        log.info("Получен GET-запрос к эндпоинту /items/search на поиск вещи.");
        return itemService.searchItem(text);
    }

    @ResponseBody
    @PostMapping()
    public ItemDto createItem(@Valid @RequestBody ItemDto itemDto,
                              @RequestHeader(USER_ID) int ownerId) {
        log.info("Получен POST-запрос к эндпоинту /items на добавление вещи.");
        return itemService.createItem(itemDto, ownerId);
    }

    @ResponseBody
    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestBody ItemDto itemDto,
                              @PathVariable int itemId,
                              @RequestHeader(USER_ID) int ownerId) {
        log.info("Получен PATCH-запрос к эндпоинту /items/{itemId} на обновление вещи по id.");
        itemDto.setId(itemId);
        return itemService.updateItem(itemDto, ownerId);
    }

    @ResponseBody
    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@Valid @RequestBody CommentDto commentDto,
                                 @PathVariable int itemId,
                                 @RequestHeader(USER_ID) int userId) {
        log.info("Получен POST-запрос к эндпоинту /items/{itemId}/comment на добавление комментария.");
        return itemService.addComment(commentDto, itemId, userId);
    }
}
