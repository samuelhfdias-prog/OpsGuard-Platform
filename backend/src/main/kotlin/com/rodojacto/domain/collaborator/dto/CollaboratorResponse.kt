package com.rodojacto.domain.collaborator.dto

import com.rodojacto.domain.collaborator.Collaborator
import java.time.LocalDateTime

data class CollaboratorResponse(
    val id: Long,
    val name: String,
    val cpf: String,
    val email: String,
    val position: String,
    val organizationId: Long,
    val organizationName: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

fun Collaborator.toResponse() = CollaboratorResponse(
    id = id!!,
    name = name,
    cpf = cpf,
    email = email,
    position = position,
    organizationId = organization.id!!,
    organizationName = organization.name,
    createdAt = createdAt,
    updatedAt = updatedAt
)
