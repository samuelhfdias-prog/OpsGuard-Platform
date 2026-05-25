package com.rodojacto.domain.collaborator.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class CollaboratorRequest(

    @field:NotBlank(message = "Nome é obrigatório")
    @field:Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    val name: String,

    @field:NotBlank(message = "CPF é obrigatório")
    @field:Size(max = 14, message = "CPF deve ter no máximo 14 caracteres")
    val cpf: String,

    @field:NotBlank(message = "E-mail é obrigatório")
    @field:Email(message = "E-mail inválido")
    val email: String,

    @field:NotBlank(message = "Cargo é obrigatório")
    @field:Size(max = 50, message = "Cargo deve ter no máximo 50 caracteres")
    val position: String,

    @field:NotNull(message = "ID da organização é obrigatório")
    val organizationId: Long
)
