package com.rodojacto.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

/**
 * Serviço responsável pela geração e validação de tokens JWT.
 * Utiliza JJWT 0.12.x com assinatura HMAC-SHA256.
 */
@Service
class JwtService {

    @Value("\${security.jwt.secret-key}")
    private lateinit var secretKey: String

    @Value("\${security.jwt.expiration}")
    private var jwtExpiration: Long = 86400000L

    @Value("\${security.jwt.issuer}")
    private lateinit var issuer: String

    fun generateToken(userDetails: UserDetails, extraClaims: Map<String, Any> = emptyMap()): String {
        return Jwts.builder()
            .claims(extraClaims)
            .subject(userDetails.username)
            .issuer(issuer)
            .id(UUID.randomUUID().toString())
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + jwtExpiration))
            .signWith(getSigningKey())
            .compact()
    }

    fun extractUsername(token: String): String = extractClaim(token) { it.subject }

    fun isTokenValid(token: String, userDetails: UserDetails): Boolean {
        val username = extractUsername(token)
        return username == userDetails.username && !isTokenExpired(token)
    }

    private fun isTokenExpired(token: String): Boolean =
        extractClaim(token) { it.expiration }.before(Date())

    private fun <T> extractClaim(token: String, resolver: (Claims) -> T): T {
        val claims = Jwts.parser()
            .verifyWith(getSigningKey())
            .requireIssuer(issuer)
            .build()
            .parseSignedClaims(token)
            .payload
        return resolver(claims)
    }

    private fun getSigningKey(): SecretKey {
        val keyBytes = Decoders.BASE64.decode(secretKey)
        return Keys.hmacShaKeyFor(keyBytes)
    }
}
