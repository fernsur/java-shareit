package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    Booking findFirstByItemIdAndEndIsBeforeAndStatusOrderByEndDesc(int itemId, LocalDateTime end, Status status);

    Booking findFirstByItemIdAndStartIsAfterAndStatusOrderByStartAsc(int itemId, LocalDateTime start, Status status);

    Booking findFirstByItemIdAndBookerIdAndEndIsBeforeAndStatus(int itemId, int userId,
                                                                LocalDateTime end, Status status);

    List<Booking> findByBookerIdOrderByStartDesc(int bookerId);

    List<Booking> findByBookerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(int bookerId, LocalDateTime start,
                                                              LocalDateTime end);

    List<Booking> findByBookerIdAndEndIsBeforeOrderByStartDesc(int bookerId, LocalDateTime end);

    List<Booking> findByBookerIdAndStartIsAfterOrderByStartDesc(int bookerId, LocalDateTime start);

    List<Booking> findByBookerIdAndStatusOrderByStartDesc(int bookerId, Status status);

    List<Booking> findByItemOwnerIdOrderByStartDesc(int ownerId);

    List<Booking> findByItemOwnerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(int ownerId, LocalDateTime start,
                                                                              LocalDateTime end);

    List<Booking> findByItemOwnerIdAndEndIsBeforeOrderByStartDesc(int ownerId, LocalDateTime end);

    List<Booking> findByItemOwnerIdAndStartIsAfterOrderByStartDesc(int ownerId, LocalDateTime start);

    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(int ownerId, Status status);
}
