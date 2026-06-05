package br.com.fiap.Phycocarbon.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AquaOrbital API")
                        .description("""
                                Plataforma IoT + IA para otimização do cultivo de microalgas
                                com integração de dados orbitais (NASA/Copernicus).
                                
                                **FIAP Global Solution 2026/1** — Análise e Desenvolvimento de Sistemas
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipe AquaOrbital")
                                .email("aquaorbital@fiap.com.br")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
