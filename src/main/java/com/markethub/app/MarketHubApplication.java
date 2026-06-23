package com.markethub.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class 	MarketHubApplication {

	public static void main(String[] args) {
		SpringApplication.run(MarketHubApplication.class, args);
	}
}
