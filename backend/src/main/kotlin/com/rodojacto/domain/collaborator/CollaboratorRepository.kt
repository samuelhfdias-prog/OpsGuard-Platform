package com.rodojacto.domain.collaborator

import org.springframework.data.jpa.repository.JpaRepository

interface CollaboratorRepository : JpaRepository<Collaborator, Long> {
    fun findAllByOrganizationId(organizationId: Long): List<Collaborator>
    fun existsByCpf(cpf: String): Boolean
    fun existsByEmail(email: String): Boolean
    fun existsByCpfAndIdNot(cpf: String, id: Long): Boolean
    fun existsByEmailAndIdNot(email: String, id: Long): Boolean
}
