package com.rodojacto.domain.organization

import com.rodojacto.domain.organization.dto.OrganizationRequest
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
class OrganizationServiceTest {

    @MockK
    private lateinit var organizationRepository: OrganizationRepository

    @InjectMockKs
    private lateinit var organizationService: OrganizationService

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun buildOrg(id: Long = 1L, name: String = "Org $id", cnpj: String = "00.000.00$id/0001-0$id") =
        Organization(id = id, name = name, cnpj = cnpj)

    private fun buildManager(org: Organization? = null) =
        User(id = 1L, name = "Manager", email = "manager@test.com", password = "hash", role = Role.MANAGER, organization = org)

    private fun buildOperator(org: Organization) =
        User(id = 2L, name = "Operator", email = "op@test.com", password = "hash", role = Role.OPERATOR, organization = org)

    // ─── findAll ──────────────────────────────────────────────────────────────

    @Test
    fun `findAll deve retornar todas as organizacoes quando usuario e MANAGER`() {
        val manager = buildManager()
        val orgs = listOf(buildOrg(1L), buildOrg(2L))
        every { organizationRepository.findAll() } returns orgs

        val result = organizationService.findAll(manager)

        assertThat(result).hasSize(2)
        verify(exactly = 1) { organizationRepository.findAll() }
    }

    @Test
    fun `findAll deve retornar apenas a propria organizacao quando usuario e OPERATOR`() {
        val org = buildOrg(1L)
        val operator = buildOperator(org)

        val result = organizationService.findAll(operator)

        assertThat(result).hasSize(1)
        assertThat(result[0].id).isEqualTo(1L)
        verify(exactly = 0) { organizationRepository.findAll() }
    }

    // ─── findById ─────────────────────────────────────────────────────────────

    @Test
    fun `findById deve retornar organizacao quando MANAGER busca qualquer org`() {
        val org = buildOrg(1L)
        val manager = buildManager()
        every { organizationRepository.findById(1L) } returns Optional.of(org)

        val result = organizationService.findById(1L, manager)

        assertThat(result.id).isEqualTo(1L)
    }

    @Test
    fun `findById deve retornar propria organizacao quando OPERATOR busca a sua`() {
        val org = buildOrg(1L)
        val operator = buildOperator(org)
        every { organizationRepository.findById(1L) } returns Optional.of(org)

        val result = organizationService.findById(1L, operator)

        assertThat(result.id).isEqualTo(1L)
    }

    @Test
    fun `findById deve lancar ForbiddenException quando OPERATOR tenta acessar outra org`() {
        val userOrg = buildOrg(1L)
        val otherOrg = buildOrg(2L)
        val operator = buildOperator(userOrg)
        every { organizationRepository.findById(2L) } returns Optional.of(otherOrg)

        assertThatThrownBy { organizationService.findById(2L, operator) }
            .isInstanceOf(ForbiddenException::class.java)
            .hasMessageContaining("2")
    }

    @Test
    fun `findById deve lancar ResourceNotFoundException quando organizacao nao existe`() {
        val manager = buildManager()
        every { organizationRepository.findById(99L) } returns Optional.empty()

        assertThatThrownBy { organizationService.findById(99L, manager) }
            .isInstanceOf(ResourceNotFoundException::class.java)
            .hasMessageContaining("99")
    }

    // ─── create ───────────────────────────────────────────────────────────────

    @Test
    fun `create deve salvar e retornar organizacao quando CNPJ e unico`() {
        val request = OrganizationRequest(name = "Nova Org", cnpj = "11.111.111/0001-11")
        val saved = buildOrg(1L, "Nova Org", "11.111.111/0001-11")
        every { organizationRepository.existsByCnpj(request.cnpj) } returns false
        every { organizationRepository.save(any()) } returns saved

        val result = organizationService.create(request)

        assertThat(result.name).isEqualTo("Nova Org")
        assertThat(result.cnpj).isEqualTo("11.111.111/0001-11")
    }

    @Test
    fun `create deve lancar BusinessException quando CNPJ ja existe`() {
        val request = OrganizationRequest(name = "Org Dup", cnpj = "11.111.111/0001-11")
        every { organizationRepository.existsByCnpj(request.cnpj) } returns true

        assertThatThrownBy { organizationService.create(request) }
            .isInstanceOf(BusinessException::class.java)
            .hasMessageContaining("CNPJ")
    }

    // ─── delete ───────────────────────────────────────────────────────────────

    @Test
    fun `delete deve excluir organizacao quando ela existe`() {
        every { organizationRepository.existsById(1L) } returns true
        every { organizationRepository.deleteById(1L) } returns Unit

        organizationService.delete(1L)

        verify(exactly = 1) { organizationRepository.deleteById(1L) }
    }

    @Test
    fun `delete deve lancar ResourceNotFoundException quando organizacao nao existe`() {
        every { organizationRepository.existsById(99L) } returns false

        assertThatThrownBy { organizationService.delete(99L) }
            .isInstanceOf(ResourceNotFoundException::class.java)
            .hasMessageContaining("99")
    }
}
