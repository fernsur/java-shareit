package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import ru.practicum.shareit.request.dto.ItemRequestDto;


@Controller
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemRequestController {
    private final ItemRequestClient requestClient;

    private static final String USER_ID = "X-Sharer-User-Id";

    @ResponseBody
    @PostMapping()
    public ResponseEntity<Object> createItemRequest(@Valid @RequestBody ItemRequestDto requestDto,
                                                    @RequestHeader(USER_ID) int requesterId) {
        log.info("Получен POST-запрос к эндпоинту /requests на создание запроса вещи.");
        return requestClient.createItemRequest(requestDto, requesterId);
    }

    @GetMapping()
    public ResponseEntity<Object> getItemRequestsByOwnerId(@RequestHeader(USER_ID) int requesterId) {
        log.info("Получен GET-запрос к эндпоинту /requests на получение списка собственных запросов на вещи.");
        return requestClient.getItemRequestsByOwnerId(requesterId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> allItemRequests(@RequestHeader(USER_ID) int userId,
                                                  @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                                  @Positive @RequestParam(defaultValue = "10") int size) {
        log.info("Получен GET-запрос к эндпоинту /requests/all на получение списка запросов на вещи.");
        return requestClient.allItemRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> itemRequestById(@PathVariable int requestId,
                                                  @RequestHeader(USER_ID) int requesterId) {
        log.info("Получен GET-запрос к эндпоинту /requests/{requestId} на получение запроса на вещь по id.");
        return requestClient.itemRequestById(requestId, requesterId);
    }
}
