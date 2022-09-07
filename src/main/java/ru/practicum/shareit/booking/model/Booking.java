package ru.practicum.shareit.booking.model;

import lombok.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;


@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    Long id;

    @Column(name = "start_date")
    LocalDateTime start;

    @Column(name = "end_date")
    LocalDateTime end;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", referencedColumnName = "id")
    Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booker_id", referencedColumnName = "id")
    User booker;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    StatusType status;
}
