package com.rodojacto.domain.device

import com.rodojacto.domain.device.dto.DeviceRequest
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
class DeviceServiceTest {

    @MockK
    private lateinit var deviceRepository: DeviceRepository

    @MockK
    private lateinit var organizationRepository: OrganizationRepository

    @InjectMockKs
    private lateinit var deviceService: DeviceService

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun buildOrg(id: Long = 1L) =
        Organization(id = id, name = "Org $id", cnpj = "00.000.00$id/0001-00")

    private fun buildDevice(id: Long = 1L, org: Organization) =
        Device(id = id, name = "Device $id", serialNumber = "SN-$id", type = DeviceType.SMARTPHONE, organization = org)

    private fun buildManager() =
        User(id = 1L, name = "Manager", email = "m@test.com", password = "hash", role = Role.MANAGER)

    private fun buildOperator(org: Organization) =
        User(id = 2L, name = "Operator", email = "op@test.com", password = "hash", role = Role.OPERATOR, organization = org)

    // ─── findAll ──────────────────────────────────────────────────────────────

    @Test
    fun `findAll deve retornar todos os dispositivos quando MANAGER`() {
        val manager = buildManager()
        val org = buildOrg()
        every { deviceRepository.findAll() } returns listOf(buildDevice(1L, org), buildDevice(2L, org))

        val result = deviceService.findAll(manager)

        assertThat(result).hasSize(2)
        verify(exactly = 1) { deviceRepository.findAll() }
    }

    @Test
    fun `findAll deve retornar apenas dispositivos da propria org quando OPERATOR`() {
        val org = buildOrg(1L)
        val operator = buildOperator(org)
        every { deviceRepository.findAllByOrganizationId(1L) } returns listOf(buildDevice(1L, org))

        val result = deviceService.findAll(operator)

        assertThat(result).hasSize(1)
        verify(exactly = 1) { deviceRepository.findAllByOrganizationId(1L) }
        verify(exactly = 0) { deviceRepository.findAll() }
    }

    // ─── findById ─────────────────────────────────────────────────────────────

    @Test
    fun `findById deve lancar ForbiddenException quando OPERATOR acessa device de outra org`() {
        val userOrg = buildOrg(1L)
        val otherOrg = buildOrg(2L)
        val operator = buildOperator(userOrg)
        every { deviceRepository.findById(1L) } returns Optional.of(buildDevice(1L, otherOrg))

        assertThatThrownBy { deviceService.findById(1L, operator) }
            .isInstanceOf(ForbiddenException::class.java)
    }

    @Test
    fun `findById deve lancar ResourceNotFoundException quando dispositivo nao existe`() {
        val manager = buildManager()
        every { deviceRepository.findById(99L) } returns Optional.empty()

        assertThatThrownBy { deviceService.findById(99L, manager) }
            .isInstanceOf(ResourceNotFoundException::class.java)
            .hasMessageContaining("99")
    }

    // ─── create ───────────────────────────────────────────────────────────────

    @Test
    fun `create deve usar org do OPERATOR ignorando organizationId do request`() {
        val org = buildOrg(1L)
        val operator = buildOperator(org)
        val request = DeviceRequest(name = "Novo", serialNumber = "SN-NEW", type = DeviceType.TABLET, organizationId = 99L)
        val saved = buildDevice(1L, org)

        every { organizationRepository.findById(1L) } returns Optional.of(org)
        every { deviceRepository.existsBySerialNumber(request.serialNumber) } returns false
        every { deviceRepository.save(any()) } returns saved

        val result = deviceService.create(request, operator)

        assertThat(result.organizationId).isEqualTo(1L)
    }

    @Test
    fun `create deve lancar BusinessException quando numero de serie ja existe`() {
        val org = buildOrg(1L)
        val manager = buildManager()
        val request = DeviceRequest(name = "Dup", serialNumber = "SN-DUP", type = DeviceType.LAPTOP, organizationId = 1L)

        every { organizationRepository.findById(1L) } returns Optional.of(org)
        every { deviceRepository.existsBySerialNumber(request.serialNumber) } returns true

        assertThatThrownBy { deviceService.create(request, manager) }
            .isInstanceOf(BusinessException::class.java)
            .hasMessageContaining("série")
    }

    // ─── delete ───────────────────────────────────────────────────────────────

    @Test
    fun `delete deve lancar ForbiddenException quando OPERATOR tenta excluir device de outra org`() {
        val userOrg = buildOrg(1L)
        val otherOrg = buildOrg(2L)
        val operator = buildOperator(userOrg)
        every { deviceRepository.findById(1L) } returns Optional.of(buildDevice(1L, otherOrg))

        assertThatThrownBy { deviceService.delete(1L, operator) }
            .isInstanceOf(ForbiddenException::class.java)
    }

    @Test
    fun `delete deve remover device quando OPERATOR tem acesso`() {
        val org = buildOrg(1L)
        val operator = buildOperator(org)
        val device = buildDevice(1L, org)
        every { deviceRepository.findById(1L) } returns Optional.of(device)
        every { deviceRepository.deleteById(1L) } returns Unit

        deviceService.delete(1L, operator)

        verify(exactly = 1) { deviceRepository.deleteById(1L) }
    }
}
