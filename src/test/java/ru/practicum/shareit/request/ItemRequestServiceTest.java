package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestServiceTest {
    @Mock
    private ItemRequestRepository requestRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    private ItemRequestService requestService;

    private ItemRequestDto requestDto;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(
                1,
                "Test",
                "test@mail.ru");

        LocalDateTime date = LocalDateTime.now();
        requestDto = new ItemRequestDto(
                1,
                "test",
                date,
                Collections.emptyList());

        requestService = new ItemRequestServiceImpl(requestRepository, userRepository, itemRepository);
    }

    @Test
    public void shouldCreateItemRequest() {
        when(userRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(user));
        when(requestRepository.save(any(ItemRequest.class)))
                .thenReturn(ItemRequestMapper.toItemRequest(requestDto, user));

        ItemRequestDto createdRequest = requestService.createItemRequest(requestDto, 1);

        assertEquals(requestDto.getId(), createdRequest.getId());
        assertEquals(requestDto.getDescription(), createdRequest.getDescription());
    }

    @Test
    public void shouldNotCreateItemRequest() {
        when(requestRepository.save(any(ItemRequest.class)))
                .thenThrow(new UserNotFoundException("Невозможно найти. Такого пользователя нет."));

        assertThrows(UserNotFoundException.class, () -> requestService.createItemRequest(requestDto, 1));
    }

    @Test
    public void shouldGetItemRequest() {
        when(userRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(user));
        when(requestRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(ItemRequestMapper.toItemRequest(requestDto, user)));

        ItemRequestDto request = requestService.itemRequestById(1, 1);

        assertEquals(requestDto.getId(), request.getId());
        assertEquals(requestDto.getDescription(), request.getDescription());
    }

    @Test
    public void shouldNotGetItemRequest() {
        when(userRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(user));
        when(requestRepository.findById(any(Integer.class)))
                .thenThrow(new ItemRequestNotFoundException("Невозможно найти. Такого запроса на вещь нет."));

        assertThrows(ItemRequestNotFoundException.class, () -> requestService.itemRequestById(1, 1));
    }

    @Test
    public void shouldGetOwnItemRequests() {
        when(userRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(user));
        when(requestRepository.findAllByRequesterIdOrderByCreatedDesc(any(Integer.class)))
                .thenReturn(List.of(ItemRequestMapper.toItemRequest(requestDto, user)));
        List<ItemRequestDto> saveRequest = requestService.getItemRequestsByOwnerId(1);

        verify(requestRepository, times(1))
                .findAllByRequesterIdOrderByCreatedDesc(1);
        assertEquals(1, saveRequest.size());
    }

    @Test
    public void shouldNotGetAllItemRequests() {
        ValidationException ex = assertThrows(
                ValidationException.class, () -> requestService.allItemRequests(1,-1,3));
        assertEquals("Индекс или количество эллементов не могут быть отрицательными.", ex.getMessage());
    }

    @Test
    public void shouldGetAllItemRequests() {
        when(userRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(user));
        when(requestRepository.findAllByRequesterIdNot(any(Integer.class), any(Pageable.class)))
                .thenReturn(Page.empty());
        when(itemRepository.findAllByRequestId(any(Integer.class)))
                .thenReturn(Collections.emptyList());

        List<ItemRequestDto> saveRequest = requestService.allItemRequests(1,1,1);
        verify(requestRepository, times(1))
                .findAllByRequesterIdNot(any(Integer.class), any(Pageable.class));
        assertEquals(0, saveRequest.size());
    }
}
