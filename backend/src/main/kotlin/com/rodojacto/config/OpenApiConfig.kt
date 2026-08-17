package com.rodojacto.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Contact
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.context.annotation.Configuration

@Configuration
@OpenAPIDefinition(
    info = Info(
        title = "OpsGuard API",
        version = "1.0.0",
        description = "Sistema de gestão de Organizações, Colaboradores e Dispositivos. " +
                "Autentique-se em /api/auth/login e use o token no botão 'Authorize'.",
        contact = Contact(name = "OpsGuard Platform")
    )
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer",
    `in` = SecuritySchemeIn.HEADER,
    description = "Insira o token JWT obtido no endpoint /api/auth/login"
)
class OpenApiConfig
