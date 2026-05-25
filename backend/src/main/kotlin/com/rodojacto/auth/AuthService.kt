package com.rodojacto.auth

import com.rodojacto.auth.dto.AuthResponse
import com.rodojacto.auth.dto.LoginRequest
import com.rodojacto.domain.user.UserRepository
import com.rodojacto.security.JwtService
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
    private val authenticationManager: AuthenticationManager
) {

    fun login(request: LoginRequest): AuthResponse {
        // O AuthenticationManager lança BadCredentialsException se inválido (tratado pelo GlobalExceptionHandler)
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.email, request.password)
        )

        val user = userRepository.findByEmail(request.email)
            .orElseThrow { IllegalStateException("Usuário não encontrado após autenticação bem-sucedida") }

        val extraClaims = mapOf(
            "role" to user.role.name,
            "organizationId" to (user.organization?.id ?: ""),
            "name" to user.name
        )

        val token = jwtService.generateToken(user, extraClaims)

        return AuthResponse(
            token = token,
            email = user.email,
            name = user.name,
            role = user.role.name,
            organizationId = user.organization?.id
        )
    }
}
