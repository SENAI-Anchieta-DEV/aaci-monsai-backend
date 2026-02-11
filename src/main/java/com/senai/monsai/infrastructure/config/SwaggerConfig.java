package com.senai.monsai.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI oficinaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API - Conta Bancaria")
                        .description("Cadastro e gestão de serviços de um banco.")
                        .version("1.0")
                        .contact(new Contact()
                                .name("Equipe Banco")
                                .email("suporte@banco.com")))
                .addSecurityItem(new SecurityRequirement()
                        .addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()));
    }
    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer");
    }
}