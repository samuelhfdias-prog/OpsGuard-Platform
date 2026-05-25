# 🚛 Desafio Rodojacto — Sistema de Gestão Full Stack

> Desafio técnico para a vaga **Full Stack Developer** na Rodojacto.
> Aplicação de gestão de **Organizações**, **Colaboradores** e **Dispositivos** com controle de acesso baseado em perfis JWT.

---

## 🏗️ Stack Tecnológica

| Camada | Tecnologia |
|---|---|
| Back-end | Kotlin + Spring Boot 3.2 |
| Segurança | Spring Security + JWT (jjwt 0.12) |
| Persistência | Spring Data JPA + Hibernate |
| Migrações | Flyway |
| Banco de Dados | MySQL 8.0 (H2 in-memory em dev) |
| Front-end | Angular 21 (Standalone Components) |
| Infraestrutura | Docker + Docker Compose |
| Testes | JUnit 5 + MockK |
| Documentação API | SpringDoc OpenAPI (Swagger UI) |

---

## 🚀 Como Rodar o Projeto

### Pré-requisitos
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) instalado e rodando
- Java 17+ (para rodar o backend sem Docker)
- Node.js 18+ (para rodar o frontend sem Docker)

---

### ✅ Opção 1 — Docker Compose (Recomendado)

> **⚠️ ATENÇÃO IMPORTANTE**: Antes de rodar os comandos abaixo, certifique-se de que o **Docker Desktop está ABERTO e RODANDO** no seu computador. Se o ícone da baleia não estiver ativo ao lado do relógio do Windows, o comando falhará com erro de conexão.

```bash
# Clone o repositório
git clone <url-do-repositorio>
cd rodojacto-challenge

# Suba toda a infraestrutura com um único comando
docker-compose up --build
```

Aguarde os serviços iniciarem. A ordem de boot é garantida pelo `healthcheck` do MySQL:

| Serviço | Porta | URL |
|---|---|---|
| API Spring Boot | 8080 | http://localhost:8080 |
| Swagger UI | 8080 | http://localhost:8080/swagger-ui.html |
| MySQL | 3306 | localhost:3306/rodojacto_db |

---

### 🛠️ Opção 2 — Desenvolvimento Local

**1. Suba apenas o MySQL:**
```bash
docker-compose up mysql -d
```

**2. Rode o backend:**
```bash
cd backend
./gradlew bootRun
```

**3. Rode o frontend:**
```bash
cd frontend
npm install
npm start
```

O frontend estará disponível em **http://localhost:4200/**

---

## 🔐 Credenciais de Acesso (Seed Automático)

O seed é executado automaticamente na primeira inicialização da aplicação.

| E-mail | Senha | Perfil | Organização |
|---|---|---|---|
| `manager@rodojacto.com` | `Manager@123` | **MANAGER** | Logística Brasil Ltda |
| `operator1@rodojacto.com` | `Operator@123` | **OPERATOR** | Logística Brasil Ltda |
| `operator2@rodojacto.com` | `Operator@123` | **OPERATOR** | TransporteMax S.A. |

---

## 📋 Endpoints da API

