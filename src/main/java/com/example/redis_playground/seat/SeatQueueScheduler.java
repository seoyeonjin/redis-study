package com.example.redis_playground.seat;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;


@Component
@RequiredArgsConstructor
public class SeatQueueScheduler {

    public static final String QUEUE_SEAT = "queue:seat:";
    public static final String ASTERISK = "*";
    private static final long SCHEDULER_INTERVAL_MS = 3000;

    private final StringRedisTemplate redisTemplate;
    private final SeatQueueProcessor seatQueueProcessor;

    @Scheduled(fixedDelay = SCHEDULER_INTERVAL_MS)
    public void process() {
        Set<String> keys = redisTemplate.keys(QUEUE_SEAT + ASTERISK);
        if (keys == null || keys.isEmpty()) return;

        for (String key : keys) {
            Long seatId = extractSeatId(key);
            int batchSize = calculateBatchSize(seatId);
            seatQueueProcessor.processBatch(seatId, batchSize);
        }
    }

    private Long extractSeatId(String key) {
        return Long.parseLong(key.replace(QUEUE_SEAT, ""));
    }

    private int calculateBatchSize(Long seatId) {
        Long size = redisTemplate.opsForZSet()
                .size(QUEUE_SEAT + seatId);

        if (size == null || size == 0) return 0;
        if (size < 10) return 1;
        if (size < 100) return 5;
        return 20;
    }
}
