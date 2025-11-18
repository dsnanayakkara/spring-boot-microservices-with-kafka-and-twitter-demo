package com.microservices.demo.elastic.query.service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Social Events Query API")
                        .version("1.0.0")
                        .description("REST API for querying social events from Elasticsearch. " +
                                "This API provides full-text search, filtering, and pagination capabilities " +
                                "for social media-like events stored in Elasticsearch.")
                        .contact(new Contact()
                                .name("API Support")
                                .email("support@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server().url("http://localhost:8084").description("Local Development Server"),
                        new Server().url("http://localhost:8084/api/v1").description("API Base Path")
                ));
    }
}
