package com.rodojacto.domain.organization

import com.rodojacto.domain.organization.dto.OrganizationRequest
import com.rodojacto.domain.organization.dto.OrganizationResponse
import com.rodojacto.domain.organization.dto.toResponse
import com.rodojacto.domain.user.Role
import com.rodojacto.domain.user.User
import com.rodojacto.exception.BusinessException
import com.rodojacto.exception.ForbiddenException
import com.rodojacto.exception.ResourceNotFoundException
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * Regras de acesso por perfil:
 * - MANAGER: acesso irrestrito a todas as organizações.
 * - OPERATOR: vê apenas sua própria organização; não pode criar, atualizar ou excluir.
 *
 * Operações de escrita protegidas por @PreAuthorize("hasRole('MANAGER')")
 * — verificação feita via Spring Security AOP antes da execução transacional.
 */
@Service
@Transactional(readOnly = true)
class OrganizationService(
    private val organizationRepository: OrganizationRepository
) {

    fun findAll(currentUser: User): List<OrganizationResponse> {
        return when (currentUser.role) {
            Role.MANAGER -> organizationRepository.findAll()
            Role.OPERATOR -> {
                val org = currentUser.organization
                    ?: throw BusinessException("Operador não possui organização vinculada")
                listOf(org)
            }
        }.map { it.toResponse() }
    }

    fun findById(id: Long, currentUser: User): OrganizationResponse {
        val organization = organizationRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Organização com ID $id não encontrada") }

        if (currentUser.role == Role.OPERATOR) {
            val userOrgId = currentUser.organization?.id
                ?: throw BusinessException("Operador não possui organização vinculada")
            if (organization.id != userOrgId) {
                throw ForbiddenException("Acesso negado à organização com ID $id")
            }
        }
        return organization.toResponse()
    }

    @Transactional
    @PreAuthorize("hasRole('MANAGER')")
    fun create(request: OrganizationRequest): OrganizationResponse {
        val cnpj = formatCnpj(request.cnpj)
        if (organizationRepository.existsByCnpj(cnpj)) {
            throw BusinessException("Já existe uma organização com o CNPJ $cnpj")
        }
        val organization = Organization(
            name = request.name.trim(),
            cnpj = cnpj,
            address = request.address?.trim()?.ifBlank { null }
        )
        return organizationRepository.save(organization).toResponse()
    }

    @Transactional
    @PreAuthorize("hasRole('MANAGER')")
    fun update(id: Long, request: OrganizationRequest): OrganizationResponse {
        val organization = organizationRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Organização com ID $id não encontrada") }

        val cnpj = formatCnpj(request.cnpj)
        if (organizationRepository.existsByCnpjAndIdNot(cnpj, id)) {
            throw BusinessException("Já existe outra organização com o CNPJ $cnpj")
        }

        organization.apply {
            name = request.name.trim()
            this.cnpj = cnpj
            address = request.address?.trim()?.ifBlank { null }
            updatedAt = LocalDateTime.now()
        }
        return organizationRepository.save(organization).toResponse()
    }

    @Transactional
    @PreAuthorize("hasRole('MANAGER')")
    fun delete(id: Long) {
        if (!organizationRepository.existsById(id)) {
            throw ResourceNotFoundException("Organização com ID $id não encontrada")
        }
        organizationRepository.deleteById(id)
    }

    private fun formatCnpj(value: String): String {
        val digits = value.filter(Char::isDigit)
        return "${digits.substring(0, 2)}.${digits.substring(2, 5)}.${digits.substring(5, 8)}/${digits.substring(8, 12)}-${digits.substring(12, 14)}"
    }
}
