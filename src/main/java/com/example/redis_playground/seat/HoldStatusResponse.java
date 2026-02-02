package com.example.redis_playground.seat;

public record HoldStatusResponse(
        String status,
        Long titleSeconds
) {
}
