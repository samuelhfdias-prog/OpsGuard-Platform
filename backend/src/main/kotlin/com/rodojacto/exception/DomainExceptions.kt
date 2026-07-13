package com.rodojacto.exception

/** Violação de regra de negócio (HTTP 422). */
class BusinessException(message: String) : RuntimeException(message)

/** Recurso não encontrado (HTTP 404). */
class ResourceNotFoundException(message: String) : RuntimeException(message)

/** Acesso a recurso de outra organização (HTTP 403). */
class ForbiddenException(message: String = "Acesso negado a este recurso") : RuntimeException(message)

/** Excesso de tentativas em uma operação sensível (HTTP 429). */
class TooManyRequestsException(message: String) : RuntimeException(message)
