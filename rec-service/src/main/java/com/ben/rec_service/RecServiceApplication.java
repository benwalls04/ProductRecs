package com.ben.rec_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class RecServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RecServiceApplication.class, args);
	}

}
