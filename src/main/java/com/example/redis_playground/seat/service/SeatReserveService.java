package com.example.redis_playground.seat.service;

import com.example.redis_playground.seat.db.entity.Reservation;
import com.example.redis_playground.seat.db.repository.ReservationRepository;
import com.example.redis_playground.seat.db.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SeatReserveService {

    private final StringRedisTemplate redisTemplate;
    private final SeatRepository seatRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public void reserve(Long seatId, String userId) {

        // 1. HOLD 검증
        String holdKey = "seat:hold:" + seatId + ":" + userId;
        Boolean hasHold = redisTemplate.hasKey(holdKey);

        if (!Boolean.TRUE.equals(hasHold)) {
            throw new IllegalStateException("HOLD_EXPIRED_OR_NOT_OWNER");
        }

        // 2. 좌석 존재 검증
        seatRepository.findById(seatId)
                .orElseThrow(() -> new IllegalStateException("SEAT_NOT_FOUND"));

        // 3. DB 예약 시도
        try {
            reservationRepository.save(
                    new Reservation(seatId, userId)
            );
        } catch (DataIntegrityViolationException e) {
            // uk_seat 위반 → 이미 예약됨
            throw new IllegalStateException("ALREADY_RESERVED");
        }

        // 4. HOLD 제거
        redisTemplate.delete(holdKey);
    }
}
