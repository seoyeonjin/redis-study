package com.example.redis_playground.seat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatQueueProcessor {

    private static final long HOLD_TTL_SECONDS = 300;

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<List<String>> batchPopScript;

    public List<String> processBatch(Long seatId, int batchSize) {
        String queueKey = "queue:seat:" + seatId;
        String holdKeyPrefix = "seat:hold:" + seatId + ":";

        List<String> users = redisTemplate.execute(
                batchPopScript,
                List.of(queueKey),
                String.valueOf(batchSize),
                holdKeyPrefix,
                String.valueOf(HOLD_TTL_SECONDS)
        );

        if (users != null && !users.isEmpty()) {
            log.info("[HOLD_ISSUED] seatId={}, users={}", seatId, users);
        }

        return users;
    }
}
