package com.example.redis_playground.seat;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "seats")
@Getter
@NoArgsConstructor
public class Seat {
    @Id
    private Long id;

    private String section;

    private String seatNumber;

    private LocalDateTime createdAt;
}
