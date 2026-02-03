package com.example.redis_playground.seat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SeatQueueService {

    private static final String ACTIVE_SEAT_QUEUE_KEY = "queue:seat:active";
    private final StringRedisTemplate redisTemplate;

    public Long joinQueue(final Long seatId, final String userId) {
        final String key = "queue:seat:" + seatId;
        final double score = System.currentTimeMillis();

        redisTemplate.opsForSet().add(ACTIVE_SEAT_QUEUE_KEY, seatId.toString());
        redisTemplate.opsForZSet().add(key, userId, score);

        final Long rank = redisTemplate.opsForZSet().rank(key, userId);
        return rank == null ? null : rank + 1;
    }
}
