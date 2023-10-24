package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDtoInput;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
public class ItemServiceTest {
    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    private ItemService itemService;

    private ItemDto itemDto;

    private UserDto userDto;

    private CommentDto commentDto;

    @BeforeEach
    void setUp() {
        userDto = new UserDto(
                1,
                "Test",
                "test@mail.ru");

        itemDto = new ItemDto(
                1,
                "Test",
                "Test description",
                true,
                userDto,
                null,
                null,
                null,
                Collections.emptyList());

        commentDto = new CommentDto(
                1,
                "Test text",
                "Test name",
                LocalDateTime.now());

        itemService = new ItemServiceImpl(itemRepository, userRepository, bookingRepository, commentRepository);
    }

    @Test
    public void shouldCreateItem() {
        Item item = ItemMapper.toItem(itemDto);
        when(userRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(UserMapper.toUser(userDto)));
        when(itemRepository.save(any(Item.class)))
                .thenReturn(item);

        ItemDto createdItem = itemService.createItem(itemDto, 1);

        assertEquals(item.getId(), createdItem.getId());
        assertEquals(item.getName(), createdItem.getName());
        assertEquals(item.getDescription(), createdItem.getDescription());
    }

    @Test
    public void shouldNotCreateItem() {
        assertThrows(UserNotFoundException.class, () -> itemService.createItem(itemDto, 1));
    }

