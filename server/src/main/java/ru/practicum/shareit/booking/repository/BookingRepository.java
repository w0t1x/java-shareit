package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Booker
    List<Booking> findAllByBookerIdOrderByStartTimeDesc(long bookerId);

    List<Booking> findAllByBookerIdAndStartTimeBeforeAndEndTimeAfterOrderByStartTimeDesc(
            long bookerId, LocalDateTime now1, LocalDateTime now2);

    List<Booking> findAllByBookerIdAndEndTimeBeforeOrderByStartTimeDesc(long bookerId, LocalDateTime now);

    List<Booking> findAllByBookerIdAndStartTimeAfterOrderByStartTimeDesc(long bookerId, LocalDateTime now);

    List<Booking> findAllByBookerIdAndStatusOrderByStartTimeDesc(long bookerId, Status status);

    // Owner (item.userId)
    List<Booking> findAllByItemUserIdOrderByStartTimeDesc(long ownerId);

    List<Booking> findAllByItemUserIdAndStartTimeBeforeAndEndTimeAfterOrderByStartTimeDesc(
            long ownerId, LocalDateTime now1, LocalDateTime now2);

    List<Booking> findAllByItemUserIdAndEndTimeBeforeOrderByStartTimeDesc(long ownerId, LocalDateTime now);

    List<Booking> findAllByItemUserIdAndStartTimeAfterOrderByStartTimeDesc(long ownerId, LocalDateTime now);

    List<Booking> findAllByItemUserIdAndStatusOrderByStartTimeDesc(long ownerId, Status status);

    // For comments
    boolean existsByItemIdAndBookerIdAndStatusAndEndTimeBefore(
            long itemId, long bookerId, Status status, LocalDateTime now);

    // last/next for item
    Optional<Booking> findFirstByItemIdAndStartTimeBeforeAndStatusOrderByStartTimeDesc(
            long itemId, LocalDateTime now, Status status);

    Optional<Booking> findFirstByItemIdAndStartTimeAfterAndStatusOrderByStartTimeAsc(
            long itemId, LocalDateTime now, Status status);
}

