package com.rodojacto.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class LoginRequest(

    @field:Email(message = "E-mail inválido")
    @field:NotBlank(message = "E-mail é obrigatório")
    val email: String,

    @field:NotBlank(message = "Senha é obrigatória")
    val password: String
)
