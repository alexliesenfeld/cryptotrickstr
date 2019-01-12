package com.schumskich.cryptotrickstr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CryptoTrickstrTraderApplication {

    public static void main(String[] args) {
        SpringApplication.run(CryptoTrickstrTraderApplication.class, args);
    }
}
