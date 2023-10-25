package ru.practicum.shareit.request.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;

import ru.practicum.shareit.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Autowired
    public ItemRequestServiceImpl(ItemRequestRepository requestRepository, UserRepository userRepository,
                                  ItemRepository itemRepository) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    public ItemRequestDto createItemRequest(ItemRequestDto requestDto, int requesterId) {
        User user = findUserById(requesterId);
        ItemRequest request = ItemRequestMapper.toItemRequest(requestDto, user);
        return ItemRequestMapper.toItemRequestDto(requestRepository.save(request), null);
    }

    @Override
    public List<ItemRequestDto> getItemRequestsByOwnerId(int requesterId) {
        findUserById(requesterId);
        return requestRepository.findAllByRequesterIdOrderByCreatedDesc(requesterId)
                .stream()
                .map(request -> ItemRequestMapper.toItemRequestDto(request, getItems(request.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> allItemRequests(int userId, int from, int size) {
        if (from < 0 || size <= 0) {
            String warning = "Индекс или количество эллементов не могут быть отрицательными.";
            log.warn(warning);
            throw new ValidationException(warning);
        }
        findUserById(userId);

        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);
        return requestRepository.findAllByRequesterIdNot(userId, page)
                .stream()
                .map(request -> ItemRequestMapper.toItemRequestDto(request, getItems(request.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto itemRequestById(int requestId, int requesterId) {
        findUserById(requesterId);
        return ItemRequestMapper.toItemRequestDto(requestRepository.findById(requestId)
                .orElseThrow(() -> new ItemRequestNotFoundException("Невозможно найти. Такого запроса на вещь нет.")),
                getItems(requestId));
    }

    private User findUserById(int id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Невозможно найти. Такого пользователя нет."));
    }

    private List<ItemDto> getItems(int requestId) {
        return itemRepository
                .findAllByRequestId(requestId)
                .stream()
                .map(item -> ItemMapper.toItemDtoShort(item, null))
                .collect(Collectors.toList());
    }
}
