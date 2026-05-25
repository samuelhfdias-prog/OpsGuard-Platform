package com.rodojacto.domain.organization.dto

import com.rodojacto.domain.organization.Organization
import java.time.LocalDateTime

data class OrganizationResponse(
    val id: Long,
    val name: String,
    val cnpj: String,
    val address: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

/** Extension function para mapear a entidade para o DTO de resposta. */
fun Organization.toResponse() = OrganizationResponse(
    id = id!!,
    name = name,
    cnpj = cnpj,
    address = address,
    createdAt = createdAt,
    updatedAt = updatedAt
)
