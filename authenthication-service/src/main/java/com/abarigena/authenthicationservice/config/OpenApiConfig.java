package com.abarigena.authenthicationservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        Server localServer = new Server()
                .url("")
                .description("API Gateway URL");

        return new OpenAPI()
                .info(new Info()
                        .title("Authentication Service API")
                        .description("API для сервиса аутентификации")
                        .version("1.0.0"))
                .servers(Arrays.asList(localServer));
    }
}