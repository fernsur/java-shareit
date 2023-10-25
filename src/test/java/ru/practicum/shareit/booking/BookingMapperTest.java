package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDtoInput;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BookingMapperTest {

    @Test
    public void shouldBookingToBookingDtoShort() {
        LocalDateTime start = LocalDateTime.now().plusHours(20);
        LocalDateTime end = LocalDateTime.now().plusHours(40);

        BookingDtoInput bookingDtoInput = new BookingDtoInput(
                1,
                start,
                end);

        User user = new User(
                1,
                "Test",
                "test@mail.ru");

        User user2 = new User(
                2,
                "Testtt",
                "testtt@mail.ru");

        Item item = new Item(
                1,
                "Test",
                "Test description",
                true,
                user2,
                null);

        Booking booking = BookingMapper.toBooking(bookingDtoInput, item, user);
        BookingDtoShort bookingDtoShort = BookingMapper.toBookingDtoShort(booking);

        assertEquals(booking.getStart(), bookingDtoShort.getStart());
        assertEquals(booking.getEnd(), bookingDtoShort.getEnd());
    }
}
