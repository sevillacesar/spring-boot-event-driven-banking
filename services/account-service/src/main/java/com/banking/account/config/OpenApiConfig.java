package com.banking.account.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI accountServiceOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Account Service API")
                .description("Account management: creation, deposits, and balance queries")
                .version("1.0.0"));
    }
}
