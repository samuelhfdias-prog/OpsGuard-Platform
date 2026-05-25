package com.rodojacto.domain.organization.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class OrganizationRequest(

    @field:NotBlank(message = "Nome é obrigatório")
    @field:Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    val name: String,

    @field:NotBlank(message = "CNPJ é obrigatório")
    @field:Size(max = 18, message = "CNPJ deve ter no máximo 18 caracteres")
    val cnpj: String,

    @field:Size(max = 200, message = "Endereço deve ter no máximo 200 caracteres")
    val address: String? = null
)
