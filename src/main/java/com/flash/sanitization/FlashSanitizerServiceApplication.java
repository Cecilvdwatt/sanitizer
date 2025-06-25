package com.flash.sanitization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.flash.sanitization")
public class FlashSanitizerServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlashSanitizerServiceApplication.class, args);
	}

}
