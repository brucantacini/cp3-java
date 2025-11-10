package com.example.resourceserver.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .components(new Components()
                .addSecuritySchemes("bearer-jwt", 
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .in(SecurityScheme.In.HEADER)
                        .name("Authorization")))
            .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"))
            .info(new Info()
                .title("Resource Server API")
                .version("1.0.0")
                .description("API do Resource Server protegida com OAuth 2.0 e JWT. " +
                    "Para acessar endpoints protegidos, obtenha um token do Authorization Server " +
                    "(http://localhost:9000) e inclua no header: Authorization: Bearer <token>"));
    }
}

