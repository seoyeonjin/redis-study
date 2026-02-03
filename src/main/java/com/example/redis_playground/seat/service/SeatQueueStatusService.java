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

    private static final long SCHEDULER_INTERVAL_MS = 3000;

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

        int batchSize = calculateBatchSize(total);
        Long estimatedWaitSeconds = estimateWaitSeconds(ahead, batchSize);

        return SeatQueueStatusResponse.waiting(
                ahead,
                total,
                estimatedWaitSeconds
        );
    }

    private int calculateBatchSize(Long total) {
        if (total == null || total == 0) return 1;
        if (total < 10) return 1;
        if (total < 100) return 5;
        return 20;
    }

    private Long estimateWaitSeconds(Long ahead, int batchSize) {
        if (ahead == null || ahead == 0) return 0L;

        long cycles = (long) Math.ceil((double) ahead / batchSize);
        return (cycles * SCHEDULER_INTERVAL_MS) / 1000;
    }
}
