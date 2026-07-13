package com.rodojacto.exception

import com.rodojacto.dto.ErrorResponse
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.MalformedJwtException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * Tratamento global de exceções.
 * Centraliza todos os mapeamentos de erro → HTTP response,
 * evitando try/catch espalhados nos controllers.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleNotFound(ex: ResourceNotFoundException): ResponseEntity<ErrorResponse> {
        log.warn("Recurso não encontrado: ${ex.message}")
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.message!!))
    }

    @ExceptionHandler(BusinessException::class)
    fun handleBusiness(ex: BusinessException): ResponseEntity<ErrorResponse> {
        log.warn("Regra de negócio violada: ${ex.message}")
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(ErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(), ex.message!!))
    }

    @ExceptionHandler(ForbiddenException::class)
    fun handleForbidden(ex: ForbiddenException): ResponseEntity<ErrorResponse> {
        log.warn("Acesso negado: ${ex.message}")
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ErrorResponse(HttpStatus.FORBIDDEN.value(), ex.message!!))
    }

    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentials(ex: BadCredentialsException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "E-mail ou senha inválidos"))
    }

    @ExceptionHandler(TooManyRequestsException::class)
    fun handleTooManyRequests(ex: TooManyRequestsException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
            .header("Retry-After", "900")
            .body(ErrorResponse(HttpStatus.TOO_MANY_REQUESTS.value(), ex.message ?: "Muitas tentativas"))

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrity(ex: DataIntegrityViolationException): ResponseEntity<ErrorResponse> {
        log.warn("Operação rejeitada por integridade referencial")
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ErrorResponse(HttpStatus.CONFLICT.value(), "Operação não permitida: o registro possui vínculos ou dados duplicados"))
    }

    @ExceptionHandler(ExpiredJwtException::class)
    fun handleExpiredJwt(ex: ExpiredJwtException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Token JWT expirado"))
    }

    @ExceptionHandler(MalformedJwtException::class)
    fun handleMalformedJwt(ex: MalformedJwtException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Token JWT inválido"))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.allErrors.map { error ->
            if (error is FieldError) "${error.field}: ${error.defaultMessage}"
            else error.defaultMessage ?: "Erro de validação"
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Erro de validação nos campos", errors))
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneral(ex: Exception): ResponseEntity<ErrorResponse> {
        log.error("Erro inesperado", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Erro interno do servidor"))
    }
}
