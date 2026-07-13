package com.rodojacto.domain.collaborator

import com.rodojacto.domain.collaborator.dto.CollaboratorRequest
import com.rodojacto.domain.collaborator.dto.CollaboratorResponse
import com.rodojacto.domain.collaborator.dto.toResponse
import com.rodojacto.domain.organization.OrganizationRepository
import com.rodojacto.domain.user.Role
import com.rodojacto.domain.user.User
import com.rodojacto.exception.BusinessException
import com.rodojacto.exception.ForbiddenException
import com.rodojacto.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * Regras de acesso:
 * - MANAGER: CRUD irrestrito em qualquer organização.
 * - OPERATOR: lista/cria/edita/exclui apenas colaboradores da PRÓPRIA organização.
 *   O organizationId do request é IGNORADO para OPERATOR (usa o da organização do token).
 */
@Service
@Transactional(readOnly = true)
class CollaboratorService(
    private val collaboratorRepository: CollaboratorRepository,
    private val organizationRepository: OrganizationRepository
) {

    fun findAll(currentUser: User): List<CollaboratorResponse> {
        return when (currentUser.role) {
            Role.MANAGER -> collaboratorRepository.findAll()
            Role.OPERATOR -> {
                val orgId = currentUser.organization?.id
                    ?: throw BusinessException("Operador não possui organização vinculada")
                collaboratorRepository.findAllByOrganizationId(orgId)
            }
        }.map { it.toResponse() }
    }

    fun findById(id: Long, currentUser: User): CollaboratorResponse {
        val collaborator = collaboratorRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Colaborador com ID $id não encontrado") }
        checkAccess(collaborator, currentUser)
        return collaborator.toResponse()
    }

    @Transactional
    fun create(request: CollaboratorRequest, currentUser: User): CollaboratorResponse {
        val cpf = formatCpf(request.cpf)
        val email = request.email.trim().lowercase()
        val effectiveOrgId = resolveOrganizationId(request.organizationId, currentUser)

        val organization = organizationRepository.findById(effectiveOrgId)
            .orElseThrow { ResourceNotFoundException("Organização com ID $effectiveOrgId não encontrada") }

        if (collaboratorRepository.existsByCpf(cpf)) {
            throw BusinessException("Já existe um colaborador com o CPF $cpf")
        }
        if (collaboratorRepository.existsByEmail(email)) {
            throw BusinessException("Já existe um colaborador com o e-mail $email")
        }

        val collaborator = Collaborator(
            name = request.name.trim(),
            cpf = cpf,
            email = email,
            position = request.position.trim(),
            organization = organization
        )
        return collaboratorRepository.save(collaborator).toResponse()
    }

    @Transactional
    fun update(id: Long, request: CollaboratorRequest, currentUser: User): CollaboratorResponse {
        val cpf = formatCpf(request.cpf)
        val email = request.email.trim().lowercase()
        val collaborator = collaboratorRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Colaborador com ID $id não encontrado") }

        checkAccess(collaborator, currentUser)

        // OPERATOR não pode mover colaborador para outra org
        val effectiveOrgId = when (currentUser.role) {
            Role.MANAGER -> request.organizationId
            Role.OPERATOR -> collaborator.organization.id!!
        }

        val organization = organizationRepository.findById(effectiveOrgId)
            .orElseThrow { ResourceNotFoundException("Organização com ID $effectiveOrgId não encontrada") }

        if (collaboratorRepository.existsByCpfAndIdNot(cpf, id)) {
            throw BusinessException("Já existe outro colaborador com o CPF $cpf")
        }
        if (collaboratorRepository.existsByEmailAndIdNot(email, id)) {
            throw BusinessException("Já existe outro colaborador com o e-mail $email")
        }

        collaborator.apply {
            name = request.name.trim()
            this.cpf = cpf
            this.email = email
            position = request.position.trim()
            this.organization = organization
            updatedAt = LocalDateTime.now()
        }
        return collaboratorRepository.save(collaborator).toResponse()
    }

    @Transactional
    fun delete(id: Long, currentUser: User) {
        val collaborator = collaboratorRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Colaborador com ID $id não encontrado") }
        checkAccess(collaborator, currentUser)
        collaboratorRepository.deleteById(id)
    }

    // ─── Helpers privados ──────────────────────────────────────────────────────

    private fun resolveOrganizationId(requestedOrgId: Long, currentUser: User): Long =
        when (currentUser.role) {
            Role.MANAGER -> requestedOrgId
            Role.OPERATOR -> currentUser.organization?.id
                ?: throw BusinessException("Operador não possui organização vinculada")
        }

    private fun checkAccess(collaborator: Collaborator, currentUser: User) {
        if (currentUser.role == Role.OPERATOR) {
            val userOrgId = currentUser.organization?.id
                ?: throw BusinessException("Operador não possui organização vinculada")
            if (collaborator.organization.id != userOrgId) {
                throw ForbiddenException("Acesso negado ao colaborador com ID ${collaborator.id}")
            }
        }
    }

    private fun formatCpf(value: String): String {
        val digits = value.filter(Char::isDigit)
        return "${digits.substring(0, 3)}.${digits.substring(3, 6)}.${digits.substring(6, 9)}-${digits.substring(9, 11)}"
    }
}
