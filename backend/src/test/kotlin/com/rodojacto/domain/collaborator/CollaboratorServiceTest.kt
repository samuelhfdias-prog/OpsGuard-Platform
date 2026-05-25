package com.rodojacto.domain.collaborator

import com.rodojacto.domain.collaborator.dto.CollaboratorRequest
import com.rodojacto.domain.organization.Organization
import com.rodojacto.domain.organization.OrganizationRepository
import com.rodojacto.domain.user.Role
import com.rodojacto.domain.user.User
import com.rodojacto.exception.BusinessException
import com.rodojacto.exception.ForbiddenException
import com.rodojacto.exception.ResourceNotFoundException
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Optional

@ExtendWith(MockKExtension::class)
class CollaboratorServiceTest {

    @MockK
    private lateinit var collaboratorRepository: CollaboratorRepository

    @MockK
    private lateinit var organizationRepository: OrganizationRepository

    @InjectMockKs
    private lateinit var collaboratorService: CollaboratorService

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun buildOrg(id: Long = 1L) =
        Organization(id = id, name = "Org $id", cnpj = "00.000.00$id/0001-00")

    private fun buildCollaborator(id: Long = 1L, org: Organization) =
        Collaborator(
            id = id, name = "Colab $id", cpf = "000.000.00$id-00",
            email = "colab$id@test.com", position = "Cargo", organization = org
        )

    private fun buildManager(org: Organization? = null) =
        User(id = 1L, name = "Manager", email = "m@test.com", password = "hash", role = Role.MANAGER, organization = org)

    private fun buildOperator(org: Organization) =
        User(id = 2L, name = "Operator", email = "op@test.com", password = "hash", role = Role.OPERATOR, organization = org)

    // ─── findAll ──────────────────────────────────────────────────────────────

    @Test
    fun `findAll deve retornar todos os colaboradores quando MANAGER`() {
        val manager = buildManager()
        val org = buildOrg()
        every { collaboratorRepository.findAll() } returns listOf(buildCollaborator(1L, org), buildCollaborator(2L, org))

        val result = collaboratorService.findAll(manager)

        assertThat(result).hasSize(2)
        verify(exactly = 1) { collaboratorRepository.findAll() }
    }

    @Test
    fun `findAll deve retornar apenas colaboradores da propria org quando OPERATOR`() {
        val org = buildOrg(1L)
        val operator = buildOperator(org)
        every { collaboratorRepository.findAllByOrganizationId(1L) } returns listOf(buildCollaborator(1L, org))

        val result = collaboratorService.findAll(operator)

        assertThat(result).hasSize(1)
        verify(exactly = 1) { collaboratorRepository.findAllByOrganizationId(1L) }
        verify(exactly = 0) { collaboratorRepository.findAll() }
    }

    // ─── findById ─────────────────────────────────────────────────────────────

    @Test
    fun `findById deve lancar ForbiddenException quando OPERATOR acessa colab de outra org`() {
        val userOrg = buildOrg(1L)
        val otherOrg = buildOrg(2L)
        val operator = buildOperator(userOrg)
        every { collaboratorRepository.findById(1L) } returns Optional.of(buildCollaborator(1L, otherOrg))

        assertThatThrownBy { collaboratorService.findById(1L, operator) }
            .isInstanceOf(ForbiddenException::class.java)
    }

    @Test
    fun `findById deve lancar ResourceNotFoundException quando colaborador nao existe`() {
        val manager = buildManager()
        every { collaboratorRepository.findById(99L) } returns Optional.empty()

        assertThatThrownBy { collaboratorService.findById(99L, manager) }
            .isInstanceOf(ResourceNotFoundException::class.java)
            .hasMessageContaining("99")
    }

    // ─── create ───────────────────────────────────────────────────────────────

    @Test
    fun `create deve usar org do OPERATOR ignorando organizationId do request`() {
        val org = buildOrg(1L)
        val operator = buildOperator(org)
        // organizationId = 99L (diferente) — deve ser ignorado pelo service
        val request = CollaboratorRequest(
            name = "Novo", cpf = "111.222.333-44",
            email = "novo@test.com", position = "Dev", organizationId = 99L
        )
        val saved = buildCollaborator(1L, org)

        every { organizationRepository.findById(1L) } returns Optional.of(org) // usa org do operador
        every { collaboratorRepository.existsByCpf(request.cpf) } returns false
        every { collaboratorRepository.existsByEmail(request.email) } returns false
        every { collaboratorRepository.save(any()) } returns saved

        val result = collaboratorService.create(request, operator)

        assertThat(result.organizationId).isEqualTo(1L)
        verify { organizationRepository.findById(1L) } // nunca busca org 99
    }

    @Test
    fun `create deve lancar BusinessException quando CPF ja existe`() {
        val org = buildOrg(1L)
        val manager = buildManager(org)
        val request = CollaboratorRequest(
            name = "Dup", cpf = "111.222.333-44",
            email = "dup@test.com", position = "Dev", organizationId = 1L
        )
        every { organizationRepository.findById(1L) } returns Optional.of(org)
        every { collaboratorRepository.existsByCpf(request.cpf) } returns true

        assertThatThrownBy { collaboratorService.create(request, manager) }
            .isInstanceOf(BusinessException::class.java)
            .hasMessageContaining("CPF")
    }

    @Test
    fun `create deve lancar BusinessException quando email ja existe`() {
        val org = buildOrg(1L)
        val manager = buildManager(org)
        val request = CollaboratorRequest(
            name = "Dup", cpf = "000.000.000-01",
            email = "dup@test.com", position = "Dev", organizationId = 1L
        )
        every { organizationRepository.findById(1L) } returns Optional.of(org)
        every { collaboratorRepository.existsByCpf(request.cpf) } returns false
        every { collaboratorRepository.existsByEmail(request.email) } returns true

        assertThatThrownBy { collaboratorService.create(request, manager) }
            .isInstanceOf(BusinessException::class.java)
            .hasMessageContaining("e-mail")
    }

    // ─── delete ───────────────────────────────────────────────────────────────

    @Test
    fun `delete deve lancar ForbiddenException quando OPERATOR tenta excluir colab de outra org`() {
        val userOrg = buildOrg(1L)
        val otherOrg = buildOrg(2L)
        val operator = buildOperator(userOrg)
        every { collaboratorRepository.findById(1L) } returns Optional.of(buildCollaborator(1L, otherOrg))

        assertThatThrownBy { collaboratorService.delete(1L, operator) }
            .isInstanceOf(ForbiddenException::class.java)
    }

    @Test
    fun `delete deve remover colaborador quando OPERATOR tem acesso`() {
        val org = buildOrg(1L)
        val operator = buildOperator(org)
        val collaborator = buildCollaborator(1L, org)
        every { collaboratorRepository.findById(1L) } returns Optional.of(collaborator)
        every { collaboratorRepository.deleteById(1L) } returns Unit

        collaboratorService.delete(1L, operator)

        verify(exactly = 1) { collaboratorRepository.deleteById(1L) }
    }
}
