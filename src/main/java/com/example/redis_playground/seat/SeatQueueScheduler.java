package com.example.redis_playground.seat;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class SeatQueueScheduler {

    private final StringRedisTemplate redisTemplate;
    private final SeatQueueProcessor seatQueueProcessor;

    @Scheduled(fixedDelay = 300)
    public void process() {
        int batchSize = calculateBatchSize();
        seatQueueProcessor.processBatch(1L, batchSize);
    }

    private int calculateBatchSize() {
        Long size = redisTemplate.opsForZSet()
                .size("queue:seat:1");

        if (size == null) return 0;

        if (size < 10) return 1;
        if (size < 100) return 5;
        return 20;
    }
}
