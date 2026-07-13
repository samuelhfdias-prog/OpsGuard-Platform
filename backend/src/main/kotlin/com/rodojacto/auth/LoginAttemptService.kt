package com.rodojacto.auth

import com.rodojacto.exception.TooManyRequestsException
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@Service
class LoginAttemptService(
    private val clock: Clock = Clock.systemUTC()
) {
    private data class AttemptWindow(val failures: Int, val startedAt: Instant)

    private val attempts = ConcurrentHashMap<String, AttemptWindow>()
    private val window = Duration.ofMinutes(15)
    private val maxFailures = 5
    private val maxTrackedEmails = 10_000

    fun ensureAllowed(email: String) {
        val current = attempts[email] ?: return
        if (Duration.between(current.startedAt, Instant.now(clock)) >= window) {
            attempts.remove(email, current)
            return
        }
        if (current.failures >= maxFailures) {
            throw TooManyRequestsException("Muitas tentativas de acesso. Aguarde 15 minutos e tente novamente")
        }
    }

    fun recordFailure(email: String) {
        val now = Instant.now(clock)
        if (attempts.size >= maxTrackedEmails && !attempts.containsKey(email)) {
            attempts.entries.removeIf { Duration.between(it.value.startedAt, now) >= window }
            if (attempts.size >= maxTrackedEmails) {
                throw TooManyRequestsException("Serviço de autenticação temporariamente limitado. Tente novamente em alguns minutos")
            }
        }
        attempts.compute(email) { _, current ->
            if (current == null || Duration.between(current.startedAt, now) >= window) {
                AttemptWindow(1, now)
            } else {
                current.copy(failures = current.failures + 1)
            }
        }
    }

    fun recordSuccess(email: String) {
        attempts.remove(email)
    }
}
