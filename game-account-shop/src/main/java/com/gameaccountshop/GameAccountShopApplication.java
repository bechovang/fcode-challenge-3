package com.gameaccountshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GameAccountShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(GameAccountShopApplication.class, args);
    }
}
