package com.rodojacto.domain.organization

import org.springframework.data.jpa.repository.JpaRepository

interface OrganizationRepository : JpaRepository<Organization, Long> {
    fun existsByCnpj(cnpj: String): Boolean
    fun existsByCnpjAndIdNot(cnpj: String, id: Long): Boolean
}
