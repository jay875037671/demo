package com.example.eurekaproducer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableEurekaClient
@ComponentScan(value = "com.example")
public class EurekaProducerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaProducerApplication.class, args);
    }



}
