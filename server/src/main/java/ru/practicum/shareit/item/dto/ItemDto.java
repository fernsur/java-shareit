package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {
    private int id;

    private String name;

    private String description;

    private Boolean available;

    @JsonIgnore
    private UserDto owner;

    private Integer requestId;

    private BookingDtoShort lastBooking;

    private BookingDtoShort nextBooking;

    private List<CommentDto> comments;
}
