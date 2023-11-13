package ru.practicum.shareit.booking;

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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;

import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingClient bookingClient;

    private static final String USER_ID = "X-Sharer-User-Id";

    @ResponseBody
    @PostMapping()
    public ResponseEntity<Object> addBooking(@Valid @RequestBody BookItemRequestDto bookingDto,
                                             @RequestHeader(USER_ID) int bookerId) {
        log.info("Получен POST-запрос к эндпоинту /bookings на добавление бронирования.");
        return bookingClient.createBooking(bookingDto, bookerId);
    }

    @ResponseBody
    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> updateBooking(@PathVariable int bookingId,
                                                @RequestHeader(USER_ID) int ownerId,
                                                @RequestParam Boolean approved) {
        log.info("Получен PATCH-запрос к эндпоинту /bookings/{bookingId} на обновление статуса бронирования.");
        return bookingClient.updateBooking(bookingId, ownerId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingById(@PathVariable int bookingId,
                                                 @RequestHeader(USER_ID) int userId) {
        log.info("Получен GET-запрос к эндпоинту /bookings/{bookingId} на получение бронирования по id.");
        return bookingClient.getBookingById(bookingId, userId);
    }

    @GetMapping()
    public ResponseEntity<Object> getBookings(@RequestParam(name = "state", defaultValue = "ALL") String state,
                                             @RequestHeader(USER_ID) int userId,
                                             @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                             @Positive @RequestParam(defaultValue = "10") int size) {
        log.info("Получен GET-запрос к эндпоинту /bookings на получение всех бронирований пользователя");
        BookingState bookingState = BookingState.from(state)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + state));
        return bookingClient.getBookings(bookingState, userId, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getBookingsOwner(@RequestParam(name = "state", defaultValue = "ALL") String state,
                                                   @RequestHeader(USER_ID) int userId,
                                                   @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                                   @Positive @RequestParam(defaultValue = "10") int size) {
        log.info("Получен GET-запрос к эндпоинту /bookings/owner " +
                "на получение бронирований для всех вещей пользователя");
        BookingState bookingState = BookingState.from(state)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + state));
        return bookingClient.getBookingsOwner(bookingState, userId, from, size);
    }
}
