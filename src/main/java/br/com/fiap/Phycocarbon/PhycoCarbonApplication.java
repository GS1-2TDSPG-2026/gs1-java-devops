package br.com.fiap.Phycocarbon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PhycoCarbonApplication {
	public static void main(String[] args) {
		SpringApplication.run(PhycoCarbonApplication.class, args);
	}
}