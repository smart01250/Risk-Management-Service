package com.assessment.riskmanagement.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Risk Management Service API")
                        .version("1.0.0"))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8081")
                                .description("Development Server"),
                        new Server()
                                .url("https://igor-riskmanagement.duckdns.org")
                                .description("Production Server")))
                .tags(List.of(
                        new Tag()
                                .name("User Management")
                                .description("User registration and management operations"),
                        new Tag()
                                .name("Order Management")
                                .description("Trading signal processing and order management"),
                        new Tag()
                                .name("Risk Management")
                                .description("Risk monitoring and threshold management"),
                        new Tag()
                                .name("Monitoring")
                                .description("System monitoring and health check operations")));
    }
}
