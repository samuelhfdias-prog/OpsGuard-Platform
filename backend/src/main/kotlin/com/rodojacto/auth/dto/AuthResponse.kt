package com.rodojacto.auth.dto

data class AuthResponse(
    val token: String,
    val type: String = "Bearer",
    val email: String,
    val name: String,
    val role: String,
    val organizationId: Long?
)
