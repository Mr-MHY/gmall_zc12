package com.gmall.cartweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.gmall")
public class GamllCartWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(GamllCartWebApplication.class, args);
	}

}
