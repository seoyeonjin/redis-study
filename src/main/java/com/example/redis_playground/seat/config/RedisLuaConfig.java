package com.example.redis_playground.seat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.List;

@Configuration
public class RedisLuaConfig {

    @Bean
    public DefaultRedisScript<List<String>> batchPopScript() {
        DefaultRedisScript<List<String>> script = new DefaultRedisScript<>();
        script.setScriptText("""
            -- KEYS[1]: queueKey (e.g., queue:seat:1)
            -- KEYS[2]: tokenKey (e.g., queue:seat:token:1)
            -- KEYS[3]: activeSeatKey (e.g., queue:seat:active)
            -- ARGV[1]: seatId
            -- ARGV[2]: holdKeyPrefix (e.g., seat:hold:1:)
            -- ARGV[3]: holdKeyTtl
            -- ARGV[4]: maxBatchSize
                            
            local queueKey = KEYS[1]
            local tokenKey = KEYS[2]
            local activeSeatKey = KEYS[3]
            local seatId = ARGV[1]
            local holdKeyPrefix = ARGV[2]
            local holdKeyTtl = tonumber(ARGV[3])
            local maxBatchSize = tonumber(ARGV[4])
            
            -- Get queue size
            local queueSize = redis.call("ZCARD", queueKey)
            
            -- Determine tokens to add based on rate policy
            local tokensToAdd = 0
            if queueSize > 0 then
                if queueSize < 10 then
                    tokensToAdd = 1
                elseif queueSize < 100 then
                    tokensToAdd = 5
                else
                    tokensToAdd = 20
                end
            end
            
            -- Increment token count. If key doesn't exist, INCRBY starts from 0.
            local currentTokens = redis.call("INCRBY", tokenKey, tokensToAdd)
            
            -- Determine popCount
            local popCount = math.min(currentTokens, maxBatchSize)
            
            if popCount <= 0 then
                if queueSize == 0 then
                    redis.call("SREM", activeSeatKey, seatId)
                    redis.call("DEL", tokenKey)
                end
                return {}
            end
            
            -- Pop users from the sorted set. ZPOPMIN returns [member, score, member, score, ...]
            local popped = redis.call("ZPOPMIN", queueKey, popCount)
            if #popped == 0 then
                return {}
            end
            
            -- Decrement the tokens by the number of users actually popped.
            local actualPopCount = #popped / 2
            redis.call("DECRBY", tokenKey, actualPopCount)
            
            -- Create hold keys for popped users
            local users = {}
            for i = 1, #popped, 2 do
                local userId = popped[i]
                table.insert(users, userId)
                local holdKey = holdKeyPrefix .. userId
                redis.call("SET", holdKey, userId, "EX", holdKeyTtl)
            end
            
            -- After popping, check if the queue is now empty and clean up if so.
            local finalQueueSize = redis.call("ZCARD", queueKey)
            if finalQueueSize == 0 then
                redis.call("SREM", activeSeatKey, seatId)
                redis.call("DEL", tokenKey)
            end
            
            return users
        """);

        script.setResultType((Class<List<String>>) (Class<?>) List.class);
        return script;
    }
}
