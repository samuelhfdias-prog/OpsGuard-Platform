package com.rodojacto.domain.device.dto

import com.rodojacto.domain.device.Device
import com.rodojacto.domain.device.DeviceType
import java.time.LocalDateTime

data class DeviceResponse(
    val id: Long,
    val name: String,
    val serialNumber: String,
    val type: DeviceType,
    val organizationId: Long,
    val organizationName: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

fun Device.toResponse() = DeviceResponse(
    id = id!!,
    name = name,
    serialNumber = serialNumber,
    type = type,
    organizationId = organization.id!!,
    organizationName = organization.name,
    createdAt = createdAt,
    updatedAt = updatedAt
)
