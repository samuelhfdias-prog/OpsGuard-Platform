package com.rodojacto.domain.collaborator

import com.rodojacto.domain.collaborator.dto.CollaboratorRequest
import com.rodojacto.domain.collaborator.dto.CollaboratorResponse
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
@RequestMapping("/api/collaborators")
@Tag(name = "Colaboradores", description = "Gerenciamento de Colaboradores")
@SecurityRequirement(name = "bearerAuth")
class CollaboratorController(
    private val collaboratorService: CollaboratorService
) {

    @GetMapping
    @Operation(summary = "Listar colaboradores", description = "MANAGER retorna todos. OPERATOR retorna apenas os da sua organização.")
    fun findAll(@AuthenticationPrincipal currentUser: User): ResponseEntity<List<CollaboratorResponse>> =
        ResponseEntity.ok(collaboratorService.findAll(currentUser))

    @GetMapping("/{id}")
    @Operation(summary = "Buscar colaborador por ID")
    fun findById(
        @PathVariable id: Long,
        @AuthenticationPrincipal currentUser: User
    ): ResponseEntity<CollaboratorResponse> =
        ResponseEntity.ok(collaboratorService.findById(id, currentUser))

    @PostMapping
    @Operation(summary = "Criar colaborador", description = "OPERATOR sempre cria na própria organização, independente do organizationId enviado.")
    fun create(
        @Valid @RequestBody request: CollaboratorRequest,
        @AuthenticationPrincipal currentUser: User
    ): ResponseEntity<CollaboratorResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(collaboratorService.create(request, currentUser))

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar colaborador")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: CollaboratorRequest,
        @AuthenticationPrincipal currentUser: User
    ): ResponseEntity<CollaboratorResponse> =
        ResponseEntity.ok(collaboratorService.update(id, request, currentUser))

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir colaborador")
    fun delete(
        @PathVariable id: Long,
        @AuthenticationPrincipal currentUser: User
    ): ResponseEntity<Void> {
        collaboratorService.delete(id, currentUser)
        return ResponseEntity.noContent().build()
    }
}
