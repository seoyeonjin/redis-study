package com.example.redis_playground.seat;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SeatReserveService {

    private final StringRedisTemplate redisTemplate;
    private final ReservationRepository reservationRepository;

    @Transactional
    public void reserve(final Long seatId, final String userId) {
        final String key = "seat:hold:" + seatId;

        // 1. Redis HOLD 검증 (HOLD 키 존재, 값이 요청한 userId와 같은지 검증)
        final String holder = redisTemplate.opsForValue().get(key);

        if (holder == null) {
            throw new SeatHoldException("HOLD_NOT_FOUND");
        }
        if (!holder.equals(userId)) {
            throw new SeatHoldException("NOT_HOLDER");
        }

        // 2. DB insert
        try {
            reservationRepository.save(new Reservation(seatId, userId));
        } catch (DataIntegrityViolationException e) {
            throw new SeatHoldException("ALREADY_RESERVED");
        }

        // 3. Redis key 삭제
        redisTemplate.delete(key);
    }
}
