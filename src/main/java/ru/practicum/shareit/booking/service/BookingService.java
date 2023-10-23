package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoInput;

import java.util.List;

public interface BookingService {
    BookingDto createBooking(BookingDtoInput bookingDto, int bookerId);

    BookingDto updateBooking(int bookingId, int ownerId, Boolean approved);

    BookingDto getBookingById(int bookingId, int userId);

    List<BookingDto> getBookings(String state, int userId, int from, int size);

    List<BookingDto> getBookingsOwner(String state, int userId, int from, int size);
}
