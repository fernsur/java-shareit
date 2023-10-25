package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.List;

public class ItemMapper {

    public static ItemDto toItemDto(Item item, Booking lastBooking, Booking nextBooking, List<CommentDto> comments) {
        ItemDto dto = new ItemDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.getAvailable());
        dto.setOwner(UserMapper.toUserDto(item.getOwner()));
        dto.setRequestId(item.getRequestId() == null ? null : item.getRequestId());
        if (lastBooking != null) {
            dto.setLastBooking(BookingMapper.toBookingDtoShort(lastBooking));
        }
        if (nextBooking != null) {
            dto.setNextBooking(BookingMapper.toBookingDtoShort(nextBooking));
        }
        if (comments != null) {
            dto.setComments(comments);
        }
        return dto;
    }

    public static ItemDto toItemDtoShort(Item item, List<CommentDto> comments) {
        ItemDto dto = new ItemDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.getAvailable());
        dto.setOwner(UserMapper.toUserDto(item.getOwner()));
        if (comments != null) {
            dto.setComments(comments);
        }
        dto.setRequestId(item.getRequestId() == null ? null : item.getRequestId());
        return dto;
    }

    public static Item toItem(ItemDto itemDto) {
        return new Item(itemDto.getId(),
                        itemDto.getName(),
                        itemDto.getDescription(),
                        itemDto.getAvailable(),
                        UserMapper.toUser(itemDto.getOwner()),
                        itemDto.getRequestId() == null ? null : itemDto.getRequestId());
    }

    public static CommentDto toCommentDto(Comment comment) {
       return new CommentDto(comment.getId(),
                             comment.getText(),
                             comment.getAuthor().getName(),
                             comment.getCreated());
    }
}
