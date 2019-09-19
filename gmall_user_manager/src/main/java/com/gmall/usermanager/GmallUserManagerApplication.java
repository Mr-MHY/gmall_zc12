package com.gmall.usermanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;
@ComponentScan("com.gmall")
@MapperScan(basePackages = "com.gmall.usermanager.mapper")
@SpringBootApplication
public class GmallUserManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallUserManagerApplication.class, args);
	}

}
