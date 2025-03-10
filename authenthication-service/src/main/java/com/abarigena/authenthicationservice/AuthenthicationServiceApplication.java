package com.abarigena.authenthicationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class AuthenthicationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthenthicationServiceApplication.class, args);
    }

}
