package com.rodojacto.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.rodojacto.dto.ErrorResponse
import com.rodojacto.security.JwtAuthFilter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter
import org.springframework.security.web.header.writers.StaticHeadersWriter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

/**
 * Configuração central do Spring Security.
 * - Stateless (JWT) — sem HttpSession
 * - CSRF desabilitado (APIs REST stateless não precisam)
 * - CORS configurado para permitir o frontend Angular
 * - @EnableMethodSecurity ativa @PreAuthorize nos Services
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val jwtAuthFilter: JwtAuthFilter,
    private val userDetailsService: UserDetailsService,
    private val objectMapper: ObjectMapper,
    @Value("\${security.cors.allowed-origins:}") private val configuredAllowedOrigins: String
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .headers { headers ->
                headers.frameOptions { it.deny() }
                headers.referrerPolicy { it.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER) }
                headers.addHeaderWriter(
                    StaticHeadersWriter("Permissions-Policy", "camera=(), geolocation=(), microphone=(), payment=(), usb=()")
                )
                headers.httpStrictTransportSecurity { hsts ->
                    hsts.includeSubDomains(true).maxAgeInSeconds(31536000)
                }
            }
            .authorizeHttpRequests { auth ->
                auth.requestMatchers("/api/auth/**").permitAll()
                auth.requestMatchers(
                    "/swagger-ui/**",
                    "/api-docs/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**"
                ).permitAll()
                auth.requestMatchers(HttpMethod.GET, "/api/health").permitAll()
                auth.anyRequest().authenticated()
            }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .exceptionHandling { exceptions ->
                exceptions.authenticationEntryPoint { _, response, _ ->
                    response.status = HttpStatus.UNAUTHORIZED.value()
                    response.contentType = "application/json"
                    response.characterEncoding = "UTF-8"
                    objectMapper.writeValue(
                        response.writer,
                        ErrorResponse(response.status, "Autenticação necessária ou token inválido")
                    )
                }
                exceptions.accessDeniedHandler { _, response, _ ->
                    response.status = HttpStatus.FORBIDDEN.value()
                    response.contentType = "application/json"
                    response.characterEncoding = "UTF-8"
                    objectMapper.writeValue(response.writer, ErrorResponse(response.status, "Acesso negado"))
                }
            }
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOrigins = configuredAllowedOrigins.split(',')
                .map(String::trim)
                .filter(String::isNotEmpty)
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
            allowedHeaders = listOf("Accept", "Authorization", "Content-Type", "X-Requested-With")
            exposedHeaders = listOf("Retry-After")
            allowCredentials = false
            maxAge = 3600L
        }
        return UrlBasedCorsConfigurationSource().also {
            it.registerCorsConfiguration("/api/**", configuration)
        }
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder(12)

    @Bean
    fun authenticationProvider(): AuthenticationProvider {
        val provider = DaoAuthenticationProvider()
        provider.setUserDetailsService(userDetailsService)
        provider.setPasswordEncoder(passwordEncoder())
        return provider
    }

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager =
        config.authenticationManager
}
