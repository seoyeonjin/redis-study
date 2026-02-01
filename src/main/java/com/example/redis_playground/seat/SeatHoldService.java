package com.example.redis_playground.seat;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatHoldService {

    private static final long HOLD_TTL_SECONDS = 300;

    private final StringRedisTemplate redisTemplate;
    private static final DefaultRedisScript<Long> HOLD_SCRIPT;

    static {
        HOLD_SCRIPT = new DefaultRedisScript<>();
        HOLD_SCRIPT.setScriptText(
                """
                        if redis.call("exists", KEYS[1]) == 1 then
                        return 0
                    end
                    redis.call("set", KEYS[1], ARGV[1])
                    redis.call("expire", KEYS[1], ARGV[2])
                    return 1
                """
        );
        HOLD_SCRIPT.setResultType(Long.class);
    }

    public boolean hold(final Long seatId, final String userId) {
        final String key = "seat:hold:" + seatId;

        final Long result = redisTemplate.execute(
                HOLD_SCRIPT,
                List.of(key),
                userId,
                String.valueOf(HOLD_TTL_SECONDS)
        );

        return result != null && result == 1;
    }
}
