package com.example.redis_playground.seat.service;

import com.example.redis_playground.seat.dto.SeatQueueStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SeatQueueStatusService {

    public static final String SEAT_HOLD = "seat:hold:";
    public static final String QUEUE_SEAT = "queue:seat:";
    private final StringRedisTemplate redisTemplate;

    private static final double SCHEDULER_INTERVAL_SECONDS = 3.0;

    public SeatQueueStatusResponse getStatus(Long seatId, String userId) {

        String queueKey = QUEUE_SEAT + seatId;
        String holdKey = SEAT_HOLD + seatId + ":" + userId;

        Boolean hasHold = redisTemplate.hasKey(holdKey);
        if (Boolean.TRUE.equals(hasHold)) {
            Long ttl = redisTemplate.getExpire(holdKey);
            return SeatQueueStatusResponse.holding(ttl);
        }

        Long ahead = redisTemplate.opsForZSet().rank(queueKey, userId);
        Long total = redisTemplate.opsForZSet().zCard(queueKey);

        if (ahead == null) {
            return SeatQueueStatusResponse.waiting(null, total, null);
        }

        long estimatedWaitSeconds = estimateWaitSeconds(ahead, total);

        return SeatQueueStatusResponse.waiting(
                ahead,
                total,
                estimatedWaitSeconds
        );
    }

    private long estimateWaitSeconds(long ahead, long total) {
        if (ahead == 0) {
            return 0L;
        }

        long tokensPerTick;
        if (total < 10) {
            tokensPerTick = 1;
        } else if (total < 100) {
            tokensPerTick = 5;
        } else {
            tokensPerTick = 20;
        }

        double rate = tokensPerTick / SCHEDULER_INTERVAL_SECONDS;
        if (rate <= 0) {
            return -1L;
        }

        return (long) (ahead / rate);
    }
}