    @Test
    public void shouldUpdateItem() {
        Item item = ItemMapper.toItem(itemDto);
        when(userRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(UserMapper.toUser(userDto)));
        when(itemRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class)))
                .thenReturn(item);

        ItemDto createdItem = itemService.updateItem(itemDto, 1);

        assertEquals(item.getId(), createdItem.getId());
        assertEquals(item.getName(), createdItem.getName());
        assertEquals(item.getDescription(), createdItem.getDescription());
    }

    @Test
    public void shouldNotUpdateItemByNotFoundItem() {
        when(userRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(UserMapper.toUser(userDto)));

        assertThrows(ItemNotFoundException.class, () -> itemService.updateItem(itemDto, 1));
    }

    @Test
    public void shouldUpdateItemByNotOwnerId() {
        Item item = ItemMapper.toItem(itemDto);
        when(userRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(UserMapper.toUser(userDto)));
        when(itemRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(item));

        assertThrows(UserNotFoundException.class, () -> itemService.updateItem(itemDto, 2));
    }

    @Test
    public void shouldAddComment() {
        Item item = ItemMapper.toItem(itemDto);
        User user = UserMapper.toUser(userDto);
        BookingDtoInput bookingDtoInput = new BookingDtoInput(
                1,
                LocalDateTime.now().plusHours(20),
                LocalDateTime.now().plusHours(40));
        Booking booking = BookingMapper.toBooking(bookingDtoInput, item, user);
        Comment comment = new Comment(1, "Test text", item, user, LocalDateTime.now());

        when(userRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(user));
        when(itemRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(item));
        when(bookingRepository.findFirstByItemIdAndBookerIdAndEndIsBeforeAndStatus(any(Integer.class),
                any(Integer.class), any(LocalDateTime.class), any(Status.class)))
                .thenReturn(booking);
        when(commentRepository.save(any(Comment.class)))
                .thenReturn(comment);

        CommentDto createdComment = itemService.addComment(commentDto, 1, 1);

        assertEquals(commentDto.getId(), createdComment.getId());
        assertEquals(commentDto.getText(), createdComment.getText());
    }

    @Test
    public void shouldNotAddComment() {
        Item item = ItemMapper.toItem(itemDto);
        User user = UserMapper.toUser(userDto);

        when(userRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(user));
        when(itemRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(item));
        when(bookingRepository.findFirstByItemIdAndBookerIdAndEndIsBeforeAndStatus(any(Integer.class),
                any(Integer.class), any(LocalDateTime.class), any(Status.class)))
                .thenReturn(null);

        assertThrows(ValidationException.class, () -> itemService.addComment(commentDto, 1, 1));
    }

    @Test
    public void shouldGetItem() {
        Item item = ItemMapper.toItem(itemDto);
        when(itemRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(item));
        when(commentRepository.findAllByItemIdOrderByCreated(any(Integer.class)))
                .thenReturn(Collections.emptyList());
        when(bookingRepository.findFirstByItemIdAndStartIsBeforeAndStatusOrderByStartDesc(any(Integer.class),
                any(LocalDateTime.class), any(Status.class)))
                .thenReturn(null);
        when(bookingRepository.findFirstByItemIdAndStartIsAfterAndStatusOrderByStartAsc(any(Integer.class), any(LocalDateTime.class), any(Status.class)))
                .thenReturn(null);

        ItemDto createdItem = itemService.itemById(1, 1);

        assertEquals(item.getId(), createdItem.getId());
        assertEquals(item.getName(), createdItem.getName());
        assertEquals(item.getDescription(), createdItem.getDescription());
    }

    @Test
    public void shouldGetItemByNotOwnerId() {
        Item item = ItemMapper.toItem(itemDto);
        when(itemRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(item));
        when(commentRepository.findAllByItemIdOrderByCreated(any(Integer.class)))
                .thenReturn(Collections.emptyList());

        ItemDto createdItem = itemService.itemById(1, 2);

        assertEquals(item.getId(), createdItem.getId());
        assertEquals(item.getName(), createdItem.getName());
        assertEquals(item.getDescription(), createdItem.getDescription());
    }

    @Test
    public void shouldNotGetSearchItemByUncorrectedSize() {
        ValidationException ex = assertThrows(
                ValidationException.class, () -> itemService.searchItem("Text", -3, 1));
        assertEquals("Индекс или количество эллементов не могут быть отрицательными.", ex.getMessage());
    }

    @Test
    public void shouldToCommentDto() {
        Item item = ItemMapper.toItem(itemDto);
        User user = UserMapper.toUser(userDto);
        user.setName("Test name");
        Comment comment = new Comment(1, "Test text", item, user, LocalDateTime.now());
        CommentDto dto = ItemMapper.toCommentDto(comment);

        assertEquals(commentDto.getId(), dto.getId());
        assertEquals(commentDto.getText(), dto.getText());
        assertEquals(commentDto.getAuthorName(), dto.getAuthorName());
    }

    @Test
    public void shouldSearchItem() {
        when(itemRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndAvailableIsTrue(
                any(String.class), any(String.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        List<ItemDto> searchItems = itemService.searchItem("Text", 1, 2);

        verify(itemRepository, times(1))
                .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndAvailableIsTrue(
                        any(String.class), any(String.class), any(Pageable.class));
        assertEquals(0, searchItems.size());
    }

    @Test
    public void shouldSearchItemByTextEmpty() {
        List<ItemDto> searchItems = itemService.searchItem("", 1, 2);
        assertEquals(0, searchItems.size());
    }

    @Test
    public void shouldAllItemsByOwner() {
        Item item = ItemMapper.toItem(itemDto);
        List<Item> items = new ArrayList<>();
        items.add(item);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Item> page = new PageImpl<>(items, pageable, items.size());

        when(itemRepository.findAllByOwnerId(any(Integer.class), any(Pageable.class)))
                .thenReturn(page);
        when(bookingRepository.findFirstByItemIdAndStartIsBeforeAndStatusOrderByStartDesc(
                any(Integer.class), any(LocalDateTime.class), any(Status.class)))
                .thenReturn(null);
        when(bookingRepository.findFirstByItemIdAndStartIsAfterAndStatusOrderByStartAsc(
                any(Integer.class), any(LocalDateTime.class), any(Status.class)))
                .thenReturn(null);
        when(commentRepository.findAllByItemIdOrderByCreated(any(Integer.class)))
                .thenReturn(Collections.emptyList());

        List<ItemDto> itemsByOwner = itemService.allItemsByOwner(1, 1, 2);

        verify(itemRepository, times(1))
                .findAllByOwnerId(any(Integer.class), any(Pageable.class));
        verify(bookingRepository, times(1))
                .findFirstByItemIdAndStartIsBeforeAndStatusOrderByStartDesc(
                        any(Integer.class), any(LocalDateTime.class), any(Status.class));
        verify(bookingRepository, times(1))
                .findFirstByItemIdAndStartIsAfterAndStatusOrderByStartAsc(
                        any(Integer.class), any(LocalDateTime.class), any(Status.class));
        verify(commentRepository, times(1))
                .findAllByItemIdOrderByCreated(any(Integer.class));
        assertEquals(1, itemsByOwner.size());
    }
}
