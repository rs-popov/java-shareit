package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("select b from Booking b " +
            "where b.booker.id = ?1 " +
            "order by b.id desc")
    List<Booking> findAllByBookerId(Long bookerId);

    @Query("select b from Booking b " +
            "where b.booker.id = ?1 " +
            "order by b.id desc")
    Page<Booking> findAllByBookerId(Long bookerId, Pageable page);

    @Query("select b from Booking b " +
            "where b.item.owner.id = ?1 " +
            "order by b.id desc")
    Page<Booking> getAllByOwnerId(Long ownerId, Pageable page);

    @Query("select b from Booking b " +
            "where b.item.id = ?1 " +
            "and b.item.owner.id = ?2 " +
            "and b.end < current_timestamp " +
            "order by b.end desc")
    Optional<Booking> findLastBooking(Long itemId, Long ownerId);

    @Query("select b from Booking b " +
            "where b.item.id = ?1 " +
            "and b.item.owner.id = ?2 " +
            "and b.start > current_timestamp " +
            "order by b.start")
    Optional<Booking> findNextBooking(Long itemId, Long ownerId);
}