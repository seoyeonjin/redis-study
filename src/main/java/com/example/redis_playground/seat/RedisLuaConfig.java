package com.example.redis_playground.seat;

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
            local queueKey = KEYS[1]
            local batchSize = tonumber(ARGV[1])
            local holdKeyPrefix = ARGV[2]
            local ttl = tonumber(ARGV[3])

            local users = redis.call("ZRANGE", queueKey, 0, batchSize - 1)
            if #users == 0 then
              return {}
            end

            redis.call("ZREM", queueKey, unpack(users))

            for i, userId in ipairs(users) do
              local holdKey = holdKeyPrefix .. userId
              redis.call("SET", holdKey, userId, "EX", ttl)
            end

            return users
        """);

        script.setResultType((Class<List<String>>) (Class<?>) List.class);
        return script;
    }
}
