package ru.practicum.shareit.booking.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDtoInput;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.BookingNotFoundException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Autowired
    public BookingServiceImpl(BookingRepository bookingRepository, UserRepository userRepository,
                              ItemRepository itemRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    public BookingDto createBooking(BookingDtoInput bookingDto, int bookerId) {
        LocalDateTime start = bookingDto.getStart();
        LocalDateTime end = bookingDto.getEnd();
        if (start.isAfter(end) || start.isEqual(end)) {
            String warning = "Время окончания бронирования должно быть после начала.";
            log.warn(warning);
            throw new ValidationException(warning);
        }

        User user = findUserById(bookerId);
        int itemId = bookingDto.getItemId();
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Невозможно получить. Такой вещи нет."));;
        boolean available = item.getAvailable();

        if (bookerId == item.getOwner().getId()) {
            String warning = "Владелец не может забронировать свою же вещь.";
            log.warn(warning);
            throw new UserNotFoundException(warning);
        }

        if (!available) {
            String warning = "Вещь с id = " + itemId + " недоступна для бронирования";
            log.warn(warning);
            throw new ValidationException(warning);
        }
        Booking booking = BookingMapper.toBooking(bookingDto, item, user);

        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto updateBooking(int bookingId, int ownerId, Boolean approved) {
        findUserById(ownerId);
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
        findUserById(userId);
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
    public List<BookingDto> getBookings(String state, int userId, int from, int size) {
        validateSize(from, size);
        findUserById(userId);
        Page<Booking> bookings;
        State st = findState(state);
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);

        switch (st) {
            case ALL:
                bookings = bookingRepository.findByBookerIdOrderByStartDesc(userId, page);
                break;
            case CURRENT:
                bookings = bookingRepository.findByBookerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(userId,
                        LocalDateTime.now(), LocalDateTime.now(), page);
                break;
            case PAST:
                bookings = bookingRepository.findByBookerIdAndEndIsBeforeOrderByStartDesc(userId,
                        LocalDateTime.now(), page);
                break;
            case FUTURE:
                bookings = bookingRepository.findByBookerIdAndStartIsAfterOrderByStartDesc(userId,
                        LocalDateTime.now(), page);
                break;
            case WAITING:
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId,
                        Status.WAITING, page);
                break;
            case REJECTED:
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, Status.REJECTED, page);
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
    public List<BookingDto> getBookingsOwner(String state, int userId, int from, int size) {
        validateSize(from, size);
        findUserById(userId);
        Page<Booking> bookings;
        State st = findState(state);
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);

        switch (st) {
            case ALL:
                bookings = bookingRepository.findByItemOwnerIdOrderByStartDesc(userId, page);
                break;
            case CURRENT:
                bookings = bookingRepository.findByItemOwnerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(userId,
                        LocalDateTime.now(), LocalDateTime.now(), page);
                break;
            case PAST:
                bookings = bookingRepository.findByItemOwnerIdAndEndIsBeforeOrderByStartDesc(userId,
                        LocalDateTime.now(), page);
                break;
            case FUTURE:
                bookings = bookingRepository.findByItemOwnerIdAndStartIsAfterOrderByStartDesc(userId,
                        LocalDateTime.now(), page);
                break;
            case WAITING:
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, Status.WAITING, page);
                break;
            case REJECTED:
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, Status.REJECTED, page);
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

    private User findUserById(int id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Невозможно найти. Такого пользователя нет."));
    }

    private State findState(String line) {
        try {
            return State.valueOf(line);
        } catch (IllegalArgumentException e) {
            return State.UNKNOWN;
        }
    }

    private void validateSize(int from, int size) {
        if (from < 0 || size <= 0) {
            String warning = "Индекс или количество эллементов не могут быть отрицательными.";
            log.warn(warning);
            throw new ValidationException(warning);
        }
    }
}
