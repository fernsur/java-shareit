package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoInput;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.State;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
    public void shouldNotGetBookingsByUncorrectedSize() {
        ValidationException ex = assertThrows(
                ValidationException.class, () -> bookingService.getBookings("ALL", 1, -3, 1));
        assertEquals("Индекс или количество эллементов не могут быть отрицательными.", ex.getMessage());
    }

    @Test
    public void shouldConvertingState() {
        State state1 = State.valueOf("ALL");
        State state2 = State.valueOf("CURRENT");
        State state3 = State.valueOf("PAST");
        State state4 = State.valueOf("FUTURE");
        State state5 = State.valueOf("WAITING");
        State state6 = State.valueOf("REJECTED");
        State state7 = State.valueOf("UNKNOWN");

        assertEquals(State.ALL, state1);
        assertEquals(State.CURRENT, state2);
        assertEquals(State.PAST, state3);
        assertEquals(State.FUTURE, state4);
        assertEquals(State.WAITING, state5);
        assertEquals(State.REJECTED, state6);
        assertEquals(State.UNKNOWN, state7);
    }
}
