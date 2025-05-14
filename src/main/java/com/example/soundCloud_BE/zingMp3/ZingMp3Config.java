package com.example.soundCloud_BE.zingMp3;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ZingMp3Config {

    @Bean
    public RestTemplate zingMp3RestTemplate() {
        return new RestTemplate();
    }


}
