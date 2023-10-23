package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoInput;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.BookingNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {
    @MockBean
    private BookingService bookingService;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    private BookingDtoInput bookingDtoInput;

    private User user;

    private Item item;

    private static final String USER_ID = "X-Sharer-User-Id";

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
    }

    @Test
    public void shouldAddBooking() throws Exception {
        Booking booking = BookingMapper.toBooking(bookingDtoInput, item, user);
        BookingDto bookingDto = BookingMapper.toBookingDto(booking);

        when(bookingService.createBooking(any(BookingDtoInput.class), any(Integer.class)))
                .thenReturn(bookingDto);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_ID, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.start", is(bookingDto.getStart().toString())))
                .andExpect(jsonPath("$.end", is(bookingDto.getEnd().toString())));
    }

    @Test
    public void shouldNotAddBookingByStartNull() throws Exception {
        Booking booking = BookingMapper.toBooking(bookingDtoInput, item, user);
        BookingDto bookingDto = BookingMapper.toBookingDto(booking);
        bookingDto.setStart(null);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_ID, 1))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldNotAddBookingByEndNull() throws Exception {
        Booking booking = BookingMapper.toBooking(bookingDtoInput, item, user);
        BookingDto bookingDto = BookingMapper.toBookingDto(booking);
        bookingDto.setEnd(null);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_ID, 1))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldUpdateBooking() throws Exception {
        Booking booking = BookingMapper.toBooking(bookingDtoInput, item, user);
        BookingDto bookingDto = BookingMapper.toBookingDto(booking);

        when(bookingService.updateBooking(any(Integer.class), any(Integer.class), any(Boolean.class)))
                .thenReturn(bookingDto);

        mvc.perform(patch("/bookings/1")
                        .content(mapper.writeValueAsString(bookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_ID, 1)
                        .param("approved", String.valueOf(true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.start", is(bookingDto.getStart().toString())))
                .andExpect(jsonPath("$.end", is(bookingDto.getEnd().toString())));
    }

    @Test
    public void shouldGetBooking() throws Exception {
        Booking booking = BookingMapper.toBooking(bookingDtoInput, item, user);
        BookingDto bookingDto = BookingMapper.toBookingDto(booking);

        when(bookingService.getBookingById(any(Integer.class), any(Integer.class)))
                .thenReturn(bookingDto);

        mvc.perform(get("/bookings/1")
                        .content(mapper.writeValueAsString(bookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_ID, 1))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldGetBookings() throws Exception {
        Booking booking = BookingMapper.toBooking(bookingDtoInput, item, user);
        BookingDto bookingDto = BookingMapper.toBookingDto(booking);

        when(bookingService.getBookings(any(String.class), any(Integer.class), any(Integer.class), any(Integer.class)))
                .thenReturn(List.of(bookingDto));

        mvc.perform(get("/bookings")
                        .content(mapper.writeValueAsString(bookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_ID, 1)
                        .param("state", "ALL")
                        .param("from", String.valueOf(1))
                        .param("size", String.valueOf(1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].start", is(bookingDto.getStart().toString())));
    }

    @Test
    public void shouldGetBookingsOwner() throws Exception {
        Booking booking = BookingMapper.toBooking(bookingDtoInput, item, user);
        BookingDto bookingDto = BookingMapper.toBookingDto(booking);

        when(bookingService.getBookingsOwner(any(String.class), any(Integer.class), any(Integer.class), any(Integer.class)))
                .thenReturn(List.of(bookingDto));

        mvc.perform(get("/bookings/owner")
                        .content(mapper.writeValueAsString(bookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_ID, 1)
                        .param("state", "ALL")
                        .param("from", String.valueOf(1))
                        .param("size", String.valueOf(1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].start", is(bookingDto.getStart().toString())))
                .andExpect(jsonPath("$[0].end", is(bookingDto.getEnd().toString())));
    }

    @Test
    public void shouldHandlerBookingNotFound() throws Exception {
        when(bookingService.getBookingById(any(Integer.class), any(Integer.class)))
                .thenThrow(new BookingNotFoundException("Невозможно получить. Такого бронирования нет."));

        mvc.perform(get("/bookings/1")
                        .header(USER_ID, 1))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("Невозможно получить. Такого бронирования нет.")));
    }
}
