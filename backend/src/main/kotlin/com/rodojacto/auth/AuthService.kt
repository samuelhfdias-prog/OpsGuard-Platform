package com.rodojacto.auth

import com.rodojacto.auth.dto.AuthResponse
import com.rodojacto.auth.dto.LoginRequest
import com.rodojacto.domain.user.UserRepository
import com.rodojacto.security.JwtService
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.AuthenticationException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
    private val authenticationManager: AuthenticationManager,
    private val loginAttemptService: LoginAttemptService
) {

    fun login(request: LoginRequest): AuthResponse {
        val normalizedEmail = request.email.trim().lowercase()
        loginAttemptService.ensureAllowed(normalizedEmail)

        try {
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(normalizedEmail, request.password)
            )
        } catch (ex: AuthenticationException) {
            loginAttemptService.recordFailure(normalizedEmail)
            throw BadCredentialsException("Credenciais inválidas", ex)
        }

        val user = userRepository.findByEmail(normalizedEmail)
            .orElseThrow { IllegalStateException("Usuário não encontrado após autenticação bem-sucedida") }
        loginAttemptService.recordSuccess(normalizedEmail)

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
