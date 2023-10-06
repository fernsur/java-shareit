package ru.practicum.shareit.booking.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDtoInput;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.BookingNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemService itemService;

    @Autowired
    public BookingServiceImpl(BookingRepository bookingRepository, UserService userService, ItemService itemService) {
        this.bookingRepository = bookingRepository;
        this.userService = userService;
        this.itemService = itemService;
    }

    @Override
    public BookingDto createBooking(BookingDtoInput bookingDto, int bookerId) {
        LocalDateTime start = bookingDto.getStart();
        LocalDateTime end = bookingDto.getEnd();
        if(start.isAfter(end) || start.isEqual(end)) {
            String warning = "Время окончания бронирования должно быть после начала.";
            log.warn(warning);
            throw new ValidationException(warning);
        }

        User user = userService.findUserById(bookerId);
        int itemId = bookingDto.getItemId();
        Item item = itemService.findItemById(itemId);
        boolean available = item.getAvailable();

        if (bookerId == item.getOwner().getId()) {
            String warning = "Владелец не может забронировать свою же вещь.";
            log.warn(warning);
            throw new UserNotFoundException(warning);
        }

        if(!available) {
            String warning = "Вещь с id = " + itemId + " недоступна для бронирования";
            log.warn(warning);
            throw new ValidationException(warning);
        }
        Booking booking = BookingMapper.toBooking(bookingDto, item, user);

        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto updateBooking(int bookingId, int ownerId, Boolean approved) {
        userService.findUserById(ownerId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Невозможно получить. Такого бронирования нет."));
        int id = booking.getItem().getOwner().getId();

        if (ownerId != id) {
            String warning = "Подтвердить бронирование может только владелец вещи.";
            log.warn(warning);
            throw new UserNotFoundException(warning);
        }

        if (!booking.getStatus().equals(Status.WAITING)) {
            String warning = "Бронирование уже подтверждено.";
            log.warn(warning);
            throw new ValidationException(warning);
        }

        if (approved) {
            booking.setStatus(Status.APPROVED);
        } else {
            booking.setStatus(Status.REJECTED);
        }

        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto getBookingById(int bookingId, int userId) {
        userService.findUserById(userId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Невозможно получить. Такого бронирования нет."));
        int bookerId = booking.getBooker().getId();
        int ownerId = booking.getItem().getOwner().getId();

        if (bookerId == userId || ownerId == userId) {
            return BookingMapper.toBookingDto(booking);
        } else {
            String warning = "Просмотр бронирования доступен только забронировавшему вещь или ее владельцу.";
            log.warn(warning);
            throw new BookingNotFoundException(warning);
        }
    }

    @Override
    public List<BookingDto> getBookings(String state, int userId) {
        userService.findUserById(userId);
        List<Booking> bookings;

        switch (state) {
            case "ALL":
                bookings = bookingRepository.findByBookerIdOrderByStartDesc(userId);
                break;
            case "CURRENT":
                bookings = bookingRepository.findByBookerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(userId,
                        LocalDateTime.now(), LocalDateTime.now());
                break;
            case "PAST":
                bookings = bookingRepository.findByBookerIdAndEndIsBeforeOrderByStartDesc(userId, LocalDateTime.now());
                break;
            case "FUTURE":
                bookings = bookingRepository.findByBookerIdAndStartIsAfterOrderByStartDesc(userId, LocalDateTime.now());
                break;
            case "WAITING":
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, Status.WAITING);
                break;
            case "REJECTED":
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, Status.REJECTED);
                break;
            default:
                String warning = "Unknown state: ";
                log.warn(warning);
                throw new ValidationException(warning + state);
        }

        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getBookingsOwner(String state, int userId) {
        userService.findUserById(userId);
        List<Booking> bookings;

        switch (state) {
            case "ALL":
                bookings = bookingRepository.findByItemOwnerIdOrderByStartDesc(userId);
                break;
            case "CURRENT":
                bookings = bookingRepository.findByItemOwnerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(userId,
                        LocalDateTime.now(), LocalDateTime.now());
                break;
            case "PAST":
                bookings = bookingRepository.findByItemOwnerIdAndEndIsBeforeOrderByStartDesc(userId,
                        LocalDateTime.now());
                break;
            case "FUTURE":
                bookings = bookingRepository.findByItemOwnerIdAndStartIsAfterOrderByStartDesc(userId,
                        LocalDateTime.now());
                break;
            case "WAITING":
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, Status.WAITING);
                break;
            case "REJECTED":
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, Status.REJECTED);
                break;
            default:
                String warning = "Unknown state: ";
                log.warn(warning);
                throw new ValidationException(warning + state);
        }

        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }
}
