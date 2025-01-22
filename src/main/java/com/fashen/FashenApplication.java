package com.fashen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.fashen")
public class FashenApplication {
    public static void main(String[] args) {
        SpringApplication.run(FashenApplication.class, args);
    }
} 