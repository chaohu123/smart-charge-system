package com.smartcharge;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.smartcharge.mapper")
@EnableScheduling
public class SmartChargeApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartChargeApplication.class, args);
    }
}

