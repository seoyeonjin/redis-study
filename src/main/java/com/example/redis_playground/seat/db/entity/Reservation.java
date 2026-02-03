package com.example.redis_playground.seat.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "reservations",
        uniqueConstraints = @UniqueConstraint(name = "uk_seat", columnNames = "seat_id"))
@Getter
@NoArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seat_id", nullable = false)
    private Long seatId;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Column(name = "reserved_at", nullable = false)
    private Instant reservedAt = Instant.now();

    public Reservation(Long seatId, String userId) {
        this.seatId = seatId;
        this.userId = userId;
        this.reservedAt = Instant.now();
    }

}
