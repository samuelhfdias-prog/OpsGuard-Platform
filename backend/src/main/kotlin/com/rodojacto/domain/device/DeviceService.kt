package com.rodojacto.domain.device

import com.rodojacto.domain.device.dto.DeviceRequest
import com.rodojacto.domain.device.dto.DeviceResponse
import com.rodojacto.domain.device.dto.toResponse
import com.rodojacto.domain.organization.OrganizationRepository
import com.rodojacto.domain.user.Role
import com.rodojacto.domain.user.User
import com.rodojacto.exception.BusinessException
import com.rodojacto.exception.ForbiddenException
import com.rodojacto.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class DeviceService(
    private val deviceRepository: DeviceRepository,
    private val organizationRepository: OrganizationRepository
) {

    fun findAll(currentUser: User): List<DeviceResponse> {
        return when (currentUser.role) {
            Role.MANAGER -> deviceRepository.findAll()
            Role.OPERATOR -> {
                val orgId = currentUser.organization?.id
                    ?: throw BusinessException("Operador não possui organização vinculada")
                deviceRepository.findAllByOrganizationId(orgId)
            }
        }.map { it.toResponse() }
    }

    fun findById(id: Long, currentUser: User): DeviceResponse {
        val device = deviceRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Dispositivo com ID $id não encontrado") }
        checkAccess(device, currentUser)
        return device.toResponse()
    }

    @Transactional
    fun create(request: DeviceRequest, currentUser: User): DeviceResponse {
        val serialNumber = request.serialNumber.trim().uppercase()
        val effectiveOrgId = resolveOrganizationId(request.organizationId, currentUser)

        val organization = organizationRepository.findById(effectiveOrgId)
            .orElseThrow { ResourceNotFoundException("Organização com ID $effectiveOrgId não encontrada") }

        if (deviceRepository.existsBySerialNumber(serialNumber)) {
            throw BusinessException("Já existe um dispositivo com o número de série $serialNumber")
        }

        val device = Device(
            name = request.name.trim(),
            serialNumber = serialNumber,
            type = request.type,
            organization = organization
        )
        return deviceRepository.save(device).toResponse()
    }

    @Transactional
    fun update(id: Long, request: DeviceRequest, currentUser: User): DeviceResponse {
        val serialNumber = request.serialNumber.trim().uppercase()
        val device = deviceRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Dispositivo com ID $id não encontrado") }

        checkAccess(device, currentUser)

        val effectiveOrgId = when (currentUser.role) {
            Role.MANAGER -> request.organizationId
            Role.OPERATOR -> device.organization.id!!
        }

        val organization = organizationRepository.findById(effectiveOrgId)
            .orElseThrow { ResourceNotFoundException("Organização com ID $effectiveOrgId não encontrada") }

        if (deviceRepository.existsBySerialNumberAndIdNot(serialNumber, id)) {
            throw BusinessException("Já existe outro dispositivo com o número de série $serialNumber")
        }

        device.apply {
            name = request.name.trim()
            this.serialNumber = serialNumber
            type = request.type
            this.organization = organization
            updatedAt = LocalDateTime.now()
        }
        return deviceRepository.save(device).toResponse()
    }

    @Transactional
    fun delete(id: Long, currentUser: User) {
        val device = deviceRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Dispositivo com ID $id não encontrado") }
        checkAccess(device, currentUser)
        deviceRepository.deleteById(id)
    }

    // ─── Helpers privados ──────────────────────────────────────────────────────

    private fun resolveOrganizationId(requestedOrgId: Long, currentUser: User): Long =
        when (currentUser.role) {
            Role.MANAGER -> requestedOrgId
            Role.OPERATOR -> currentUser.organization?.id
                ?: throw BusinessException("Operador não possui organização vinculada")
        }

    private fun checkAccess(device: Device, currentUser: User) {
        if (currentUser.role == Role.OPERATOR) {
            val userOrgId = currentUser.organization?.id
                ?: throw BusinessException("Operador não possui organização vinculada")
            if (device.organization.id != userOrgId) {
                throw ForbiddenException("Acesso negado ao dispositivo com ID ${device.id}")
            }
        }
    }
}
