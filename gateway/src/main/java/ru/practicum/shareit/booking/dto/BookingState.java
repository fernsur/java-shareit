package ru.practicum.shareit.booking.dto;

import java.util.Optional;

public enum BookingState {
    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED;

    public static Optional<BookingState> from(String line) {
        for (BookingState state: values()) {
            if (state.name().equalsIgnoreCase(line)) {
                return Optional.of(state);
            }
        }
        return Optional.empty();
    }
}
