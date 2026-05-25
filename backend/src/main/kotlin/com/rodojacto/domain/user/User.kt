package com.rodojacto.domain.user

import com.rodojacto.domain.organization.Organization
import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

/**
 * Entidade de usuário que implementa UserDetails do Spring Security.
 * Decisão: implementar UserDetails diretamente na entidade evita a criação
 * de uma classe adaptadora extra, mantendo a codebase enxuta para este escopo.
 */
@Entity
@Table(name = "users")
class User(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 100)
    val name: String,

    @Column(nullable = false, unique = true, length = 100)
    val email: String,

    @Column(nullable = false)
    private val password: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val role: Role,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "organization_id")
    val organization: Organization? = null

) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> =
        listOf(SimpleGrantedAuthority("ROLE_${role.name}"))

    override fun getPassword(): String = password

    /** O Spring Security usa o email como "username". */
    override fun getUsername(): String = email

    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = true
}
