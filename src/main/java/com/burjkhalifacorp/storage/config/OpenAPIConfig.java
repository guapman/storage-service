package com.burjkhalifacorp.storage.config;

import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "Storage Service API", version = "v1", description = "API v1 docs")
)
public class OpenAPIConfig {
}

