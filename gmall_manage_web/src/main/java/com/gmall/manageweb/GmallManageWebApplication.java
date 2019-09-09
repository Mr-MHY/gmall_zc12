package com.gmall.manageweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = "com.gmall.manageweb")
@SpringBootApplication
public class GmallManageWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallManageWebApplication.class, args);
	}

}
