package com.rodojacto.domain.organization

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "organizations")
class Organization(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 100)
    var name: String,

    @Column(nullable = false, unique = true, length = 18)
    var cnpj: String,

    @Column(length = 200)
    var address: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
