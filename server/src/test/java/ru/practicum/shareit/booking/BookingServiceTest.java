package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoInput;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.exception.BookingNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
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
public class BookingServiceTest {
    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    private BookingService bookingService;

    private BookingDtoInput bookingDtoInput;

    private User user;

    private Item item;

    @BeforeEach
    void setUp() {
        bookingDtoInput = new BookingDtoInput(
                1,
                LocalDateTime.now().plusHours(20),
                LocalDateTime.now().plusHours(40));

        user = new User(
                1,
                "Test",
                "test@mail.ru");

        User user2 = new User(
                2,
                "Testtt",
                "testtt@mail.ru");

        item = new Item(
                1,
                "Test",
                "Test description",
                true,
                user2,
                null);

        bookingService = new BookingServiceImpl(bookingRepository, userRepository, itemRepository);
    }

    @Test
    public void shouldCreateBooking() {
        Booking booking = BookingMapper.toBooking(bookingDtoInput, item, user);
        BookingDto bookingDto = BookingMapper.toBookingDto(booking);

        when(userRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(user));
        when(itemRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(booking);

        BookingDto createdBooking = bookingService.createBooking(bookingDtoInput, 1);

        assertEquals(bookingDto.getStart(), createdBooking.getStart());
        assertEquals(bookingDto.getEnd(), createdBooking.getEnd());
    }

    @Test
    public void shouldNotCreateBookingByNotFoundUser() {
        assertThrows(UserNotFoundException.class, () -> bookingService.createBooking(bookingDtoInput, 1));
    }

    @Test
    public void shouldNotCreateBookingByNotFoundItem() {
        when(userRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(user));
        assertThrows(ItemNotFoundException.class, () -> bookingService.createBooking(bookingDtoInput, 1));
    }

    @Test
    public void shouldNotCreateBookingByStartAfterEnd() {
        bookingDtoInput.setStart(bookingDtoInput.getEnd().plusHours(21));
        ValidationException ex = assertThrows(
                ValidationException.class, () -> bookingService.createBooking(bookingDtoInput, 1));
        assertEquals("Время окончания бронирования должно быть после начала.", ex.getMessage());
    }

    @Test
    public void shouldNotCreateBookingByBookerIdEqualOwnerId() {
        item.setOwner(user);
        when(userRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(user));
        when(itemRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(item));

        UserNotFoundException ex = assertThrows(
                UserNotFoundException.class, () -> bookingService.createBooking(bookingDtoInput, 1));
        assertEquals("Владелец не может забронировать свою же вещь.", ex.getMessage());
    }

    @Test
    public void shouldNotCreateBookingByAvailableFalse() {
        item.setAvailable(false);
        when(userRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(user));
        when(itemRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(item));

        ValidationException ex = assertThrows(
                ValidationException.class, () -> bookingService.createBooking(bookingDtoInput, 1));
        assertEquals("Вещь с id = 1 недоступна для бронирования", ex.getMessage());
    }

    @Test
    public void shouldUpdateBookingApproved() {
        Booking booking = BookingMapper.toBooking(bookingDtoInput, item, user);
        BookingDto bookingDto = BookingMapper.toBookingDto(booking);

        when(userRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(user));
        when(bookingRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(booking);

        BookingDto createdBooking = bookingService.updateBooking(1,2,true);

        assertEquals(bookingDto.getStart(), createdBooking.getStart());
        assertEquals(bookingDto.getEnd(), createdBooking.getEnd());
    }

    @Test
    public void shouldUpdateBookingRejected() {
        Booking booking = BookingMapper.toBooking(bookingDtoInput, item, user);
        BookingDto bookingDto = BookingMapper.toBookingDto(booking);

        when(userRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(user));
        when(bookingRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(booking);

        BookingDto createdBooking = bookingService.updateBooking(1,2,false);

        assertEquals(bookingDto.getStart(), createdBooking.getStart());
        assertEquals(bookingDto.getEnd(), createdBooking.getEnd());
    }

    @Test
    public void shouldNotUpdateBookingByNotFoundBooking() {
        when(userRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(user));
        assertThrows(BookingNotFoundException.class, () ->
                bookingService.updateBooking(1,1,true));
    }

    @Test
    public void shouldNotUpdateBookingByNotOwnerId() {
        Booking booking = BookingMapper.toBooking(bookingDtoInput, item, user);

        when(userRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(user));
        when(bookingRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(booking));

        UserNotFoundException ex = assertThrows(
                UserNotFoundException.class, () -> bookingService.updateBooking(1,3,true));
        assertEquals("Подтвердить бронирование может только владелец вещи.", ex.getMessage());
    }

    @Test
    public void shouldNotUpdateBookingByRepeatApproved() {
        Booking booking = BookingMapper.toBooking(bookingDtoInput, item, user);
        booking.setStatus(Status.APPROVED);

        when(userRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(user));
        when(bookingRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(booking));

        ValidationException ex = assertThrows(
                ValidationException.class, () -> bookingService.updateBooking(1,2,true));
        assertEquals("Бронирование уже подтверждено.", ex.getMessage());
    }

    @Test
    public void shouldGetBookingById() {
        Booking booking = BookingMapper.toBooking(bookingDtoInput, item, user);
        BookingDto bookingDto = BookingMapper.toBookingDto(booking);

        when(userRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(user));
        when(bookingRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(booking));

        BookingDto getBooking = bookingService.getBookingById(1, 2);

        assertEquals(bookingDto.getStart(), getBooking.getStart());
        assertEquals(bookingDto.getEnd(), getBooking.getEnd());
    }

    @Test
    public void shouldNotGetBookingByIdByNotOwnerId() {
        Booking booking = BookingMapper.toBooking(bookingDtoInput, item, user);

        when(userRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(user));
        when(bookingRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(booking));

        BookingNotFoundException ex = assertThrows(
                BookingNotFoundException.class, () -> bookingService.getBookingById(1, 3));
        assertEquals("Просмотр бронирования доступен только забронировавшему вещь или ее владельцу.",
                ex.getMessage());
    }

    @Test
    public void shouldGetBookingsStateAll() {
        when(userRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerId(any(Integer.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        List<BookingDto> getBookings = bookingService.getBookings("ALL", 1, 1, 1);

        verify(bookingRepository, times(1))
                .findByBookerId(any(Integer.class), any(Pageable.class));
        assertEquals(0, getBookings.size());
    }

    @Test
    public void shouldGetBookingsStateCurrent() {
        when(userRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerIdAndStartIsBeforeAndEndIsAfter(any(Integer.class),
                any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        List<BookingDto> getBookings = bookingService.getBookings("CURRENT", 1, 1, 1);

        verify(bookingRepository, times(1))
                .findByBookerIdAndStartIsBeforeAndEndIsAfter(any(Integer.class),
                        any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class));
        assertEquals(0, getBookings.size());
    }

    @Test
    public void shouldGetBookingsStatePast() {
        when(userRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerIdAndEndIsBefore(any(Integer.class),
                any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        List<BookingDto> getBookings = bookingService.getBookings("PAST", 1, 1, 1);

        verify(bookingRepository, times(1))
                .findByBookerIdAndEndIsBefore(any(Integer.class),
                        any(LocalDateTime.class), any(Pageable.class));
        assertEquals(0, getBookings.size());
    }

    @Test
    public void shouldGetBookingsStateFuture() {
        when(userRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerIdAndStartIsAfter(any(Integer.class),
                any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        List<BookingDto> getBookings = bookingService.getBookings("FUTURE", 1, 1, 1);

        verify(bookingRepository, times(1))
                .findByBookerIdAndStartIsAfter(any(Integer.class),
                        any(LocalDateTime.class), any(Pageable.class));
        assertEquals(0, getBookings.size());
    }

    @Test
    public void shouldGetBookingsStateWaiting() {
        when(userRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerIdAndStatus(any(Integer.class),
                any(Status.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        List<BookingDto> getBookings = bookingService.getBookings("WAITING", 1, 1, 1);

        verify(bookingRepository, times(1))
                .findByBookerIdAndStatus(any(Integer.class),
                        any(Status.class), any(Pageable.class));
        assertEquals(0, getBookings.size());
    }

    @Test
    public void shouldGetBookingsStateRejected() {
        when(userRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerIdAndStatus(any(Integer.class),
                any(Status.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        List<BookingDto> getBookings = bookingService.getBookings("REJECTED", 1, 1, 1);

        verify(bookingRepository, times(1))
                .findByBookerIdAndStatus(any(Integer.class),
                        any(Status.class), any(Pageable.class));
        assertEquals(0, getBookings.size());
    }

    @Test
    public void shouldGetBookingsStateUnknown() {
        when(userRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(user));

        ValidationException ex = assertThrows(
                ValidationException.class, () -> bookingService.getBookings("GGGG", 1, 1, 1));
        assertEquals("Unknown state: GGGG", ex.getMessage());
    }

    @Test
    public void shouldGetBookingsOwnerStateAll() {
        when(userRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(user));
        when(bookingRepository.findByItemOwnerId(any(Integer.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        List<BookingDto> getBookingsOwner = bookingService.getBookingsOwner("ALL", 1, 1, 1);

        verify(bookingRepository, times(1))
                .findByItemOwnerId(any(Integer.class), any(Pageable.class));
        assertEquals(0, getBookingsOwner.size());
    }

    @Test
    public void shouldGetBookingsOwnerStateCurrent() {
        when(userRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(user));
        when(bookingRepository.findByItemOwnerIdAndStartIsBeforeAndEndIsAfter(any(Integer.class),
                any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        List<BookingDto> getBookingsOwner = bookingService.getBookingsOwner("CURRENT", 1, 1, 1);

        verify(bookingRepository, times(1))
                .findByItemOwnerIdAndStartIsBeforeAndEndIsAfter(any(Integer.class),
                        any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class));
        assertEquals(0, getBookingsOwner.size());
    }

    @Test
    public void shouldGetBookingsOwnerStatePast() {
        when(userRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(user));
        when(bookingRepository.findByItemOwnerIdAndEndIsBefore(any(Integer.class),
                any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        List<BookingDto> getBookingsOwner = bookingService.getBookingsOwner("PAST", 1, 1, 1);

        verify(bookingRepository, times(1))
                .findByItemOwnerIdAndEndIsBefore(any(Integer.class),
                        any(LocalDateTime.class), any(Pageable.class));
        assertEquals(0, getBookingsOwner.size());
    }

    @Test
    public void shouldGetBookingsOwnerStateFuture() {
        when(userRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(user));
        when(bookingRepository.findByItemOwnerIdAndStartIsAfter(any(Integer.class),
                any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        List<BookingDto> getBookingsOwner = bookingService.getBookingsOwner("FUTURE", 1, 1, 1);

        verify(bookingRepository, times(1))
                .findByItemOwnerIdAndStartIsAfter(any(Integer.class),
                        any(LocalDateTime.class), any(Pageable.class));
        assertEquals(0, getBookingsOwner.size());
    }

    @Test
    public void shouldGetBookingsOwnerStateWaiting() {
        when(userRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(user));
        when(bookingRepository.findByItemOwnerIdAndStatus(any(Integer.class),
                any(Status.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        List<BookingDto> getBookingsOwner = bookingService.getBookingsOwner("WAITING", 1, 1, 1);

        verify(bookingRepository, times(1))
                .findByItemOwnerIdAndStatus(any(Integer.class),
                        any(Status.class), any(Pageable.class));
        assertEquals(0, getBookingsOwner.size());
    }

    @Test
    public void shouldGetBookingsOwnerStateRejected() {
        when(userRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(user));
        when(bookingRepository.findByItemOwnerIdAndStatus(any(Integer.class),
                any(Status.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        List<BookingDto> getBookingsOwner = bookingService.getBookingsOwner("REJECTED", 1, 1, 1);

        verify(bookingRepository, times(1))
                .findByItemOwnerIdAndStatus(any(Integer.class),
                        any(Status.class), any(Pageable.class));
        assertEquals(0, getBookingsOwner.size());
    }

    @Test
    public void shouldGetBookingsOwnerStateUnknown() {
        when(userRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(user));

        ValidationException ex = assertThrows(
                ValidationException.class, () -> bookingService.getBookingsOwner("GGGG", 1, 1, 1));
        assertEquals("Unknown state: GGGG", ex.getMessage());
    }
}
