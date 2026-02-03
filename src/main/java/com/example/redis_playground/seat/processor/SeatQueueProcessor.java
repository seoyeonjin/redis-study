package com.example.redis_playground.seat.processor;

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
    public static final String QUEUE_SEAT = "queue:seat:";
    private static final String TOKEN_KEY_PREFIX = "queue:seat:token:";
    private static final String ACTIVE_SEAT_QUEUE_KEY = "queue:seat:active";
    private static final int MAX_BATCH_SIZE = 20;

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<List<String>> batchPopScript;

    public void processTick(Long seatId) {
        String queueKey = QUEUE_SEAT + seatId;
        String tokenKey = TOKEN_KEY_PREFIX + seatId;
        String holdKeyPrefix = "seat:hold:" + seatId + ":";

        List<String> users = redisTemplate.execute(
                batchPopScript,
                List.of(queueKey, tokenKey, ACTIVE_SEAT_QUEUE_KEY), // KEYS
                seatId.toString(),                                  // ARGV[1]
                holdKeyPrefix,                                      // ARGV[2]
                String.valueOf(HOLD_TTL_SECONDS),                   // ARGV[3]
                String.valueOf(MAX_BATCH_SIZE)                      // ARGV[4]
        );

        if (users != null && !users.isEmpty()) {
            log.info("[HOLD_ISSUED] seatId={}, popCount={}, users={}", seatId, users.size(), users);
        }
    }
}
