package com.banking.transaction.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI transactionServiceOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Transaction Service API")
                .description("Transaction processing: transfers, history, and status tracking")
                .version("1.0.0"));
    }
}
