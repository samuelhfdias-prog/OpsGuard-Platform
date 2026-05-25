-- V3: Criação da tabela de colaboradores
-- Sintaxe compatível com H2 (dev) e MySQL (produção via Docker)
CREATE TABLE collaborators
(
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    name            VARCHAR(100) NOT NULL,
    cpf             VARCHAR(14)  NOT NULL,
    email           VARCHAR(100) NOT NULL,
    position        VARCHAR(50)  NOT NULL,
    organization_id BIGINT       NOT NULL,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_collaborators PRIMARY KEY (id),
    CONSTRAINT uq_collaborators_cpf UNIQUE (cpf),
    CONSTRAINT uq_collaborators_email UNIQUE (email),
    CONSTRAINT fk_collaborators_organization
        FOREIGN KEY (organization_id) REFERENCES organizations (id)
);
