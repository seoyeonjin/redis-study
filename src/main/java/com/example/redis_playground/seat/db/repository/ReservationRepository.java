package com.example.redis_playground.seat.db.repository;

import com.example.redis_playground.seat.db.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}
