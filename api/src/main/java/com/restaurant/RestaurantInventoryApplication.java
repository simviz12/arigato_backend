package com.restaurant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(scanBasePackages = {"com.restaurant"})
@EntityScan(basePackages = {"com.restaurant"})
public class RestaurantInventoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestaurantInventoryApplication.class, args);
    }
}
