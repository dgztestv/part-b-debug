package com.meterline.status;

import com.google.common.base.Ticker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;

@SpringBootApplication
@EnableScheduling
public class StatusApplication {

    public static void main(String[] args) {
        SpringApplication.run(StatusApplication.class, args);
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public Ticker ticker() {
        return Ticker.systemTicker();
    }

    @Bean
    public EventPublisher eventPublisher() {
        return event -> {
            // wired to the paging pipeline in the real deployment
        };
    }
}
