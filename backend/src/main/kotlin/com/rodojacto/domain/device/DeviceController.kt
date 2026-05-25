package com.rodojacto.domain.device

import com.rodojacto.domain.device.dto.DeviceRequest
import com.rodojacto.domain.device.dto.DeviceResponse
import com.rodojacto.domain.user.User
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/devices")
@Tag(name = "Dispositivos", description = "Gerenciamento de Dispositivos")
@SecurityRequirement(name = "bearerAuth")
class DeviceController(
    private val deviceService: DeviceService
) {

    @GetMapping
    @Operation(summary = "Listar dispositivos", description = "MANAGER retorna todos. OPERATOR retorna apenas os da sua organização.")
    fun findAll(@AuthenticationPrincipal currentUser: User): ResponseEntity<List<DeviceResponse>> =
        ResponseEntity.ok(deviceService.findAll(currentUser))

    @GetMapping("/{id}")
    @Operation(summary = "Buscar dispositivo por ID")
    fun findById(
        @PathVariable id: Long,
        @AuthenticationPrincipal currentUser: User
    ): ResponseEntity<DeviceResponse> =
        ResponseEntity.ok(deviceService.findById(id, currentUser))

    @PostMapping
    @Operation(summary = "Criar dispositivo")
    fun create(
        @Valid @RequestBody request: DeviceRequest,
        @AuthenticationPrincipal currentUser: User
    ): ResponseEntity<DeviceResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(deviceService.create(request, currentUser))

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar dispositivo")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: DeviceRequest,
        @AuthenticationPrincipal currentUser: User
    ): ResponseEntity<DeviceResponse> =
        ResponseEntity.ok(deviceService.update(id, request, currentUser))

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir dispositivo")
    fun delete(
        @PathVariable id: Long,
        @AuthenticationPrincipal currentUser: User
    ): ResponseEntity<Void> {
        deviceService.delete(id, currentUser)
        return ResponseEntity.noContent().build()
    }
}
