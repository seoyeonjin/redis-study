package com.example.redis_playground.test;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/redis")
@RequiredArgsConstructor
public class RedisTestController {

    private final RedisTemplate<String, String> redisTemplate;

    @PostMapping("/string/incr")
    public String incr(@RequestParam String key) {
        Long value = redisTemplate.opsForValue()
                .increment(key);
        return key + " = " + value;
    }

    @GetMapping("/string/get")
    public String get(@RequestParam String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @PostMapping("/hash/set")
    public String hset(@RequestParam String key, @RequestParam String field, @RequestParam String value) {
        redisTemplate.opsForHash().put(key, field, value);
        return "OK";
    }

    @GetMapping("/hash/get")
    public String hget(@RequestParam String key, @RequestParam String field) {
        Object value = redisTemplate.opsForHash().get(key, field);
        return value == null ? null : value.toString();
    }

    @GetMapping("/hash/getall")
    public Map<Object, Object> hgetall(@RequestParam String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    @PostMapping("/list/push")
    public Long lpush(@RequestParam String key, @RequestParam String value) {
        return redisTemplate.opsForList().leftPush(key, value);
    }

    @GetMapping("/list/range")
    public List<String> lrange(
            @RequestParam String key,
            @RequestParam long start,
            @RequestParam long end
    ) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    @PostMapping("/zset/add")
    public String zadd(
            @RequestParam String key,
            @RequestParam String member,
            @RequestParam double score
    ) {
        Boolean added = redisTemplate.opsForZSet().add(key, member, score);
        return Boolean.TRUE.equals(added) ? "ADDED" : "UPDATED";
    }

    @GetMapping("/zset/range")
    public List<Map<String, Object>> zrange(
            @RequestParam String key,
            @RequestParam long start,
            @RequestParam long end,
            @RequestParam(defaultValue = "false") boolean withScores
    ) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (withScores) {
            Set<ZSetOperations.TypedTuple<String>> tuples =
                    redisTemplate.opsForZSet().rangeWithScores(key, start, end);
            if (tuples == null) {
                return result;
            }
            for (ZSetOperations.TypedTuple<String> tuple : tuples) {
                Map<String, Object> row = new HashMap<>();
                row.put("member", tuple.getValue());
                row.put("score", tuple.getScore());
                result.add(row);
            }
            return result;
        }

        Set<String> members = redisTemplate.opsForZSet().range(key, start, end);
        if (members == null) {
            return result;
        }
        for (String member : members) {
            Map<String, Object> row = new HashMap<>();
            row.put("member", member);
            result.add(row);
        }
        return result;
    }

    @PostMapping("/expire")
    public String expire(@RequestParam String key, @RequestParam long seconds) {
        Boolean applied = redisTemplate.expire(key, Duration.ofSeconds(seconds));
        return Boolean.TRUE.equals(applied) ? "OK" : "NOT_FOUND";
    }

    @GetMapping("/ttl")
    public Long ttl(@RequestParam String key) {
        return redisTemplate.getExpire(key);
    }
}
