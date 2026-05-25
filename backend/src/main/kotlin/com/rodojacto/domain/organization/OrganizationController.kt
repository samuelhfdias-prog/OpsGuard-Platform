package com.rodojacto.domain.organization

import com.rodojacto.domain.organization.dto.OrganizationRequest
import com.rodojacto.domain.organization.dto.OrganizationResponse
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
@RequestMapping("/api/organizations")
@Tag(name = "Organizações", description = "Gerenciamento de Organizações")
@SecurityRequirement(name = "bearerAuth")
class OrganizationController(
    private val organizationService: OrganizationService
) {

    @GetMapping
    @Operation(summary = "Listar organizações", description = "MANAGER retorna todas. OPERATOR retorna apenas a sua.")
    fun findAll(@AuthenticationPrincipal currentUser: User): ResponseEntity<List<OrganizationResponse>> =
        ResponseEntity.ok(organizationService.findAll(currentUser))

    @GetMapping("/{id}")
    @Operation(summary = "Buscar organização por ID")
    fun findById(
        @PathVariable id: Long,
        @AuthenticationPrincipal currentUser: User
    ): ResponseEntity<OrganizationResponse> =
        ResponseEntity.ok(organizationService.findById(id, currentUser))

    @PostMapping
    @Operation(summary = "Criar organização", description = "Acesso exclusivo: MANAGER")
    fun create(
        @Valid @RequestBody request: OrganizationRequest
    ): ResponseEntity<OrganizationResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(organizationService.create(request))

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar organização", description = "Acesso exclusivo: MANAGER")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: OrganizationRequest
    ): ResponseEntity<OrganizationResponse> =
        ResponseEntity.ok(organizationService.update(id, request))

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir organização", description = "Acesso exclusivo: MANAGER")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        organizationService.delete(id)
        return ResponseEntity.noContent().build()
    }
}
