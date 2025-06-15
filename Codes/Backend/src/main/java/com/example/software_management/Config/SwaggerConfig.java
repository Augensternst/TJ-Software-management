package com.example.software_management.Config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.media.Schema;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI(@Value("${springdoc.version}") String appVersion) {

        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");


        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("bearerAuth");

        return new OpenAPI()
                .info(new Info().title("SM Project Backend API")
                        .version(appVersion)
                        .description("设备剩余寿命预测调用的后端API")
                        .termsOfService("http://swagger.io/terms/")
                        .contact(new Contact().email("aaa.com").name("aaa").url("https://aaa.com")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", securityScheme)
                        .addSchemas("file", new Schema().type("string").format("binary"))) // 注册 Security Scheme
                .addSecurityItem(securityRequirement); // 全局应用 Security Requirement
    }
}