package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    Booking findFirstByItemIdAndStartIsBeforeAndStatusOrderByStartDesc(int itemId, LocalDateTime start, Status status);

    Booking findFirstByItemIdAndStartIsAfterAndStatusOrderByStartAsc(int itemId, LocalDateTime start, Status status);

    Booking findFirstByItemIdAndBookerIdAndEndIsBeforeAndStatus(int itemId, int userId,
                                                                LocalDateTime end, Status status);

    Page<Booking> findByBookerIdOrderByStartDesc(int bookerId, Pageable page);

    Page<Booking> findByBookerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(int bookerId, LocalDateTime start,
                                                              LocalDateTime end, Pageable page);

    Page<Booking> findByBookerIdAndEndIsBeforeOrderByStartDesc(int bookerId, LocalDateTime end, Pageable page);

    Page<Booking> findByBookerIdAndStartIsAfterOrderByStartDesc(int bookerId, LocalDateTime start, Pageable page);

    Page<Booking> findByBookerIdAndStatusOrderByStartDesc(int bookerId, Status status, Pageable page);

    Page<Booking> findByItemOwnerIdOrderByStartDesc(int ownerId, Pageable page);

    Page<Booking> findByItemOwnerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(int ownerId, LocalDateTime start,
                                                                              LocalDateTime end, Pageable page);

    Page<Booking> findByItemOwnerIdAndEndIsBeforeOrderByStartDesc(int ownerId, LocalDateTime end, Pageable page);

    Page<Booking> findByItemOwnerIdAndStartIsAfterOrderByStartDesc(int ownerId, LocalDateTime start, Pageable page);

    Page<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(int ownerId, Status status, Pageable page);
}
