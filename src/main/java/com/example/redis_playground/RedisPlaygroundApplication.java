package com.example.redis_playground;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RedisPlaygroundApplication {

	public static void main(String[] args) {
		SpringApplication.run(RedisPlaygroundApplication.class, args);
	}

}
