package br.com.fiap.aquaorbital;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AquaOrbitalApplication {
	public static void main(String[] args) {
		SpringApplication.run(AquaOrbitalApplication.class, args);
	}
}