package com.rodojacto.domain.device

import org.springframework.data.jpa.repository.JpaRepository

interface DeviceRepository : JpaRepository<Device, Long> {
    fun findAllByOrganizationId(organizationId: Long): List<Device>
    fun existsBySerialNumber(serialNumber: String): Boolean
    fun existsBySerialNumberAndIdNot(serialNumber: String, id: Long): Boolean
}
