-- V1: Criação da tabela de organizações
-- Sintaxe compatível com H2 (dev) e MySQL (produção via Docker)
CREATE TABLE organizations
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    name       VARCHAR(100) NOT NULL,
    cnpj       VARCHAR(18)  NOT NULL,
    address    VARCHAR(200),
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_organizations PRIMARY KEY (id),
    CONSTRAINT uq_organizations_cnpj UNIQUE (cnpj)
);
