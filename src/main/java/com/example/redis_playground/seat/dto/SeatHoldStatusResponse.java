package com.example.redis_playground.seat.dto;

public record SeatHoldStatusResponse(
        String status,
        Long titleSeconds
) {
}
