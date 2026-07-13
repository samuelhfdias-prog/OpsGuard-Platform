package com.rodojacto.auth

import com.rodojacto.auth.dto.LoginRequest
import com.rodojacto.domain.organization.Organization
import com.rodojacto.domain.user.Role
import com.rodojacto.domain.user.User
import com.rodojacto.domain.user.UserRepository
import com.rodojacto.security.JwtService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import java.util.Optional

@ExtendWith(MockKExtension::class)
class AuthServiceTest {

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var jwtService: JwtService

    @MockK
    private lateinit var authenticationManager: AuthenticationManager

    @MockK(relaxed = true)
    private lateinit var loginAttemptService: LoginAttemptService

    @InjectMockKs
    private lateinit var authService: AuthService

    private val testOrg = Organization(id = 1L, name = "Test Org", cnpj = "00.000.000/0001-00")

    private val testUser = User(
        id = 1L,
        name = "Test Manager",
        email = "manager@test.com",
        password = "hashedPass",
        role = Role.MANAGER,
        organization = testOrg
    )

    @Test
    fun `login deve retornar AuthResponse com token quando credenciais sao validas`() {
        val request = LoginRequest(email = "manager@test.com", password = "Manager@123")
        every { authenticationManager.authenticate(any()) } returns
                UsernamePasswordAuthenticationToken(testUser, null)
        every { userRepository.findByEmail(request.email) } returns Optional.of(testUser)
        every { jwtService.generateToken(testUser, any()) } returns "mocked.jwt.token"

        val result = authService.login(request)

        assertThat(result.token).isEqualTo("mocked.jwt.token")
        assertThat(result.email).isEqualTo("manager@test.com")
        assertThat(result.role).isEqualTo("MANAGER")
        assertThat(result.organizationId).isEqualTo(1L)
        assertThat(result.type).isEqualTo("Bearer")
        verify(exactly = 1) { authenticationManager.authenticate(any()) }
        verify(exactly = 1) { jwtService.generateToken(testUser, any()) }
    }

    @Test
    fun `login deve propagar BadCredentialsException quando credenciais sao invalidas`() {
        val request = LoginRequest(email = "manager@test.com", password = "wrong_pass")
        every { authenticationManager.authenticate(any()) } throws BadCredentialsException("Bad credentials")

        assertThatThrownBy { authService.login(request) }
            .isInstanceOf(BadCredentialsException::class.java)

        verify(exactly = 0) { userRepository.findByEmail(any()) }
        verify(exactly = 0) { jwtService.generateToken(any(), any()) }
    }

    @Test
    fun `login deve incluir organizationId nulo no response quando usuario nao tem org`() {
        val userWithoutOrg = User(
            id = 2L, name = "Sem Org", email = "semorg@test.com",
            password = "hash", role = Role.MANAGER, organization = null
        )
        val request = LoginRequest(email = "semorg@test.com", password = "pass")
        every { authenticationManager.authenticate(any()) } returns
                UsernamePasswordAuthenticationToken(userWithoutOrg, null)
        every { userRepository.findByEmail(request.email) } returns Optional.of(userWithoutOrg)
        every { jwtService.generateToken(userWithoutOrg, any()) } returns "token"

        val result = authService.login(request)

        assertThat(result.organizationId).isNull()
    }
}
