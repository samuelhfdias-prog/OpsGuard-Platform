package com.rodojacto.config

import com.rodojacto.domain.collaborator.Collaborator
import com.rodojacto.domain.collaborator.CollaboratorRepository
import com.rodojacto.domain.device.Device
import com.rodojacto.domain.device.DeviceRepository
import com.rodojacto.domain.device.DeviceType
import com.rodojacto.domain.organization.Organization
import com.rodojacto.domain.organization.OrganizationRepository
import com.rodojacto.domain.user.Role
import com.rodojacto.domain.user.User
import com.rodojacto.domain.user.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * Seed de dados iniciais executado na inicialização da aplicação.
 *
 * DECISÃO ARQUITETURAL: Usamos ApplicationRunner em vez de Flyway SQL puro (V5)
 * porque o BCryptPasswordEncoder precisa ser injetado pelo Spring para gerar os hashes
 * de forma segura e idiomática. Inserir hashes BCrypt hardcoded em SQL é uma prática
 * frágil e não reproduzível.
 *
 * O seeder é idempotente: não executa se já houver dados no banco.
 */
@Component
class DataSeeder(
    private val organizationRepository: OrganizationRepository,
    private val userRepository: UserRepository,
    private val collaboratorRepository: CollaboratorRepository,
    private val deviceRepository: DeviceRepository,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${app.seed.enabled:false}") private val seedEnabled: Boolean,
    @Value("\${app.seed.manager-password:}") private val managerPassword: String,
    @Value("\${app.seed.operator-password:}") private val operatorPassword: String
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(DataSeeder::class.java)

    @Transactional
    override fun run(args: ApplicationArguments) {
        if (!seedEnabled) {
            log.info("Seed de demonstração desabilitado.")
            return
        }
        require(managerPassword.length >= 10 && operatorPassword.length >= 10) {
            "As senhas do seed devem ter ao menos 10 caracteres"
        }

        if (organizationRepository.count() > 0) {
            log.info("Banco de dados já populado. Seed ignorado.")
            return
        }

        log.info("═══════════════════════════════════════════")
        log.info("  Iniciando seed do banco de dados...")
        log.info("═══════════════════════════════════════════")

        // ─── Organizações ───────────────────────────────────────────────────
        val orgLogistica = organizationRepository.save(
            Organization(
                name = "Logística Brasil Ltda",
                cnpj = "12.345.678/0001-90",
                address = "Av. Brasil, 1000, São Paulo - SP"
            )
        )
        val orgTransporte = organizationRepository.save(
            Organization(
                name = "TransporteMax S.A.",
                cnpj = "98.765.432/0001-10",
                address = "Rua das Flores, 500, Curitiba - PR"
            )
        )

        // ─── Usuários ────────────────────────────────────────────────────────
        userRepository.saveAll(
            listOf(
                User(
                    name = "Admin Manager",
                    email = "manager@opsguard.dev",
                    password = passwordEncoder.encode(managerPassword),
                    role = Role.MANAGER,
                    organization = orgLogistica
                ),
                User(
                    name = "Operador Logística",
                    email = "operator1@opsguard.dev",
                    password = passwordEncoder.encode(operatorPassword),
                    role = Role.OPERATOR,
                    organization = orgLogistica
                ),
                User(
                    name = "Operador Transporte",
                    email = "operator2@opsguard.dev",
                    password = passwordEncoder.encode(operatorPassword),
                    role = Role.OPERATOR,
                    organization = orgTransporte
                )
            )
        )

        // ─── Colaboradores ───────────────────────────────────────────────────
        collaboratorRepository.saveAll(
            listOf(
                Collaborator(
                    name = "João Silva",
                    cpf = "123.456.789-00",
                    email = "joao.silva@logistica.com",
                    position = "Motorista",
                    organization = orgLogistica
                ),
                Collaborator(
                    name = "Maria Santos",
                    cpf = "987.654.321-00",
                    email = "maria.santos@logistica.com",
                    position = "Despachante",
                    organization = orgLogistica
                ),
                Collaborator(
                    name = "Carlos Oliveira",
                    cpf = "111.222.333-44",
                    email = "carlos.oliveira@transportemax.com",
                    position = "Coordenador de Frota",
                    organization = orgTransporte
                ),
                Collaborator(
                    name = "Ana Costa",
                    cpf = "555.666.777-88",
                    email = "ana.costa@transportemax.com",
                    position = "Motorista",
                    organization = orgTransporte
                )
            )
        )

        // ─── Dispositivos ────────────────────────────────────────────────────
        deviceRepository.saveAll(
            listOf(
                Device(
                    name = "Smartphone Samsung Galaxy A54",
                    serialNumber = "SN-LOG-001",
                    type = DeviceType.SMARTPHONE,
                    organization = orgLogistica
                ),
                Device(
                    name = "Tablet Samsung Tab S7",
                    serialNumber = "SN-LOG-002",
                    type = DeviceType.TABLET,
                    organization = orgLogistica
                ),
                Device(
                    name = "Rastreador Queclink GV600",
                    serialNumber = "SN-TRX-001",
                    type = DeviceType.TRACKER,
                    organization = orgTransporte
                ),
                Device(
                    name = "Laptop Dell Latitude 5420",
                    serialNumber = "SN-TRX-002",
                    type = DeviceType.LAPTOP,
                    organization = orgTransporte
                )
            )
        )

        log.info("Seed concluído com usuários de demonstração configurados por ambiente.")
    }
}
