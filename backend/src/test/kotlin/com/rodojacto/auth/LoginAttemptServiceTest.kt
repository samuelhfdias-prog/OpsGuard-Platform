package com.rodojacto.auth

import com.rodojacto.exception.TooManyRequestsException
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class LoginAttemptServiceTest {
    private val clock = Clock.fixed(Instant.parse("2026-07-13T12:00:00Z"), ZoneOffset.UTC)

    @Test
    fun `deve bloquear depois de cinco falhas`() {
        val service = LoginAttemptService(clock)
        repeat(5) { service.recordFailure("user@test.com") }

        assertThatThrownBy { service.ensureAllowed("user@test.com") }
            .isInstanceOf(TooManyRequestsException::class.java)
    }

    @Test
    fun `sucesso deve limpar as tentativas anteriores`() {
        val service = LoginAttemptService(clock)
        repeat(5) { service.recordFailure("user@test.com") }
        service.recordSuccess("user@test.com")

        assertThatCode { service.ensureAllowed("user@test.com") }.doesNotThrowAnyException()
    }
}
