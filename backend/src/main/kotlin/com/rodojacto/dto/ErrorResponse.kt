package com.rodojacto.dto

import java.time.LocalDateTime

data class ErrorResponse(
    val status: Int,
    val message: String,
    val errors: List<String> = emptyList(),
    val timestamp: LocalDateTime = LocalDateTime.now()
)
