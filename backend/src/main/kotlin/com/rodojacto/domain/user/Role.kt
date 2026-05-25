package com.rodojacto.domain.user

/**
 * Perfis de acesso do sistema.
 * - MANAGER: acesso global a todos os registros.
 * - OPERATOR: acesso restrito à sua própria organização.
 */
enum class Role {
    MANAGER,
    OPERATOR
}
