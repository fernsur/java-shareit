package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BookingDtoShort {
    private int id;

    private int bookerId;

    private LocalDateTime start;

    private LocalDateTime end;
}
