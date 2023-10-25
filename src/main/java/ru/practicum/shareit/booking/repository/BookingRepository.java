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

    Page<Booking> findByBookerId(int bookerId, Pageable page);

    Page<Booking> findByBookerIdAndStartIsBeforeAndEndIsAfter(int bookerId, LocalDateTime start,
                                                              LocalDateTime end, Pageable page);

    Page<Booking> findByBookerIdAndEndIsBefore(int bookerId, LocalDateTime end, Pageable page);

    Page<Booking> findByBookerIdAndStartIsAfter(int bookerId, LocalDateTime start, Pageable page);

    Page<Booking> findByBookerIdAndStatus(int bookerId, Status status, Pageable page);

    Page<Booking> findByItemOwnerId(int ownerId, Pageable page);

    Page<Booking> findByItemOwnerIdAndStartIsBeforeAndEndIsAfter(int ownerId, LocalDateTime start,
                                                                 LocalDateTime end, Pageable page);

    Page<Booking> findByItemOwnerIdAndEndIsBefore(int ownerId, LocalDateTime end, Pageable page);

    Page<Booking> findByItemOwnerIdAndStartIsAfter(int ownerId, LocalDateTime start, Pageable page);

    Page<Booking> findByItemOwnerIdAndStatus(int ownerId, Status status, Pageable page);
}
