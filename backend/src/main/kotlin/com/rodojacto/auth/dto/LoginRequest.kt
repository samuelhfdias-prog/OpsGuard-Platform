package com.rodojacto.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class LoginRequest(

    @field:Email(message = "E-mail inválido")
    @field:NotBlank(message = "E-mail é obrigatório")
    val email: String,

    @field:NotBlank(message = "Senha é obrigatória")
    @field:Size(max = 128, message = "Senha deve ter no máximo 128 caracteres")
    val password: String
)