### Autenticação
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "manager@rodojacto.com",
  "password": "Manager@123"
}
```

### Organizações
```
GET    /api/organizations           → Lista organizações (filtrado por perfil)
GET    /api/organizations/{id}      → Busca por ID
POST   /api/organizations           → Cria organização (MANAGER only)
PUT    /api/organizations/{id}      → Atualiza organização (MANAGER only)
DELETE /api/organizations/{id}      → Exclui organização (MANAGER only)
```

### Colaboradores
```
GET    /api/collaborators           → Lista colaboradores (filtrado por perfil)
GET    /api/collaborators/{id}      → Busca por ID
POST   /api/collaborators           → Cria colaborador
PUT    /api/collaborators/{id}      → Atualiza colaborador
DELETE /api/collaborators/{id}      → Exclui colaborador
```

### Dispositivos
```
GET    /api/devices                 → Lista dispositivos (filtrado por perfil)
GET    /api/devices/{id}            → Busca por ID
POST   /api/devices                 → Cria dispositivo
PUT    /api/devices/{id}            → Atualiza dispositivo
DELETE /api/devices/{id}            → Exclui dispositivo
```

### Utilitários
```
GET    /api/health                  → Health check (público)
GET    /swagger-ui.html             → Documentação interativa
```

---

## 🔒 Regras de Controle de Acesso

O isolamento de dados por perfil é implementado **na camada de Service** (backend), não apenas no frontend. Isso garante segurança real — mesmo via Postman/curl, as regras são aplicadas.

| Operação | MANAGER | OPERATOR |
|---|---|---|
| Ver todas as organizações | ✅ | ❌ (apenas a própria) |
| Criar/Editar/Excluir organizações | ✅ | ❌ |
| Ver todos os colaboradores | ✅ | ❌ (apenas da própria org) |
| Criar colaborador em qualquer org | ✅ | ❌ (sempre na própria org) |
| Editar/Excluir colaborador de outra org | ✅ | ❌ (HTTP 403) |
| Ver todos os dispositivos | ✅ | ❌ (apenas da própria org) |
| Criar dispositivo em qualquer org | ✅ | ❌ (sempre na própria org) |
| Editar/Excluir dispositivo de outra org | ✅ | ❌ (HTTP 403) |

---

## 🧪 Executando os Testes

```bash
cd backend
./gradlew test
```

Os testes unitários (JUnit 5 + MockK) cobrem os Services com os cenários críticos de negócio:
- Isolamento de dados por perfil (MANAGER vs OPERATOR)
- Validações de unicidade (CNPJ, CPF, e-mail, número de série)
- Comportamento de exceções (404, 403, 422)

---

## 🏛️ Decisões Arquiteturais

### 1. Arquitetura Domain-Driven por Pacotes
Cada domínio (`organization`, `collaborator`, `device`) possui seu próprio pacote com `Entity`, `Repository`, `Service` e `Controller`. Promove coesão e facilita manutenção.

### 2. Controle de Acesso na Camada de Service
O filtro por organização para o perfil `OPERATOR` é aplicado na camada de `Service`, não apenas via UI. Mesmo acessando a API diretamente (Postman, curl), um operador não consegue visualizar ou modificar dados de outra organização. O `@AuthenticationPrincipal` injeta o `User` completo (com `organizationId`) sem chamada extra ao banco.

### 3. Flyway para Migrações de Schema
Todos os scripts DDL são versionados com Flyway, garantindo rastreabilidade e reproducibilidade em qualquer ambiente. O `ddl-auto: validate` no Hibernate garante que o schema seja validado contra as entidades, sem alterações automáticas.

### 4. DataSeeder via ApplicationRunner (não SQL puro)
O seed de dados usa um componente Spring (`ApplicationRunner`) em vez de um script SQL no Flyway. Isso permite o uso do `BCryptPasswordEncoder` injetado pelo Spring para gerar os hashes de senha de forma segura e idiomática. Inserir hashes BCrypt hardcoded em SQL seria frágil e dificilmente reproduzível.

### 5. JWT Stateless (sem Sessão)
A autenticação é completamente stateless: o backend não mantém `HttpSession`. Cada requisição carrega o token JWT no header `Authorization: Bearer <token>`, que contém `role` e `organizationId` como claims extras.

### 6. Multi-Stage Docker Build
O `Dockerfile` do backend usa dois estágios: `eclipse-temurin:17-jdk-alpine` para compilar e `eclipse-temurin:17-jre-alpine` para o runtime, reduzindo significativamente o tamanho da imagem final.

---

## ⚠️ Nota sobre SGDBs — MySQL vs PostgreSQL

O edital do desafio cita **PostgreSQL** na seção de entrega, mas a stack principal não especifica o banco de dados relacional.

**Decisão tomada**: Implementamos com **MySQL 8.0** com a seguinte justificativa:

1. **Facilidade de migração**: O Flyway suporta ambos nativamente. Para migrar para PostgreSQL bastaria:
   - Trocar `com.mysql:mysql-connector-j` por `org.postgresql:postgresql` no `build.gradle.kts`
   - Atualizar a URL do datasource no `application.yml` (`jdbc:postgresql://...`)
   - Ajustar diferenças de sintaxe SQL (`AUTO_INCREMENT` → `SERIAL`, `ENGINE=InnoDB` removido)
   - Trocar `flyway-mysql` por sem dependência extra (PostgreSQL é suportado nativamente pelo flyway-core)

2. **Troca de imagem Docker**: `mysql:8.0` → `postgres:16` no `docker-compose.yml`

3. **Tempo de migração estimado**: < 30 minutos para um desenvolvedor experiente.

Se preferir PostgreSQL, posso realizar essa migração imediatamente.

---

## 📁 Estrutura do Projeto

```
rodojacto-challenge/
├── docker-compose.yml
├── README.md
├── .gitignore
│
├── backend/
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   ├── Dockerfile
│   └── src/
│       ├── main/kotlin/com/rodojacto/
│       │   ├── RodojactoApplication.kt
│       │   ├── auth/                    # Login + JWT
│       │   ├── config/                  # SecurityConfig, OpenApiConfig, DataSeeder
│       │   ├── controller/              # HealthController
│       │   ├── domain/
│       │   │   ├── organization/        # Entity + Repository + Service + Controller + DTOs
│       │   │   ├── collaborator/
│       │   │   ├── device/
│       │   │   └── user/
│       │   ├── dto/                     # ErrorResponse
│       │   ├── exception/               # Exceções de domínio + GlobalExceptionHandler
│       │   └── security/               # JwtService + JwtAuthFilter + UserDetailsServiceImpl
│       └── resources/
│           ├── application.yml
│           ├── application-docker.yml
│           └── db/migration/
│               ├── V1__create_organizations.sql
│               ├── V2__create_users.sql
│               ├── V3__create_collaborators.sql
│               └── V4__create_devices.sql
│
└── frontend/                            # Angular 21 (Standalone Components)
    ├── public/
    │   └── rodojacto-logo.png           # Logo oficial da Rodojacto
    └── src/
        ├── index.html
        ├── styles.css                   # Design system Rodojacto (tokens, componentes globais)
        └── app/
            ├── app.config.ts            # provideHttpClient + jwtInterceptor
            ├── app.routes.ts            # Rotas lazy-loaded + authGuard
            ├── core/
            │   ├── guards/              # authGuard, managerGuard
            │   ├── interceptors/        # jwtInterceptor (injeta Bearer token)
            │   ├── models/              # Interfaces TypeScript dos domínios
            │   └── services/            # AuthService, OrganizationService, CollaboratorService, DeviceService
            ├── features/
            │   ├── auth/login/          # Tela de login
            │   ├── organizations/       # CRUD de organizações
            │   ├── collaborators/       # CRUD de colaboradores
            │   └── devices/             # CRUD de dispositivos
            └── shared/
                └── layout/             # Shell da aplicação (sidebar + router-outlet)
```
