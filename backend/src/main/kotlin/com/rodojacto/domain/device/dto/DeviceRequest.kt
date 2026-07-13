package com.rodojacto.domain.device.dto

import com.rodojacto.domain.device.DeviceType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import jakarta.validation.constraints.Positive

data class DeviceRequest(

    @field:NotBlank(message = "Nome é obrigatório")
    @field:Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    val name: String,

    @field:NotBlank(message = "Número de série é obrigatório")
    @field:Size(max = 50, message = "Número de série deve ter no máximo 50 caracteres")
    val serialNumber: String,

    @field:NotNull(message = "Tipo é obrigatório")
    val type: DeviceType,

    @field:NotNull(message = "ID da organização é obrigatório")
    @field:Positive(message = "ID da organização deve ser positivo")
    val organizationId: Long
)
