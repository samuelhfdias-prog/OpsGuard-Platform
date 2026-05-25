package com.rodojacto.domain.device

import com.rodojacto.domain.organization.Organization
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "devices")
class Device(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 100)
    var name: String,

    @Column(name = "serial_number", nullable = false, unique = true, length = 50)
    var serialNumber: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var type: DeviceType,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "organization_id", nullable = false)
    var organization: Organization,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
