package com.example.redis_playground.seat.db.repository;

import com.example.redis_playground.seat.db.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<Seat, Long> {
}
