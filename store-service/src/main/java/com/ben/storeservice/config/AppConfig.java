package com.ben.storeservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class AppConfig {

    @Bean("dummyJsonClient")
    public RestClient dummyJsonClient() {
        return RestClient.create("https://dummyjson.com");
    }
}
