package com.senai.monsai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.config.EnableIntegration;

@SpringBootApplication
@EnableIntegration
@IntegrationComponentScan(basePackages = "com.senai.monsai.infrastructure.config") // Procura o Gateway
public class MonsaiApplication {

	public static void main(String[] args) {
		SpringApplication.run(MonsaiApplication.class, args);
	}

}
