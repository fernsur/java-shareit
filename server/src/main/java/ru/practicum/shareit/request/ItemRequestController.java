package ru.practicum.shareit.request;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestService requestService;
    private static final String USER_ID = "X-Sharer-User-Id";

    @Autowired
    public ItemRequestController(ItemRequestService requestService) {
        this.requestService = requestService;
    }

    @ResponseBody
    @PostMapping()
    public ItemRequestDto createItemRequest(@RequestBody ItemRequestDto requestDto,
                                            @RequestHeader(USER_ID) int requesterId) {
        log.info("Получен POST-запрос к эндпоинту /requests на создание запроса вещи.");
        return requestService.createItemRequest(requestDto, requesterId);
    }

    @GetMapping()
    public List<ItemRequestDto> getItemRequestsByOwnerId(@RequestHeader(USER_ID) int requesterId) {
        log.info("Получен GET-запрос к эндпоинту /requests на получение списка собственных запросов на вещи.");
        return requestService.getItemRequestsByOwnerId(requesterId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> allItemRequests(@RequestHeader(USER_ID) int userId,
                                                @RequestParam(defaultValue = "0") int from,
                                                @RequestParam(defaultValue = "10") int size) {
        log.info("Получен GET-запрос к эндпоинту /requests/all на получение списка запросов на вещи.");
        return requestService.allItemRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto itemRequestById(@PathVariable int requestId,
                                          @RequestHeader(USER_ID) int requesterId) {
        log.info("Получен GET-запрос к эндпоинту /requests/{requestId} на получение запроса на вещь по id.");
        return requestService.itemRequestById(requestId, requesterId);
    }
}
