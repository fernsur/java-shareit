package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository, UserRepository userRepository,
                           BookingRepository bookingRepository, CommentRepository commentRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    public ItemDto itemById(int itemId, int userId) {
        Item item = findItemById(itemId);
        List<CommentDto> comments = commentsList(itemId);
        if (userId == item.getOwner().getId()) {
            Booking lastBooking = bookingRepository.findFirstByItemIdAndStartIsBeforeAndStatusOrderByStartDesc(itemId,
                    LocalDateTime.now(), Status.APPROVED);
            Booking nextBooking = bookingRepository.findFirstByItemIdAndStartIsAfterAndStatusOrderByStartAsc(itemId,
                    LocalDateTime.now(), Status.APPROVED);
            return ItemMapper.toItemDto(item, lastBooking, nextBooking, comments);
        } else {
            return ItemMapper.toItemDtoShort(item, comments);
        }
    }

    @Override
    public List<ItemDto> allItemsByOwner(int id, int from, int size) {
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);
        return itemRepository.findAllByOwnerId(id, page)
                .stream()
                .map(item -> ItemMapper.toItemDto(item,
                                        bookingRepository
                                                .findFirstByItemIdAndStartIsBeforeAndStatusOrderByStartDesc(item.getId(),
                                                        LocalDateTime.now(), Status.APPROVED),
                                        bookingRepository
                                                .findFirstByItemIdAndStartIsAfterAndStatusOrderByStartAsc(item.getId(),
                                                        LocalDateTime.now(), Status.APPROVED),
                                        commentsList(item.getId())))
                .sorted(Comparator.comparing(ItemDto::getId))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItem(String text, int from, int size) {
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);
        if (!text.isBlank() || !text.isEmpty()) {
            return itemRepository
                    .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndAvailableIsTrue(text, text, page)
                    .stream()
                    .map(item -> ItemMapper.toItemDtoShort(item, null))
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public ItemDto createItem(ItemDto itemDto, int ownerId) {
        Item item = buildItem(itemDto, ownerId);
        return ItemMapper.toItemDtoShort(itemRepository.save(item), null);
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto, int ownerId) {
        int id = itemDto.getId();
        Item item = findItemById(id);
        Item newItem = buildItem(itemDto, ownerId);

        if (ownerId != item.getOwner().getId()) {
            log.warn("Id = {} не соответсвует id владельца", newItem.getOwner().getId());
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

        return ItemMapper.toItemDtoShort(itemRepository.save(item), null);
    }

    @Override
    public CommentDto addComment(CommentDto commentDto, int itemId, int userId) {
        findUserById(userId);
        findItemById(itemId);
        Booking booking = bookingRepository.findFirstByItemIdAndBookerIdAndEndIsBeforeAndStatus(itemId, userId,
                LocalDateTime.now(), Status.APPROVED);
        Comment comment = new Comment();

        if (booking != null) {
            comment.setText(commentDto.getText());
            comment.setItem(booking.getItem());
            comment.setAuthor(booking.getBooker());
            comment.setCreated(LocalDateTime.now());
        } else {
            String warning = "Невозможно оставить комментарий, пользователь не бронировал вещь.";
            log.warn(warning);
            throw new ValidationException(warning);
        }

        return ItemMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    public Item findItemById(int id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Невозможно получить. Такой вещи нет."));
    }

    private Item buildItem(ItemDto itemDto, int ownerId) {
        UserDto owner = findUserById(ownerId);
        itemDto.setOwner(owner);
        return ItemMapper.toItem(itemDto);
    }

    private List<CommentDto> commentsList(int id) {
        return commentRepository.findAllByItemIdOrderByCreated(id)
                .stream()
                .map(ItemMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    private UserDto findUserById(int id) {
        return UserMapper.toUserDto(userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Невозможно найти. Такого пользователя нет.")));
    }
}
