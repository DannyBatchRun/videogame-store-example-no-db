package com.videogame.example.third;

import org.springframework.boot.SpringApplication;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ThirdApplication {
	public static void main(String[] args) {
		SpringApplication.run(ThirdApplication.class, args);
	}
	@Bean
    	RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder){
        	return restTemplateBuilder.setConnectTimeout(Duration.ofSeconds(5)).setReadTimeout(Duration.ofSeconds(5)).build();
    	}
}
